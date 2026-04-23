package ar.edu.utn.dds.k3003.repositories.donaciones;

import ar.edu.utn.dds.k3003.model.Donacion;

import java.util.Optional;

public interface DonacionRepository {

    Optional<Donacion> findById(String id);

    Donacion save(Donacion donacion);

    Donacion deleteById(String id);

    Donacion modifyById(String id, Donacion donacion);
}
