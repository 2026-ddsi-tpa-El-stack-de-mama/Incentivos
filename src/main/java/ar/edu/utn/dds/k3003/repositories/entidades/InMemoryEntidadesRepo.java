package ar.edu.utn.dds.k3003.repositories.entidades;

import ar.edu.utn.dds.k3003.model.Entidad;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEntidadesRepo implements EntidadesRepository {

    private List<Entidad> entidades;
    private AtomicLong idSecuencial = new AtomicLong(1);

    public InMemoryEntidadesRepo() {
        this.entidades = new ArrayList<>();
    }

    public Optional<Entidad> findById(String id) {
        return this.entidades.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public Entidad save(Entidad entidad) {
        entidad.setId(String.valueOf(idSecuencial.getAndIncrement()));
        this.entidades.add(entidad);
        return this.findById(entidad.getId()).get();
    }

    public Entidad deleteById(String id) {
        val entidad = this.findById(id);
        this.entidades.remove(entidad.get());
        return entidad.get();
    }


}
