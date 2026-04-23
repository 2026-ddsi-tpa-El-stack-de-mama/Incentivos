package ar.edu.utn.dds.k3003.repositories.necesidades;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PrioridadSubAtendidos implements MatchmakingAlgorithm {
    @Override
    public Optional<NecesidadMaterialDTO> findNecesity(PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
        return necesidades.stream()
            .filter(n -> n.productoSolicitadoID().equals(paqueteDTO.producto()))
            .max(Comparator.comparingInt(n -> (n.cantidadObjetivo())));
    }
}
