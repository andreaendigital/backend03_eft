package cl.bancoxyz.eft.cuentas.messaging;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "eventos_procesados")
public class ProcessedEvent {
    @Id
    private UUID eventId;

    protected ProcessedEvent() { }
    public ProcessedEvent(UUID eventId) { this.eventId = eventId; }
}
