package cl.bancoxyz.eft.clientes.messaging;

import java.time.Instant;
import java.util.UUID;

public record BankEvent(UUID id, String tipo, Instant fecha, int version, Object payload) { }
