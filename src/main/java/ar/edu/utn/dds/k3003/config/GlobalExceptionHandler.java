package ar.edu.utn.dds.k3003.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import ar.edu.utn.dds.k3003.exceptions.ExternalBadRequestException;
import ar.edu.utn.dds.k3003.exceptions.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
    log.warn("Recurso no encontrado: {}", ex.getMessage());
    String requestId = MDC.get("request_id");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().format(TS));
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Not Found");
    body.put("message", ex.getMessage());
    body.put("request_id", requestId);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(body);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
    log.warn("Solicitud rechazada (400): {}", ex.getMessage(), ex);
    String requestId = MDC.get("request_id");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().format(TS));
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("request_id", requestId);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("Error interno no controlado", ex);
    String requestId = MDC.get("request_id");
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().format(TS));
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put("message", ex.getMessage());
    body.put("request_id", requestId);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("X-Request-Id", requestId).body(body);
  }

  @ExceptionHandler(ExternalBadRequestException.class)
  public ResponseEntity<String> handleBadRequest(ExternalBadRequestException e) {
    log.warn("Servicio externo rechazo la solicitud: {}", e.getMessage());
    return ResponseEntity.status(400).body(e.getMessage());
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<String> handleServiceError(ExternalServiceException e) {
    log.error("Fallo de servicio externo: {}", e.getMessage());
    return ResponseEntity.status(500).body(e.getMessage());
  }
}


