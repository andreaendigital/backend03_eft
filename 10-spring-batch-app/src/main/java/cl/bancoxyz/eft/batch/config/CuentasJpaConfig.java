package cl.bancoxyz.eft.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Asocia los repositorios del paquete "cuentas" con el EntityManagerFactory
 * de banco_xyz_cuentas.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "cl.bancoxyz.eft.batch.repository.cuentas",
        entityManagerFactoryRef = "cuentasEntityManagerFactory",
        transactionManagerRef  = "cuentasTransactionManager"
)
public class CuentasJpaConfig {
}
