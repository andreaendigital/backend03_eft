package cl.bancoxyz.eft.batch.repository.pagos;

import cl.bancoxyz.eft.batch.model.pagos.InteresTrimestral;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de intereses trimestrales.
 * Opera sobre banco_xyz_pagos (pagosEntityManagerFactory).
 */
public interface InteresRepository extends JpaRepository<InteresTrimestral, Long> {
    boolean existsByCuentaId(String cuentaId);
}
