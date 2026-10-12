# Despliegue en AWS — Banco XYZ

Este documento describe el proceso para desplegar el sistema en un entorno de nube AWS,
usando los Dockerfiles existentes en cada módulo y los servicios gestionados de AWS.

---

## Arquitectura objetivo en AWS

```
Internet
    │
    ▼
Application Load Balancer (ALB)
    │
    ├── /api/v1/web/*    → ECS Service: bff-web        (puerto 8087)
    ├── /api/v1/mobile/* → ECS Service: bff-movil      (puerto 8088)
    └── /api/v1/atm/*    → ECS Service: bff-cajeros    (puerto 8089)

Servicios internos (VPC privada):
    ├── ECS Service: config-server     (puerto 8888)
    ├── ECS Service: eureka-server     (puerto 8761)
    ├── ECS Service: auth-server       (puerto 9000)
    ├── ECS Service: service-cuentas   (puerto 8081)
    ├── ECS Service: service-pagos     (puerto 8082)
    ├── ECS Service: service-clientes  (puerto 8083)
    └── ECS Task (one-shot): spring-batch-app (puerto 8090)

Servicios gestionados:
    ├── Amazon RDS for MySQL 8.x       (reemplaza el contenedor mysql)
    ├── Amazon MSK (Kafka gestionado)  (reemplaza el contenedor kafka)
    └── Amazon ECR                     (registro de imágenes Docker)
```

---

## Herramientas necesarias

- AWS CLI v2 configurado con credenciales (`aws configure`)
- Docker Desktop
- `jq` (opcional, para parsear respuestas JSON de la CLI)

---

## Paso 1 — Crear repositorios en Amazon ECR

```bash
# Variables reutilizables
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_BASE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# Crear un repositorio por cada módulo
for SERVICE in config-server eureka-server auth-server \
               service-cuentas service-pagos service-clientes \
               bff-web bff-movil bff-cajeros spring-batch-app; do
  aws ecr create-repository --repository-name "bancoxyz/$SERVICE" --region $AWS_REGION
done
```

---

## Paso 2 — Construir y publicar imágenes Docker

```bash
# Autenticar Docker con ECR
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_BASE

# Construir y publicar cada imagen
declare -A DIRS=(
  [config-server]=01-config-server
  [eureka-server]=02-eureka-server
  [auth-server]=03-auth-server
  [service-cuentas]=04-service-cuentas
  [service-pagos]=05-service-pagos
  [service-clientes]=06-service-clientes
  [bff-web]=07-bff-web
  [bff-movil]=08-bff-movil
  [bff-cajeros]=09-bff-cajeros
  [spring-batch-app]=10-spring-batch-app
)

for SERVICE in "${!DIRS[@]}"; do
  DIR="${DIRS[$SERVICE]}"
  IMAGE="$ECR_BASE/bancoxyz/$SERVICE:latest"
  docker build -t "$IMAGE" "./$DIR"
  docker push "$IMAGE"
done
```

---

## Paso 3 — Crear la infraestructura de datos

### RDS for MySQL

```bash
aws rds create-db-instance \
  --db-instance-identifier bancoxyz-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version "8.0" \
  --master-username admin \
  --master-user-password <CONTRASEÑA_SEGURA> \
  --allocated-storage 20 \
  --vpc-security-group-ids <SG_ID> \
  --db-subnet-group-name <SUBNET_GROUP> \
  --no-publicly-accessible \
  --region $AWS_REGION
```

Una vez disponible, conectarse al endpoint RDS y crear los esquemas:

```sql
CREATE DATABASE IF NOT EXISTS banco_xyz_cuentas  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS banco_xyz_pagos     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS banco_xyz_clientes  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS banco_xyz_batch     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Amazon MSK (Kafka gestionado)

```bash
aws kafka create-cluster \
  --cluster-name bancoxyz-kafka \
  --kafka-version "3.5.1" \
  --number-of-broker-nodes 3 \
  --broker-node-group-info '{
    "InstanceType": "kafka.t3.small",
    "ClientSubnets": ["<SUBNET_A>","<SUBNET_B>","<SUBNET_C>"],
    "SecurityGroups": ["<SG_ID>"]
  }' \
  --region $AWS_REGION
```

Obtener el bootstrap URL del clúster MSK:

```bash
aws kafka get-bootstrap-brokers \
  --cluster-arn <CLUSTER_ARN> \
  --region $AWS_REGION
```

---

## Paso 4 — Guardar secretos en AWS Secrets Manager

```bash
# Credenciales de base de datos
aws secretsmanager create-secret \
  --name bancoxyz/db \
  --secret-string '{"username":"admin","password":"<CONTRASEÑA_SEGURA>"}' \
  --region $AWS_REGION
```

Los servicios Spring Boot leerán las credenciales via variables de entorno inyectadas
por ECS Task Definition desde Secrets Manager (ver Paso 5).

---

## Paso 5 — Crear el clúster ECS y los servicios

### Crear clúster ECS (Fargate)

```bash
aws ecs create-cluster \
  --cluster-name bancoxyz-cluster \
  --capacity-providers FARGATE \
  --region $AWS_REGION
```

### Task Definition — ejemplo para config-server

Guardar como `task-config-server.json`:

```json
{
  "family": "config-server",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskExecutionRole",
  "containerDefinitions": [{
    "name": "config-server",
    "image": "<ECR_BASE>/bancoxyz/config-server:latest",
    "portMappings": [{"containerPort": 8888}],
    "environment": [],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs/bancoxyz",
        "awslogs-region": "us-east-1",
        "awslogs-stream-prefix": "config-server"
      }
    }
  }]
}
```

```bash
aws ecs register-task-definition \
  --cli-input-json file://task-config-server.json \
  --region $AWS_REGION
```

### Task Definition — ejemplo para service-cuentas

Las variables de entorno apuntan al endpoint RDS y al MSK:

```json
{
  "family": "service-cuentas",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<ACCOUNT_ID>:role/ecsTaskExecutionRole",
  "containerDefinitions": [{
    "name": "service-cuentas",
    "image": "<ECR_BASE>/bancoxyz/service-cuentas:latest",
    "portMappings": [{"containerPort": 8081}],
    "environment": [
      {"name": "SPRING_CONFIG_IMPORT", "value": "optional:configserver:http://<CONFIG_SERVER_IP>:8888"},
      {"name": "EUREKA_URI",           "value": "http://<EUREKA_IP>:8761/eureka/"},
      {"name": "KAFKA_BOOTSTRAP_SERVERS", "value": "<MSK_BOOTSTRAP_URL>"},
      {"name": "AUTH_JWK_SET_URI",     "value": "http://<AUTH_SERVER_IP>:9000/.well-known/jwks.json"}
    ],
    "secrets": [
      {"name": "DB_USERNAME", "valueFrom": "arn:aws:secretsmanager:...:bancoxyz/db:username::"},
      {"name": "DB_PASSWORD", "valueFrom": "arn:aws:secretsmanager:...:bancoxyz/db:password::"}
    ],
    "environment": [
      {"name": "DB_URL", "value": "jdbc:mysql://<RDS_ENDPOINT>:3306/banco_xyz_cuentas?useSSL=true&serverTimezone=UTC"}
    ]
  }]
}
```

Repetir el mismo patrón para `service-pagos`, `service-clientes`, los BFF y el Batch,
ajustando imagen, puerto, `DB_URL` y variables específicas de cada servicio.

### Crear servicio ECS

```bash
aws ecs create-service \
  --cluster bancoxyz-cluster \
  --service-name config-server \
  --task-definition config-server \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[<SUBNET_A>,<SUBNET_B>],
    securityGroups=[<SG_ID>],
    assignPublicIp=DISABLED
  }" \
  --region $AWS_REGION
```

Repetir para cada servicio en el orden de dependencias:
1. `config-server`
2. `eureka-server`
3. `auth-server`
4. `service-cuentas`, `service-pagos`, `service-clientes`
5. `bff-web`, `bff-movil`, `bff-cajeros`
6. `spring-batch-app` (como tarea ECS de un solo uso, no como servicio continuo)

---

## Paso 6 — Application Load Balancer

```bash
# Crear ALB
aws elbv2 create-load-balancer \
  --name bancoxyz-alb \
  --subnets <SUBNET_A> <SUBNET_B> \
  --security-groups <SG_ALB_ID> \
  --region $AWS_REGION

# Crear Target Groups para cada BFF
for BFF in bff-web bff-movil bff-cajeros; do
  aws elbv2 create-target-group \
    --name bancoxyz-$BFF \
    --protocol HTTP \
    --port 80 \
    --target-type ip \
    --vpc-id <VPC_ID> \
    --health-check-path /actuator/health \
    --region $AWS_REGION
done

# Crear reglas de enrutamiento por path
# /api/v1/web/*    → bff-web
# /api/v1/mobile/* → bff-movil
# /api/v1/atm/*    → bff-cajeros
```

---

## Paso 7 — Ejecutar el Job Batch en AWS

El ETL Batch se ejecuta como tarea ECS puntual (no como servicio continuo):

```bash
aws ecs run-task \
  --cluster bancoxyz-cluster \
  --task-definition spring-batch-app \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[<SUBNET_A>],
    securityGroups=[<SG_ID>],
    assignPublicIp=DISABLED
  }" \
  --region $AWS_REGION
```

Verificar resultado:

```bash
aws ecs describe-tasks \
  --cluster bancoxyz-cluster \
  --tasks <TASK_ARN> \
  --region $AWS_REGION \
  --query 'tasks[0].lastStatus'
```

---

## Paso 8 — Monitoreo con CloudWatch

Los logs de cada contenedor se envían automáticamente al grupo `/ecs/bancoxyz` de CloudWatch
(configurado en el `logConfiguration` de las Task Definitions).

```bash
# Ver logs de un servicio
aws logs tail /ecs/bancoxyz --follow --filter-pattern service-cuentas

# Ver logs del Batch
aws logs tail /ecs/bancoxyz --follow --filter-pattern spring-batch-app
```

---

## Resumen de servicios AWS utilizados

| Servicio AWS | Reemplaza | Propósito |
|---|---|---|
| Amazon ECS (Fargate) | `docker compose` local | Orquestación de contenedores sin gestionar servidores |
| Amazon ECR | DockerHub local | Registro privado de imágenes Docker |
| Amazon RDS for MySQL 8 | Contenedor `mysql` | Base de datos gestionada con backups y alta disponibilidad |
| Amazon MSK | Contenedor `kafka` + `zookeeper` | Kafka gestionado, sin Zookeeper propio |
| AWS Secrets Manager | Variables en `docker-compose.yml` | Almacenamiento seguro de credenciales |
| Application Load Balancer | Acceso directo por puerto | Enrutamiento HTTP y punto de entrada único |
| Amazon CloudWatch | Logs locales | Centralización de logs y métricas |

---

## Notas de seguridad para producción

- Reemplazar `DB_PASSWORD: admin` por secretos en AWS Secrets Manager.
- Habilitar SSL en la conexión RDS (`useSSL=true`).
- Los servicios internos (core, eureka, config) deben estar en subredes privadas sin IP pública.
- Solo el ALB y los BFF deben estar en subredes públicas o con acceso desde Internet.
- Usar grupos de seguridad granulares: el ECS de `service-pagos` solo debe poder alcanzar `service-cuentas` en el puerto 8081.
