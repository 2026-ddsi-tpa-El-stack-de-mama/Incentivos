package ar.edu.utn.dds.k3003.repositories.paquetes;

import ar.edu.utn.dds.k3003.model.Paquete;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryPaqueteRepository implements PaquetesRepository {

    private List<Paquete> paquetes;
    private AtomicLong idSecuencial = new AtomicLong(1);

    public InMemoryPaqueteRepository() {
        this.paquetes = new ArrayList<>();
    }

    @Override
    public Optional<Paquete> findById(String id) {
        return this.paquetes.stream().filter(p -> p.getPaqueteID().equals(id)).findFirst();
    }

    @Override
    public Paquete save(Paquete paquete) {
        Paquete paqueteConID = paquete;
        paqueteConID.setPaqueteID(String.valueOf(idSecuencial.getAndIncrement()));

        this.paquetes.add(paqueteConID);
        return this.findById(paqueteConID.getPaqueteID()).get();
    }

    @Override
    public Paquete deleteById(String id) {
        val paquete = this.findById(id);
        this.paquetes.remove(paquete.get());
        return paquete.get();
    }

    @Override
    public Paquete modifyById(String id, Paquete paquete) {
        val existingPaquete = this.findById(id).orElseThrow(() -> new IllegalArgumentException("Paquete with ID: " + id + " not found"));

        existingPaquete.setDonacionID(paquete.getDonacionID());
        existingPaquete.setProductoID(paquete.getProductoID());
        existingPaquete.setCantidad(paquete.getCantidad());
        existingPaquete.setEntidadAsignadaID(paquete.getEntidadAsignadaID());


        return existingPaquete;
    }
}
