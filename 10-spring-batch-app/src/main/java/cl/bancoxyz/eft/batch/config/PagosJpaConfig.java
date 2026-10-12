package cl.bancoxyz.eft.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Asocia los repositorios del paquete "pagos" con el EntityManagerFactory
 * de banco_xyz_pagos.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "cl.bancoxyz.eft.batch.repository.pagos",
        entityManagerFactoryRef = "pagosEntityManagerFactory",
        transactionManagerRef  = "pagosTransactionManager"
)
public class PagosJpaConfig {
}
