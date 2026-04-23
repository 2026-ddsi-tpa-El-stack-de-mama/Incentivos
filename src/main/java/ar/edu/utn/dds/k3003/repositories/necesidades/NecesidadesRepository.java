package ar.edu.utn.dds.k3003.repositories.necesidades;

import ar.edu.utn.dds.k3003.model.Necesidad;

import java.util.List;
import java.util.Optional;

public interface NecesidadesRepository {

    Optional<Necesidad> findById(String id);

    Necesidad save(Necesidad necesidad);

    Necesidad deleteById(String id);

    Boolean doesNecesityExist(String productoID);


    List<Necesidad> findAll();
}
