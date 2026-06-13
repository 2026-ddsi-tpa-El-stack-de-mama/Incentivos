package ar.edu.utn.dds.k3003.controllers.incentivos;

import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import java.util.List;
import java.util.NoSuchElementException;
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

@RestController
@RequestMapping("/misiones")
public class misionController {

  private final Fachada fachada;
  private final MetricasNegocio metricas;
  private static final Logger logger = LoggerFactory.getLogger(misionController.class);

  public misionController(Fachada fachada, MetricasNegocio metricas) {
    this.fachada = fachada;
    this.metricas = metricas;
  }

  @PostMapping
  public ResponseEntity<?> postMision(@RequestBody MisionDTO misionDTO) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] POST /misiones - crear mision: {}", requestId, misionDTO);

    if (misionDTO == null) {
      metricas.errores400.increment();
      return ResponseEntity.badRequest().header("X-Request-Id", requestId).body("Mision nula");
    }

    try {
      MisionDTO creada = fachada.agregarMision(misionDTO);
      metricas.misionesCreadas.increment();
      return ResponseEntity.status(HttpStatus.CREATED).header("X-Request-Id", requestId).body(creada);
    } catch (RuntimeException ex) {
      metricas.errores400.increment();
      logger.warn("[{}] POST /misiones - error: {}", requestId, ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> putMision(
      @PathVariable("id") String misionID,
      @RequestBody MisionDTO misionDTO) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] PUT /misiones/{} - modificar mision: {}", requestId, misionID, misionDTO);

    if (misionDTO == null) {
      metricas.errores400.increment();
      return ResponseEntity.badRequest().header("X-Request-Id", requestId).body("Mision nula");
    }
    if (misionDTO.id() != null && !misionID.equals(misionDTO.id())) {
      metricas.errores400.increment();
      return ResponseEntity.badRequest().header("X-Request-Id", requestId).body("Id de mision inconsistente");
    }

    try {
      MisionDTO modificada = fachada.modificarMision(
          new MisionDTO(
              misionID,
              misionDTO.nombre(),
              misionDTO.insigniaID(),
              misionDTO.categoriaInicio(),
              misionDTO.categoriaFin(),
              misionDTO.tipo()));
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
  public ResponseEntity<?> deleteMision(@PathVariable("id") String misionID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] DELETE /misiones/{} - eliminar mision", requestId, misionID);
    try {
      fachada.eliminarMision(misionID);
      return ResponseEntity.ok().header("X-Request-Id", requestId).body("Mision eliminada exitosamente");
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
  public ResponseEntity<List<MisionDTO>> getMisiones() {
    String requestId = MDC.get("request_id");
    logger.info("[{}] GET /misiones - listar misiones", requestId);
    var lista = fachada.listarMisiones();
    return ResponseEntity.ok().header("X-Request-Id", requestId).body(lista);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getMisionById(@PathVariable("id") String misionID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] GET /misiones/{} - buscar mision por id", requestId, misionID);
    try {
      return ResponseEntity.ok().header("X-Request-Id", requestId).body(fachada.buscarMisionPorID(misionID));
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @PostMapping("/{misionID}/donadores/{donadorID}")
  public ResponseEntity<?> asignarMisionADonador(
      @PathVariable("misionID") String misionID,
      @PathVariable("donadorID") String donadorID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] POST /misiones/{}/donadores/{} - asignar mision a donador", requestId, misionID, donadorID);
    try {
      MisionDTO misionDTO = fachada.buscarMisionPorID(misionID);
      fachada.asignarMisionADonador(donadorID, misionDTO);
      metricas.misionesAsignadas.increment();
      return ResponseEntity.status(HttpStatus.OK).header("X-Request-Id", requestId).body("Misión asignada exitosamente");
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (RuntimeException ex) {
      metricas.errores400.increment();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @GetMapping("/donadores/{donadorID}/mision")
  public ResponseEntity<?> getMisionEnCursoDeDonador(@PathVariable("donadorID") String donadorID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] GET /misiones/donadores/{}/mision - obtener mision en curso", requestId, donadorID);
    try {
      MisionDTO mision = fachada.getMisionEnCursoDeDonador(donadorID);
      if (mision == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .header("X-Request-Id", requestId)
            .body("El donador no tiene misión en curso");
      }
      return ResponseEntity.ok().header("X-Request-Id", requestId).body(mision);
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (Exception ex) {
      metricas.errores500.increment();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }

  @PostMapping("/donadores/{donadorID}/procesar")
  public ResponseEntity<?> procesarDonador(@PathVariable("donadorID") String donadorID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] POST /misiones/donadores/{}/procesar - procesar donador", requestId, donadorID);
    try {
      fachada.procesarDonador(donadorID);
      metricas.donadoresProcesados.increment();
      return ResponseEntity.ok().header("X-Request-Id", requestId).body("Donador procesado exitosamente");
    } catch (NoSuchElementException ex) {
      metricas.errores404.increment();
      return ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (RuntimeException ex) {
      metricas.errores400.increment();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("X-Request-Id", requestId).body(ex.getMessage());
    } catch (Exception ex) {
      metricas.errores500.increment();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("X-Request-Id", requestId).body(ex.getMessage());
    }
  }
}