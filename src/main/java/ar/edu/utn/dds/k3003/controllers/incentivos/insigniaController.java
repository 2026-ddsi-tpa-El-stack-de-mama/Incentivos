package ar.edu.utn.dds.k3003.controllers.incentivos;

import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/insignias")
public class insigniaController {

  private final Fachada fachada;
  private final MetricasNegocio metricas;
  private static final Logger logger = LoggerFactory.getLogger(insigniaController.class);

  public insigniaController(Fachada fachada, MetricasNegocio metricas) {
    this.fachada = fachada;
    this.metricas = metricas;
  }

  @PostMapping
  public ResponseEntity<?> postInsignia(@RequestBody InsigniaDTO insigniaDTO) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] POST /insignias - crear insignia: {}", requestId, insigniaDTO);

    if (insigniaDTO == null) {
      metricas.errores400.increment();
      return ResponseEntity.badRequest().header("X-Request-Id", requestId).body("Insignia nula");
    }

    try {
      InsigniaDTO creada = fachada.agregarInsignia(insigniaDTO);
      metricas.insigniasCreadas.increment();
      return ResponseEntity.status(HttpStatus.CREATED)
          .header("X-Request-Id", requestId)
          .body(creada);
    } catch (RuntimeException ex) {
      metricas.errores400.increment();
      logger.warn("[{}] POST /insignias - error: {}", requestId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> putInsignia(
      @PathVariable("id") String insigniaID,
      @RequestBody InsigniaDTO insigniaDTO) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] PUT /insignias/{} - modificar insignia: {}", requestId, insigniaID, insigniaDTO);

    if (insigniaDTO == null) {
      metricas.errores400.increment();
      return ResponseEntity.badRequest().header("X-Request-Id", requestId).body("Insignia nula");
    }
    if (insigniaDTO.id() != null && !insigniaID.equals(insigniaDTO.id())) {
      metricas.errores400.increment();
      return ResponseEntity.badRequest().header("X-Request-Id", requestId).body("Id de insignia inconsistente");
    }

    try {
      InsigniaDTO modificada = fachada.modificarInsignia(
          new InsigniaDTO(insigniaID, insigniaDTO.nombre(), insigniaDTO.descripcion()));
      return ResponseEntity.ok().header("X-Request-Id", requestId).body(modificada);
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (RuntimeException ex) {
      if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("inexistente")) {
        metricas.errores404.increment();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
      }
      metricas.errores400.increment();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteInsignia(@PathVariable("id") String insigniaID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] DELETE /insignias/{} - eliminar insignia", requestId, insigniaID);
    try {
      fachada.eliminarInsignia(insigniaID);
      metricas.insigniasEliminadas.increment();
      return ResponseEntity.ok().header("X-Request-Id", requestId).body("Insignia eliminada exitosamente");
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (RuntimeException ex) {
      if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("inexistente")) {
        metricas.errores404.increment();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
      }
      metricas.errores400.increment();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<List<InsigniaDTO>> getInsignias() {
    String requestId = MDC.get("request_id");
    logger.info("[{}] GET /insignias - listar insignias", requestId);
    var lista = fachada.listarInsignias();
    return ResponseEntity.ok().header("X-Request-Id", requestId).body(lista);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getInsigniaById(@PathVariable("id") String insigniaID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] GET /insignias/{} - buscar insignia por id", requestId, insigniaID);
    try {
      return ResponseEntity.ok().header("X-Request-Id", requestId).body(fachada.buscarInsigniaPorID(insigniaID));
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @PostMapping("/{insigniaID}/donadores/{donadorID}")
  public ResponseEntity<?> asignarInsigniaADonador(
      @PathVariable("insigniaID") String insigniaID,
      @PathVariable("donadorID") String donadorID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] POST /insignias/{}/donadores/{} - asignar insignia a donador", requestId, insigniaID, donadorID);
    try {
      InsigniaDTO insigniaDTO = fachada.buscarInsigniaPorID(insigniaID);
      fachada.asignarInsigniaADonador(donadorID, insigniaDTO);
      metricas.insigniasAsignadas.increment();
      return ResponseEntity.status(HttpStatus.OK).header("X-Request-Id", requestId).body("Insignia asignada exitosamente");
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (RuntimeException ex) {
      metricas.errores400.increment();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @GetMapping("/donadores/{donadorID}")
  public ResponseEntity<?> getInsigniasDeDonador(@PathVariable("donadorID") String donadorID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] GET /insignias/donadores/{} - obtener insignias de donador", requestId, donadorID);
    try {
      List<InsigniaDTO> insignias = fachada.getInsigniasDeDonador(donadorID);
      return ResponseEntity.ok().header("X-Request-Id", requestId).body(insignias);
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (Exception ex) {
      metricas.errores500.increment();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }
}