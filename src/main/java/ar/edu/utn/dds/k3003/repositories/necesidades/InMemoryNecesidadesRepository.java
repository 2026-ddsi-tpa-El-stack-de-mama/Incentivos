package ar.edu.utn.dds.k3003.repositories.necesidades;

import ar.edu.utn.dds.k3003.model.Necesidad;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryNecesidadesRepository implements NecesidadesRepository {

    private List<Necesidad> necesidades;
    private AtomicLong idSecuencial = new AtomicLong(1);

    public InMemoryNecesidadesRepository() {
        this.necesidades = new ArrayList<>();
    }

    public Optional<Necesidad> findById(String id) {
        return this.necesidades.stream().filter(n -> n.getId().equals(id)).findFirst();
    }

    public Necesidad save(Necesidad necesidad) {
        Necesidad necesidadConID = necesidad;
        necesidadConID.setId(String.valueOf(idSecuencial.getAndIncrement()));

        this.necesidades.add(necesidadConID);
        return this.findById(necesidadConID.getId()).get();
    }

    public Necesidad deleteById(String id) {
        val necesidad = this.findById(id);
        this.necesidades.remove(necesidad.get());
        return necesidad.get();
    }

    @Override
    public Boolean doesNecesityExist(String productoID) {
        return this.necesidades.stream().anyMatch(n -> n.getProductoID().equals(productoID));
    }

    public List<Necesidad> getNecesidadesByID(List<String> ids) {
        List<Necesidad> necesidades = new ArrayList<>();
        for (String id : ids) {
            Optional<Necesidad> necesidad = this.findById(id);
            if (necesidad.isPresent()) {
                necesidades.add(necesidad.get());
            }
        }
        return necesidades;
    }

    @Override
    public List<Necesidad> findAll() {
        return this.necesidades;
    }


}
