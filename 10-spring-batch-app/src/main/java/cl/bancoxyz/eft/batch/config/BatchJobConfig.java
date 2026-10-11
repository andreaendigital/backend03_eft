package cl.bancoxyz.eft.batch.config;

import cl.bancoxyz.eft.batch.listener.RejectSkipListener;
import cl.bancoxyz.eft.batch.model.*;
import cl.bancoxyz.eft.batch.model.input.*;
import cl.bancoxyz.eft.batch.processor.*;
import cl.bancoxyz.eft.batch.writer.*;
import cl.bancoxyz.eft.batch.support.BatchDataException;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJobConfig {
    private <T> FlatFileItemReader<T> reader(String file, String[] names, Class<T> type) {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(names);
        tokenizer.setStrict(false);
        BeanWrapperFieldSetMapper<T> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(type);
        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);
        FlatFileItemReader<T> reader = new FlatFileItemReader<>();
        reader.setName(file.replace(".csv", "Reader"));
        reader.setResource(new ClassPathResource("data/" + file));
        reader.setLinesToSkip(1);
        reader.setLineMapper(lineMapper);
        reader.setEncoding("UTF-8");
        return reader;
    }

    @Bean
    FlatFileItemReader<MovimientoInput> movimientoReader() {
        return reader("movimientos_financieros_diarios.csv",
                new String[]{"id", "fecha", "monto", "tipo"}, MovimientoInput.class);
    }
    @Bean
    FlatFileItemReader<InteresInput> interesReader() {
        return reader("intereses_trimestrales.csv",
                new String[]{"cuentaId", "nombre", "saldo", "edad", "tipo"}, InteresInput.class);
    }
    @Bean
    FlatFileItemReader<EstadoFinancieroInput> estadoReader() {
        return reader("estados_financieros_anuales.csv",
                new String[]{"cuentaId", "fecha", "transaccion", "monto", "descripcion"},
                EstadoFinancieroInput.class);
    }

    @Bean
    Step transaccionesDiariasStep(JobRepository repository, PlatformTransactionManager tx,
                                  FlatFileItemReader<MovimientoInput> reader,
                                  MovimientoProcessor processor,
                                  IdempotentMovimientoWriter writer,
                                  RejectSkipListener listener) {
        return new StepBuilder("transaccionesDiariasStep", repository)
                .<MovimientoInput, Movimiento>chunk(50, tx).reader(reader).processor(processor)
                .writer(writer).faultTolerant().skipLimit(2000)
                .skip(BatchDataException.class).listener(listener).build();
    }

    @Bean
    Step interesesTrimestralesStep(JobRepository repository, PlatformTransactionManager tx,
                                   FlatFileItemReader<InteresInput> reader,
                                   InteresProcessor processor,
                                   IdempotentInteresWriter writer,
                                   RejectSkipListener listener) {
        return new StepBuilder("interesesTrimestralesStep", repository)
                .<InteresInput, InteresTrimestral>chunk(50, tx).reader(reader).processor(processor)
                .writer(writer).faultTolerant().skipLimit(2000)
                .skip(BatchDataException.class).listener(listener).build();
    }

    @Bean
    Step estadosFinancierosAnualesStep(JobRepository repository, PlatformTransactionManager tx,
                                       FlatFileItemReader<EstadoFinancieroInput> reader,
                                       EstadoFinancieroProcessor processor,
                                       EstadoFinancieroWriter writer,
                                       RejectSkipListener listener) {
        return new StepBuilder("estadosFinancierosAnualesStep", repository)
                .<EstadoFinancieroInput, EstadoFinanciero>chunk(50, tx).reader(reader).processor(processor)
                .writer(writer).faultTolerant().skipLimit(2000)
                .skip(BatchDataException.class).listener(listener).build();
    }

    @Bean
    Job transaccionesDiariasJob(JobRepository repository,
                                @Qualifier("transaccionesDiariasStep") Step step) {
        return new JobBuilder("transaccionesDiariasJob", repository).start(step).build();
    }
    @Bean
    Job interesesTrimestralesJob(JobRepository repository,
                                 @Qualifier("interesesTrimestralesStep") Step step) {
        return new JobBuilder("interesesTrimestralesJob", repository).start(step).build();
    }
    @Bean
    Job estadosFinancierosAnualesJob(JobRepository repository,
                                     @Qualifier("estadosFinancierosAnualesStep") Step step) {
        return new JobBuilder("estadosFinancierosAnualesJob", repository).start(step).build();
    }
}
