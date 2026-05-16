package ar.edu.utn.dds.k3003.controllers.incentivos;


import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/insignias")
public class insigniaController {

	private final Fachada fachada;

	public insigniaController(Fachada fachada) {
		this.fachada = fachada;
	}

	@PostMapping
	public ResponseEntity<?> postInsignia(@RequestBody InsigniaDTO insigniaDTO) {
		if (insigniaDTO == null) {
			return ResponseEntity.badRequest().body("Insignia nula");
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(fachada.agregarInsignia(insigniaDTO));
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<List<InsigniaDTO>> getInsignias() {
		return ResponseEntity.ok(fachada.listarInsignias());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getInsigniaById(@PathVariable("id") String insigniaID) {
		try {
			return ResponseEntity.ok(fachada.buscarInsigniaPorID(insigniaID));
		} catch (NoSuchElementException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		}
	}

	@PostMapping("/{insigniaID}/donadores/{donadorID}")
	public ResponseEntity<?> asignarInsigniaADonador(
			@PathVariable("insigniaID") String insigniaID,
			@PathVariable("donadorID") String donadorID) {
		try {
			InsigniaDTO insigniaDTO = fachada.buscarInsigniaPorID(insigniaID);
			fachada.asignarInsigniaADonador(donadorID, insigniaDTO);
			return ResponseEntity.status(HttpStatus.OK).body("Insignia asignada exitosamente");
		} catch (NoSuchElementException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}
	}

	@GetMapping("/donadores/{donadorID}")
	public ResponseEntity<?> getInsigniasDeDonador(@PathVariable("donadorID") String donadorID) {
		try {
			List<InsigniaDTO> insignias = fachada.getInsigniasDeDonador(donadorID);
			return ResponseEntity.ok(insignias);
		} catch (NoSuchElementException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		}
	}

}
