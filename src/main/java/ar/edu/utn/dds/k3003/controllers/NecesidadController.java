package ar.edu.utn.dds.k3003.controllers;


import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.repositories.DonadoresYEntidadesDataMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/necesidades")
public class NecesidadController {

    private final Fachada fachada;
    private final DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper;


    public NecesidadController(Fachada fachada, DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper) {
        this.fachada = fachada;
        this.donadoresYEntidadesDataMapper = donadoresYEntidadesDataMapper;
    }

    @PostMapping
    public ResponseEntity<?> createNecesidad(@RequestBody NecesidadInputDTO necesidad) {
        NecesidadMaterialDTO necesidadMaterialDTO = new NecesidadMaterialDTO("", necesidad.entidadID(), necesidad.nivelDeUrgencia(), necesidad.descripcion(),
                necesidad.cantidadObjetivo(), necesidad.productoSolicitado(), null);

        fachada.agregarNecesidadMaterial(necesidadMaterialDTO);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
