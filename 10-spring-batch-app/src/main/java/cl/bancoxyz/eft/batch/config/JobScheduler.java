package cl.bancoxyz.eft.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Lanza los tres Jobs de Spring Batch de forma programada y reintenta
 * automáticamente el job completo si termina en estado FAILED.
 *
 * Política de reintento:
 *  - Intentos máximos : configurable via batch.retry.max-attempts   (default 3)
 *  - Delay inicial    : configurable via batch.retry.initial-delay-ms (default 5000 ms)
 *  - Backoff          : cada reintento duplica el delay (exponential backoff)
 *
 * El parámetro "run.timestamp" garantiza que Spring Batch trate cada
 * ejecución programada como una instancia nueva (JobInstance distinta).
 */
@Component
public class JobScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    private final JobLauncher       jobLauncher;
    private final Job               transaccionesJob;
    private final Job               interesesJob;
    private final Job               estadosJob;

    @Value("${batch.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${batch.retry.initial-delay-ms:5000}")
    private long initialDelayMs;

    public JobScheduler(
            JobLauncher jobLauncher,
            @Qualifier("transaccionesDiariasJob")      Job transaccionesJob,
            @Qualifier("interesesTrimestralesJob")     Job interesesJob,
            @Qualifier("estadosFinancierosAnualesJob") Job estadosJob) {
        this.jobLauncher      = jobLauncher;
        this.transaccionesJob = transaccionesJob;
        this.interesesJob     = interesesJob;
        this.estadosJob       = estadosJob;
    }

    // ── Schedules ────────────────────────────────────────────────────────────

    /**
     * Transacciones diarias: se ejecuta cada día a las 01:00 AM.
     * Cron: segundo minuto hora díaMes mes díaSemana
     */
    @Scheduled(cron = "${batch.schedule.transacciones:0 0 1 * * *}")
    public void runTransaccionesDiarias() {
        runWithRetry(transaccionesJob, "transaccionesDiariasJob");
    }

    /**
     * Intereses trimestrales (con cálculo mensual): primer día de cada mes a las 02:00 AM.
     */
    @Scheduled(cron = "${batch.schedule.intereses:0 0 2 1 * *}")
    public void runInteresesTrimestrales() {
        runWithRetry(interesesJob, "interesesTrimestralesJob");
    }

    /**
     * Estados financieros anuales: 1 de enero a las 03:00 AM.
     */
    @Scheduled(cron = "${batch.schedule.estados:0 0 3 1 1 *}")
    public void runEstadosFinancieros() {
        runWithRetry(estadosJob, "estadosFinancierosAnualesJob");
    }

    // ── Lógica de reintento ──────────────────────────────────────────────────

    /**
     * Ejecuta el job y reintenta si el estado final es FAILED.
     * Usa backoff exponencial entre intentos.
     *
     * @param job     job a ejecutar
     * @param jobName nombre para logging
     */
    private void runWithRetry(Job job, String jobName) {
        long delayMs = initialDelayMs;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.info("[{}] Intento {}/{}", jobName, attempt, maxAttempts);

            JobExecution execution = launch(job, jobName, attempt);

            if (execution == null) {
                // Error al lanzar (p.ej. base de datos no disponible)
                log.warn("[{}] No se pudo lanzar en intento {}. Reintentando en {} ms.",
                        jobName, attempt, delayMs);
            } else if (execution.getStatus() == BatchStatus.COMPLETED) {
                log.info("[{}] Completado exitosamente en el intento {}.", jobName, attempt);
                return;
            } else {
                log.warn("[{}] Terminó con estado {} en intento {}. Exit: {}",
                        jobName, execution.getStatus(), attempt,
                        execution.getExitStatus().getExitCode());
            }

            if (attempt < maxAttempts) {
                sleep(delayMs, jobName);
                delayMs *= 2; // backoff exponencial
            }
        }

        log.error("[{}] Agotados {} intentos. El job no completó correctamente.", jobName, maxAttempts);
    }

    private JobExecution launch(Job job, String jobName, int attempt) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.timestamp", Instant.now().toEpochMilli())
                    .addLong("attempt",       (long) attempt)
                    .toJobParameters();
            return jobLauncher.run(job, params);
        } catch (Exception ex) {
            log.error("[{}] Excepción al lanzar el job: {}", jobName, ex.getMessage());
            return null;
        }
    }

    private void sleep(long ms, String jobName) {
        try {
            log.info("[{}] Esperando {} ms antes del siguiente intento.", jobName, ms);
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
