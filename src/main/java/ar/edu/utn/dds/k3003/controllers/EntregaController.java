package ar.edu.utn.dds.k3003.controllers;


import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.repositories.DonadoresYEntidadesDataMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entregas")
public class EntregaController {

    private final Fachada fachada;
    private final DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper;


    public EntregaController(Fachada fachada, DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper) {
        this.fachada = fachada;
        this.donadoresYEntidadesDataMapper = donadoresYEntidadesDataMapper;
    }

    @PostMapping
    public ResponseEntity<?> registrarEntrega(@RequestBody String paqueteID) {
        try {
            PaqueteDTO paqueteDTO = fachada.buscarPaquetePorID(paqueteID);
            if (paqueteDTO == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            fachada.reportarEntrega(paqueteDTO);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing entrega: " + ex.getMessage());
        }
    }
}
