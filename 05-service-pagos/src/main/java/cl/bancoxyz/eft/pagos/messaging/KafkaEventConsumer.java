package cl.bancoxyz.eft.pagos.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KafkaEventConsumer {
    private final ObjectMapper mapper;
    private final ProcessedEventRepository processed;

    public KafkaEventConsumer(ObjectMapper mapper, ProcessedEventRepository processed) {
        this.mapper = mapper;
        this.processed = processed;
    }

    @RetryableTopic(attempts = "3", dltTopicSuffix = ".DLQ")
    @KafkaListener(topics = {"pagos-procesados", "cambios-cuenta", "alertas-seguridad"},
            groupId = "service-pagos")
    @Transactional
    public void consume(String rawEvent) throws Exception {
        JsonNode event = mapper.readTree(rawEvent);
        UUID eventId = UUID.fromString(event.path("id").asText());
        if (processed.existsById(eventId)) {
            return;
        }
        processed.save(new ProcessedEvent(eventId));
    }

    @DltHandler
    public void consumeDeadLetter(String rawEvent) { }
}
