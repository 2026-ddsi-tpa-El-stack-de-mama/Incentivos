package ar.edu.utn.dds.k3003.repositories.entidades;

import ar.edu.utn.dds.k3003.model.Entidad;

import java.util.Optional;

public interface EntidadesRepository {

    Optional<Entidad> findById(String id);

    Entidad save(Entidad entidad);

    Entidad deleteById(String id);
}
