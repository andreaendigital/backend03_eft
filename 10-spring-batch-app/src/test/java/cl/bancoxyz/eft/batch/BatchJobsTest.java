package cl.bancoxyz.eft.batch;

import static org.assertj.core.api.Assertions.assertThat;

import cl.bancoxyz.eft.batch.repository.cuentas.EstadoFinancieroRepository;
import cl.bancoxyz.eft.batch.repository.pagos.InteresRepository;
import cl.bancoxyz.eft.batch.repository.cuentas.MovimientoRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class BatchJobsTest {
    @Autowired private JobLauncherTestUtils jobs;
    @Autowired private MovimientoRepository movimientos;
    @Autowired private InteresRepository intereses;
    @Autowired private EstadoFinancieroRepository estados;
    @Autowired @Qualifier("transaccionesDiariasJob") private Job transaccionesJob;
    @Autowired @Qualifier("interesesTrimestralesJob") private Job interesesJob;
    @Autowired @Qualifier("estadosFinancierosAnualesJob") private Job estadosJob;
    @Test
    void transaccionesDiariasJobCompletesAndPersistsValidRows() throws Exception {
        jobs.setJob(transaccionesJob);
        JobExecution execution = jobs.launchJob(parameters());
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(movimientos.count()).isGreaterThan(0);
    }

    @Test
    void interesesTrimestralesJobCompletesAndCalculatesInterest() throws Exception {
        jobs.setJob(interesesJob);
        JobExecution execution = jobs.launchJob(parameters());
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(intereses.count()).isGreaterThan(0);
    }

    @Test
    void estadosFinancierosAnualesJobCompletesAndPersistsAuditRows() throws Exception {
        jobs.setJob(estadosJob);
        JobExecution execution = jobs.launchJob(parameters());
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(estados.count()).isGreaterThan(0);
    }

    private JobParameters parameters() {
        return new JobParametersBuilder()
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();
    }
}
