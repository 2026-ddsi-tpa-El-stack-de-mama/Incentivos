package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Asignacion {
    private String id;
    private String paqueteID;
    private String necesidadID;
    private LocalDateTime fecha;
    private EstadoAsginacionEnum estado;

    private List<HistorialEstadoAsignacion> historialEstados;

    public Asignacion(String id, String paqueteID, String necesidadID, LocalDateTime fecha, EstadoAsginacionEnum estado) {
        this.id = id;
        this.paqueteID = paqueteID;
        this.necesidadID = necesidadID;
        this.fecha = fecha;
        this.estado = estado;
        this.historialEstados = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaqueteID() {
        return paqueteID;
    }

    public void setPaqueteID(String paqueteID) {
        this.paqueteID = paqueteID;
    }

    public String getNecesidadID() {
        return necesidadID;
    }

    public void setNecesidadID(String necesidadID) {
        this.necesidadID = necesidadID;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoAsginacionEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsginacionEnum estado) {
        HistorialEstadoAsignacion h = new HistorialEstadoAsignacion(this.getId(), estado, LocalDateTime.now());

        this.historialEstados.add(h);
        this.estado = estado;
    }
}
