package cl.bancoxyz.eft.pagos.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public void paymentProcessed(Object payload) {
        publish("pagos-procesados", new BankEvent(UUID.randomUUID(), "PAGO_PROCESADO",
                Instant.now(), 1, payload));
    }

    public void accountChanged(Object payload) {
        publish("cambios-cuenta", new BankEvent(UUID.randomUUID(), "CUENTA_ACTUALIZADA",
                Instant.now(), 1, payload));
    }

    public void securityAlert(Object payload) {
        publish("alertas-seguridad", new BankEvent(UUID.randomUUID(), "ALERTA_SEGURIDAD",
                Instant.now(), 1, payload));
    }

    private void publish(String topic, BankEvent event) {
        try {
            kafka.send(topic, event.id().toString(), mapper.writeValueAsString(event));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el evento Kafka", ex);
        }
    }
}
