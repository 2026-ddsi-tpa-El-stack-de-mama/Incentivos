package ar.edu.utn.dds.k3003.controllers.incentivos;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/misiones")
public class misionController {

  private final Fachada fachada;

  public misionController(Fachada fachada) {
    this.fachada = fachada;
  }

  @PostMapping
  public ResponseEntity<?> postMision(@RequestBody MisionDTO misionDTO) {
    if (misionDTO == null) {
      return ResponseEntity.badRequest().body("Mision nula");
    }

    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(fachada.agregarMision(misionDTO));
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<List<MisionDTO>> getMisiones() {
    return ResponseEntity.ok(fachada.listarMisiones());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getMisionById(@PathVariable("id") String misionID) {
    try {
      return ResponseEntity.ok(fachada.buscarMisionPorID(misionID));
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
  }

  @PostMapping("/{misionID}/donadores/{donadorID}")
  public ResponseEntity<?> asignarMisionADonador(
      @PathVariable("misionID") String misionID,
      @PathVariable("donadorID") String donadorID) {
    try {
      MisionDTO misionDTO = fachada.buscarMisionPorID(misionID);
      fachada.asignarMisionADonador(donadorID, misionDTO);
      return ResponseEntity.status(HttpStatus.OK).body("Misión asignada exitosamente");
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
  }

  @GetMapping("/donadores/{donadorID}/mision")
  public ResponseEntity<?> getMisionEnCursoDeDonador(@PathVariable("donadorID") String donadorID) {
    try {
      MisionDTO mision = fachada.getMisionEnCursoDeDonador(donadorID);
      return ResponseEntity.ok(mision);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
  }

  @PostMapping("/donadores/{donadorID}/procesar")
  public ResponseEntity<?> procesarDonador(@PathVariable("donadorID") String donadorID) {
    try {
      fachada.procesarDonador(donadorID);
      return ResponseEntity.ok("Donador procesado exitosamente");
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
  }

}
