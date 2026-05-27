package ar.edu.utn.dds.k3003.controllers.incentivos;

import ar.edu.utn.dds.k3003.Fachada;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/donadores")
public class donadorController {

  private final Fachada fachada;

  public donadorController(Fachada fachada) {
    this.fachada = fachada;
  }

  @PatchMapping("/{donadorID}/categoria")
  public ResponseEntity<?> registrarCambioCategoria(
      @PathVariable("donadorID") String donadorID,
      @RequestBody CambioCategoriaRequest request) {
    if (request == null || request.nuevaCategoria() == null || request.nuevaCategoria().isBlank()) {
      return ResponseEntity.badRequest().body("Categoria nueva nula");
    }

    try {
      fachada.registrarCambioCategoriaEnDonador(donadorID, request.nuevaCategoria().trim());
      return ResponseEntity.ok("Categoria actualizada exitosamente");
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    } catch (RuntimeException ex) {
      if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("inexistente")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
  }

  @GetMapping("/{donadorID}/categorias/historial")
  public ResponseEntity<List<String>> historialCategorias(@PathVariable("donadorID") String donadorID) {
    return ResponseEntity.ok(fachada.historialCategorias(donadorID));
  }

  private record CambioCategoriaRequest(String nuevaCategoria) {}
}

