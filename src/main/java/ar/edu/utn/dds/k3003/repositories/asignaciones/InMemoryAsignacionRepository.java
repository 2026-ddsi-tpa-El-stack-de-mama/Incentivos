package ar.edu.utn.dds.k3003.repositories.asignaciones;

import ar.edu.utn.dds.k3003.model.Asignacion;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAsignacionRepository implements AsignacionRepository {

    private List<Asignacion> asignaciones = new ArrayList<>();
    private AtomicLong idSecuencial = new AtomicLong(1);

    public Optional<Asignacion> findById(String id) {
        return asignaciones.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public Asignacion save(Asignacion asignacion) {
        Asignacion asignacionConID = asignacion;
        asignacionConID.setId(String.valueOf(idSecuencial.getAndIncrement()));
        asignaciones.add(asignacionConID);
        return findById(asignacionConID.getId()).get();
    }

    public Asignacion deleteById(String id) {
        Optional<Asignacion> asignacion = findById(id);
        asignaciones.remove(asignacion.get());
        return asignacion.get();
    }

    @Override
    public Optional<Asignacion> findAsignacionByPaqueteId(String paqueteId) {
        return asignaciones.stream().filter(a -> a.getPaqueteID().equals(paqueteId)).findFirst();
    }

    @Override
    public Asignacion modifyById(String id, Asignacion asignacion) {
        Optional<Asignacion> existingAsignacion = findById(id);
        if (existingAsignacion.isEmpty()) {
            throw new NoSuchElementException("No Asignacion found with ID: " + id);
        }
        Asignacion currentAsignacion = existingAsignacion.get();

        if (asignacion.getPaqueteID() != null) {
            currentAsignacion.setPaqueteID(asignacion.getPaqueteID());
        }
        if (asignacion.getNecesidadID() != null) {
            currentAsignacion.setNecesidadID(asignacion.getNecesidadID());
        }
        if (asignacion.getFecha() != null) {
            currentAsignacion.setFecha(asignacion.getFecha());
        }
        if (asignacion.getEstado() != null) {
            currentAsignacion.setEstado(asignacion.getEstado());
        }

        return currentAsignacion;
    }

}
