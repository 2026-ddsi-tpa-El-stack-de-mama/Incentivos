package ar.edu.utn.dds.k3003.repositories.paquetes;

import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaquetesRepository {
    Optional<Paquete> findById(String id);

    Paquete save(Paquete paquete);

    Paquete deleteById(String id);

    Paquete modifyById(String id, Paquete paquete);
}
