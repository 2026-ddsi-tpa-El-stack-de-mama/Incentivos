package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.repositories.DonadoresYEntidadesDataMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/asignaciones")
public class AsignacionController {
    private final Fachada fachada;
    private final DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper;


    public AsignacionController(Fachada fachada, DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper) {
        this.fachada = fachada;
        this.donadoresYEntidadesDataMapper = donadoresYEntidadesDataMapper;
    }

    
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getAsignacionById(@PathVariable("id") String asignacionID) {
        try {
            AsignacionDTO asignacionDTO = fachada.buscarAsignacionPorID(asignacionID);
            return asignacionDTO != null ? ResponseEntity.ok(asignacionDTO) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving asignacion: " + ex.getMessage());
        }
    }


}

