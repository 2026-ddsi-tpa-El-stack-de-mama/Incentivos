package ar.edu.utn.dds.k3003.repositories.donaciones;

import ar.edu.utn.dds.k3003.model.Donacion;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryDonacionesRepository implements DonacionRepository {

    private List<Donacion> donaciones;
    private AtomicLong idSecuencial = new AtomicLong(1);

    public InMemoryDonacionesRepository() {
        this.donaciones = new ArrayList<>();
    }

    @Override
    public Optional<Donacion> findById(String id) {
        return this.donaciones.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    @Override
    public Donacion save(Donacion donacion) {
        Donacion donacionConID = donacion;
        donacionConID.setId(String.valueOf(idSecuencial.getAndIncrement()));

        this.donaciones.add(donacionConID);
        return this.findById(donacionConID.getId()).get();
    }

    @Override
    public Donacion deleteById(String id) {
        val donacion = this.findById(id);
        this.donaciones.remove(donacion.get());
        return donacion.get();
    }

    @Override
    public Donacion modifyById(String id, Donacion donacion) {
        Optional<Donacion> existingDonacion = findById(id);
        if (existingDonacion.isEmpty()) {
            throw new NoSuchElementException("No Donacion found with ID: " + id);
        }
        Donacion currentDonacion = existingDonacion.get();

        if (donacion.getDonadorID() != null) {
            currentDonacion.setDonadorID(donacion.getDonadorID());
        }
        if (donacion.getDepositoID() != null) {
            currentDonacion.setDepositoID(donacion.getDepositoID());
        }
        if (donacion.getDescripcion() != null) {
            currentDonacion.setDescripcion(donacion.getDescripcion());
        }
        if (donacion.getProductoID() != null) {
            currentDonacion.setProductoID(donacion.getProductoID());
        }
        if (donacion.getCantidad() != null) {
            currentDonacion.setCantidad(donacion.getCantidad());
        }
        if (donacion.getEstado() != null) {
            currentDonacion.setEstado(donacion.getEstado());
        }

        return currentDonacion;
    }
}

