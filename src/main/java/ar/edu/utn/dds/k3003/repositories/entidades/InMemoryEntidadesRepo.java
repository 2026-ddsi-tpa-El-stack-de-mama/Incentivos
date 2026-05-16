package ar.edu.utn.dds.k3003.repositories.entidades;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEntidadesRepo implements EntidadesRepository {

    private static final InMemoryEntidadesRepo INSTANCE = new InMemoryEntidadesRepo();

    private final List<EntidadBeneficaDTO> entidades;
    private final AtomicLong idSecuencial = new AtomicLong(1);

    private InMemoryEntidadesRepo() {
        this.entidades = new ArrayList<>();
    }

    public static InMemoryEntidadesRepo getInstance() {
        return INSTANCE;
    }

    public synchronized void clear() {
        this.entidades.clear();
        this.idSecuencial.set(1);
    }

    @Override
    public synchronized Optional<EntidadBeneficaDTO> findById(String id) {
        return this.entidades.stream().filter(e -> e != null && e.id() != null && e.id().equals(id)).findFirst();
    }

    @Override
    public synchronized EntidadBeneficaDTO save(EntidadBeneficaDTO entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("Entidad nula");
        }

                final String id = (entidad.id() == null || entidad.id().isBlank())
                        ? String.valueOf(idSecuencial.getAndIncrement())
                        : entidad.id();

        var guardada = new EntidadBeneficaDTO(id, entidad.razonSocial(), entidad.domicilio(), entidad.telefono(), entidad.correo());
        this.entidades.removeIf(e -> e != null && e.id() != null && e.id().equals(id));
        this.entidades.add(guardada);
        return guardada;
    }

    @Override
    public synchronized EntidadBeneficaDTO deleteById(String id) {
        var entidad = this.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Entidad inexistente"));
        this.entidades.remove(entidad);
        return entidad;
    }

}
