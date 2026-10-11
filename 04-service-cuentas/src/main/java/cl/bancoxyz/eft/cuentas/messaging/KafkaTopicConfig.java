package cl.bancoxyz.eft.cuentas.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean NewTopic pagosProcesados() { return new NewTopic("pagos-procesados", 3, (short) 1); }
    @Bean NewTopic cambiosCuenta() { return new NewTopic("cambios-cuenta", 3, (short) 1); }
    @Bean NewTopic alertasSeguridad() { return new NewTopic("alertas-seguridad", 3, (short) 1); }
}
