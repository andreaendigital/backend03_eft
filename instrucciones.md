# Instrucciones de ejecución y pruebas — Banco XYZ

Este documento explica paso a paso cómo ejecutar y probar cada componente del sistema,
tanto en modo local como con Docker Compose.

---

## Requisitos previos

| Herramienta | Versión mínima | Para qué se usa |
|---|---|---|
| JDK | 17 | Compilar y ejecutar todos los módulos |
| Maven | 3.9 | Build de cada proyecto |
| Docker Desktop | 4.x | Docker Compose y construcción de imágenes |
| MySQL | 8.x | Base de datos (solo si no se usa Docker) |
| Postman o `curl` | cualquiera | Probar endpoints REST |

---

## Opción A — Docker Compose (recomendada)

Levanta el stack completo con un solo comando. No requiere instalar MySQL, Kafka ni Zookeeper localmente.

```powershell
# Desde la raíz del proyecto
docker compose up --build
```

Esperar a que todos los servicios muestren estado `healthy` o `started`. El orden de inicio
es gestionado automáticamente por las dependencias declaradas en `docker-compose.yml`.

Para ejecutar en segundo plano:

```powershell
docker compose up --build -d
docker compose ps          # ver estado de cada contenedor
docker compose logs -f     # ver logs en tiempo real (Ctrl+C para salir)
docker compose logs -f service-cuentas   # logs de un servicio específico
```

Para detener:

```powershell
docker compose down        # detiene y elimina contenedores, conserva volúmenes
docker compose down -v     # elimina también los datos persistentes de MySQL
```

---

## Opción B — Ejecución local con Maven

### 1. Inicializar bases de datos MySQL

Conectarse a MySQL y ejecutar:

```sql
CREATE DATABASE IF NOT EXISTS banco_xyz_cuentas  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS banco_xyz_pagos     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS banco_xyz_clientes  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS banco_xyz_batch     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Iniciar Kafka y Zookeeper

Con Docker (solo los servicios de mensajería):

```powershell
docker compose up -d zookeeper kafka
```

### 3. Compilar todos los módulos

```powershell
Get-ChildItem -Directory | Where-Object { Test-Path "$($_.FullName)\pom.xml" } |
  ForEach-Object { Push-Location $_.FullName; mvn -B clean install -DskipTests; Pop-Location }
```

### 4. Arrancar en orden

Abrir una terminal por cada servicio (o usar el runner de tu IDE):

```powershell
# Terminal 1
Set-Location .\01-config-server;   mvn spring-boot:run

# Terminal 2 — esperar a que config-server esté UP
Set-Location .\02-eureka-server;   mvn spring-boot:run

# Terminal 3
Set-Location .\03-auth-server;     mvn spring-boot:run

# Terminales 4-6
Set-Location .\04-service-cuentas; mvn spring-boot:run
Set-Location .\05-service-pagos;   mvn spring-boot:run
Set-Location .\06-service-clientes;mvn spring-boot:run

# Terminales 7-9
Set-Location .\07-bff-web;         mvn spring-boot:run
Set-Location .\08-bff-movil;       mvn spring-boot:run
Set-Location .\09-bff-cajeros;     mvn spring-boot:run

# Terminal 10 — ETL Batch (ejecuta los jobs y termina)
Set-Location .\10-spring-batch-app;mvn spring-boot:run
```

---

## Verificar salud de la infraestructura

```http
GET http://localhost:8888/actuator/health   → Config Server
GET http://localhost:8761/actuator/health   → Eureka Server
GET http://localhost:9000/actuator/health   → Auth Server
GET http://localhost:8081/actuator/health   → service-cuentas
GET http://localhost:8082/actuator/health   → service-pagos
GET http://localhost:8083/actuator/health   → service-clientes
```

Respuesta esperada en todos: `HTTP 200` con `{"status":"UP"}`.

Consola de Eureka (servicios registrados):

```
http://localhost:8761
```

---

## Parte 1 — Probar Spring Batch (ETL)

### Ejecución automática

Al arrancar `10-spring-batch-app`, los tres Jobs se lanzan automáticamente
(`spring.batch.job.enabled: true`) y procesan los CSV en `src/main/resources/data/`.

### Verificar resultados en MySQL

Conectarse a MySQL y consultar:

```sql
-- Movimientos migrados en banco_xyz_cuentas
USE banco_xyz_cuentas;
SELECT COUNT(*) FROM movimientos_financieros_diarios;
SELECT * FROM movimientos_financieros_diarios LIMIT 10;

-- Estados financieros anuales
SELECT COUNT(*) FROM estados_financieros_anuales;

-- Intereses trimestrales en banco_xyz_pagos
USE banco_xyz_pagos;
SELECT COUNT(*) FROM intereses_trimestrales;
SELECT cuenta_id, saldo, interes_calculado, tipo FROM intereses_trimestrales LIMIT 10;

-- Metadatos de ejecución Spring Batch
USE banco_xyz_batch;
SELECT JOB_INSTANCE_ID, JOB_NAME, STATUS, EXIT_CODE
FROM BATCH_JOB_EXECUTION
ORDER BY START_TIME DESC;
```

### Pruebas automatizadas

```powershell
Set-Location .\10-spring-batch-app
mvn -B test
```

Los tests verifican:
- Los tres Jobs terminan con estado `COMPLETED`.
- Las tablas destino contienen registros (`count() > 0`).
- Los rechazos se registran en `target/rejects/batch-rejections.log`.

### Registros rechazados

```powershell
Get-Content .\10-spring-batch-app\target\rejects\batch-rejections.log
```

---

## Parte 2 — Probar los BFF

### Obtener un token JWT

**POST** `http://localhost:9000/oauth2/token`

- Authorization: Basic Auth → usuario `eft-client`, contraseña configurada en `03-auth-server`
- Body `x-www-form-urlencoded`:

```
grant_type=client_credentials
scope=openid profile
```

Guardar el valor de `access_token`. Se usa como `Bearer <token>` en todas las peticiones siguientes.

### BFF Web — payload completo

```http
GET http://localhost:8087/api/v1/web/clientes/{clienteId}/dashboard
Authorization: Bearer <token>
```

Respuesta: objeto agregado con datos del cliente y lista de sus cuentas.

```http
GET http://localhost:8087/api/v1/web/cuentas/{cuentaId}
Authorization: Bearer <token>
```

### BFF Móvil — payload reducido

```http
GET http://localhost:8088/api/v1/mobile/clientes/{clienteId}/resumen
Authorization: Bearer <token>
```

Respuesta: lista ligera con `id`, `numero` y `saldo` de cada cuenta.

### BFF Cajeros — scope ATM requerido

Para este BFF el token debe incluir el scope `atm`. Solicitar con:

```
scope=openid profile atm
```

```http
GET http://localhost:8089/api/v1/atm/cuentas/{cuentaId}/saldo
Authorization: Bearer <token-con-scope-atm>
```

Respuesta: `{ "cuentaId": "...", "saldo": 1500.00, "estado": "ACTIVA" }`

Con un token sin scope `atm` la respuesta es `403 Forbidden`.

---

## Parte 3 — Probar los microservicios core

### Gestión de clientes

**Crear cliente:**

```http
POST http://localhost:8083/api/v1/clientes
Authorization: Bearer <token>
Content-Type: application/json

{
  "rut": "12345678-9",
  "nombres": "Juan",
  "apellidos": "González",
  "email": "juan.gonzalez@example.com",
  "telefono": "+56912345678"
}
```

Respuesta: `201 Created` con `id` (UUID), datos completos y `creadoEn`.

**Consultar cliente:**

```http
GET http://localhost:8083/api/v1/clientes/{id}
Authorization: Bearer <token>
```

### Gestión de cuentas

**Abrir cuenta:**

```http
POST http://localhost:8081/api/v1/cuentas
Authorization: Bearer <token>
Content-Type: application/json

{
  "clienteId": "<UUID_DEL_CLIENTE>",
  "numero": "001-000001",
  "tipo": "AHORRO"
}
```

Respuesta: `201 Created` con `id`, `clienteId`, `numero`, `tipo`, `saldo: 0`, `estado: ACTIVA` y `creadaEn`.

**Consultar cuentas de un cliente:**

```http
GET http://localhost:8081/api/v1/cuentas/cliente/{clienteId}
Authorization: Bearer <token>
```

**Consultar movimientos migrados por el Batch:**

```http
GET http://localhost:8081/api/v1/cuentas/{cuentaId}/movimientos
Authorization: Bearer <token>
```

### Procesamiento de pagos

**Registrar pago/transferencia:**

```http
POST http://localhost:8082/api/v1/pagos
Authorization: Bearer <token>
Content-Type: application/json

{
  "cuentaOrigenId": "<UUID_CUENTA_ORIGEN>",
  "cuentaDestinoId": "<UUID_CUENTA_DESTINO>",
  "monto": 50000.00,
  "tipo": "TRANSFERENCIA"
}
```

Respuesta: `201 Created` con estado final `COMPLETADO`. Si la cuenta origen no tiene fondos o el
servicio de cuentas no responde, el workflow ejecuta compensación y devuelve el error.

**Consultar intereses calculados por el Batch:**

```sql
USE banco_xyz_pagos;
SELECT * FROM intereses_trimestrales WHERE cuenta_id = '<cuenta>';
```

### Verificar eventos Kafka

```powershell
# Listar topics (requiere docker-compose con Kafka corriendo)
docker compose exec kafka kafka-topics --bootstrap-server localhost:29092 --list

# Consumir mensajes de un topic
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic pagos-procesados \
  --from-beginning \
  --max-messages 5
```

### Verificar Resilience4j

Con `service-cuentas` detenido, enviar un pago desde `service-pagos`. El Circuit Breaker
debe abrirse tras el número de fallos configurado (`sliding-window-size: 10`,
`failure-rate-threshold: 50`) y devolver el error de compensación sin bloquear el hilo.

---

## Variables de entorno configurables

Todas las variables tienen valores por defecto para desarrollo local. En Docker Compose
se sobrescriben automáticamente.

| Variable | Servicio | Valor por defecto |
|---|---|---|
| `DB_BATCH_URL` | spring-batch-app | `jdbc:mysql://localhost:3306/banco_xyz_batch?...` |
| `DB_CUENTAS_URL` | spring-batch-app | `jdbc:mysql://localhost:3306/banco_xyz_cuentas?...` |
| `DB_PAGOS_URL` | spring-batch-app | `jdbc:mysql://localhost:3306/banco_xyz_pagos?...` |
| `DB_URL` | core services | URL de su base respectiva |
| `DB_USERNAME` | todos | `root` |
| `DB_PASSWORD` | todos | `admin` |
| `KAFKA_BOOTSTRAP_SERVERS` | core services | `localhost:9092` |
| `EUREKA_URI` | core + BFF | `http://localhost:8761/eureka/` |
| `AUTH_JWK_SET_URI` | core + BFF | `http://localhost:9000/.well-known/jwks.json` |
| `CUENTAS_SERVICE_URL` | service-pagos | `http://localhost:8081` |
