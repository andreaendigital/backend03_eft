package cl.bancoxyz.eft.clientes.error;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String error, String message, String path) { }
