package cl.bancoxyz.eft.batch.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class RejectSkipListener implements SkipListener<Object, Object> {
    private static final Logger log = LoggerFactory.getLogger(RejectSkipListener.class);
    @Override public void onSkipInRead(Throwable throwable) { record("read", throwable); }
    @Override public void onSkipInProcess(Object item, Throwable throwable) {
        record("process|" + item, throwable);
    }
    @Override public void onSkipInWrite(Object item, Throwable throwable) {
        record("write|" + item, throwable);
    }
    private synchronized void record(String phase, Throwable throwable) {
        String line = phase + "|" + throwable.getMessage() + System.lineSeparator();
        try {
            Files.createDirectories(Path.of("target/rejects"));
            Files.writeString(Path.of("target/rejects/batch-rejections.log"), line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            log.error("No se pudo registrar el rechazo batch", exception);
        }
        log.warn("Registro omitido en fase {}: {}", phase, throwable.getMessage());
    }
}
