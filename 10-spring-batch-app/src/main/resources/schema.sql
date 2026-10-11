CREATE DATABASE IF NOT EXISTS banco_xyz_batch;
USE banco_xyz_batch;

CREATE TABLE IF NOT EXISTS movimientos_financieros_diarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_id VARCHAR(100) NOT NULL,
    fecha DATE NOT NULL,
    monto DECIMAL(19, 2) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_movimientos_source_id (source_id)
);

CREATE TABLE IF NOT EXISTS intereses_trimestrales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cuenta_id VARCHAR(100) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    saldo DECIMAL(19, 2) NOT NULL,
    edad INT NULL,
    tipo VARCHAR(20) NOT NULL,
    interes_calculado DECIMAL(19, 2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_intereses_cuenta_id (cuenta_id)
);

CREATE TABLE IF NOT EXISTS estados_financieros_anuales (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cuenta_id VARCHAR(100) NOT NULL,
    fecha DATE NOT NULL,
    transaccion VARCHAR(20) NOT NULL,
    monto DECIMAL(19, 2) NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_estados_cuenta_fecha_transaccion (cuenta_id, fecha, transaccion)
);
