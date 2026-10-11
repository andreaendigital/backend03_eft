package cl.bancoxyz.eft.bffmovil.error;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handle(Exception ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError(
                Instant.now(), 502, "Bad Gateway", "No fue posible completar la solicitud BFF",
                exchange.getRequest().getPath().value()));
    }
}
