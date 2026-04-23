package ar.edu.utn.dds.k3003.repositories.necesidades;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;

import java.util.List;
import java.util.Optional;

public interface MatchmakingAlgorithm {

    Optional<NecesidadMaterialDTO> findNecesity(PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades);
}
