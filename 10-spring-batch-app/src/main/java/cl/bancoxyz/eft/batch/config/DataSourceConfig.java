package cl.bancoxyz.eft.batch.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configura múltiples DataSources para el proceso ETL:
 *
 *  - batchDataSource   (primary) → banco_xyz_batch   : metadatos de Spring Batch
 *  - cuentasDataSource           → banco_xyz_cuentas : movimientos y estados financieros
 *  - pagosDataSource             → banco_xyz_pagos   : intereses trimestrales
 *
 * Cada DataSource tiene su propio EntityManagerFactory y TransactionManager
 * para que JPA no mezcle las bases de datos.
 */
@Configuration
public class DataSourceConfig {

    // ── DataSource principal (Spring Batch metadata) ──────────────────────────

    @Primary
    @Bean("batchDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }

    // ── DataSource de cuentas (movimientos + estados financieros) ─────────────

    @Bean("cuentasDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-cuentas")
    public DataSource cuentasDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("cuentasEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cuentasEntityManagerFactory(
            @Qualifier("cuentasDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("cl.bancoxyz.eft.batch.model.cuentas");
        factory.setPersistenceUnitName("cuentas");
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        factory.setJpaVendorAdapter(adapter);
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.MySQLDialect",
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.show_sql", "false"
        ));
        return factory;
    }

    @Bean("cuentasTransactionManager")
    public PlatformTransactionManager cuentasTransactionManager(
            @Qualifier("cuentasEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // ── DataSource de pagos (intereses trimestrales) ──────────────────────────

    @Bean("pagosDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-pagos")
    public DataSource pagosDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("pagosEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pagosEntityManagerFactory(
            @Qualifier("pagosDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("cl.bancoxyz.eft.batch.model.pagos");
        factory.setPersistenceUnitName("pagos");
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(true);
        factory.setJpaVendorAdapter(adapter);
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.MySQLDialect",
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.show_sql", "false"
        ));
        return factory;
    }

    @Bean("pagosTransactionManager")
    public PlatformTransactionManager pagosTransactionManager(
            @Qualifier("pagosEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
