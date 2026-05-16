package ar.edu.utn.dds.k3003.repositories.entidades;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;

import java.util.Optional;

public interface EntidadesRepository {

    Optional<EntidadBeneficaDTO> findById(String id);

    EntidadBeneficaDTO save(EntidadBeneficaDTO entidad);

    EntidadBeneficaDTO deleteById(String id);
}
