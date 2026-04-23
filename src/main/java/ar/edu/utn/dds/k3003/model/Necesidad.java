package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;

public class Necesidad {

    private String id;
    private String entidadID;
    private Integer nivelDeUrgencia;
    private String descripcion;
    private Integer cantidadObjetivo;
    private String productoID;
    private Integer cantidadRecibida;
    private TipoNecesidadMaterialEnum tipoNecesidad;

    public Necesidad(String id, String entidadID, Integer nivelDeUrgencia, String descripcion, Integer cantidadObjetivo, String productoSolicitado,
                     Integer cantidadRecibida, TipoNecesidadMaterialEnum tipoNecesidad) {
        this.id = id;
        this.entidadID = entidadID;
        this.nivelDeUrgencia = nivelDeUrgencia;
        this.descripcion = descripcion;
        this.cantidadObjetivo = cantidadObjetivo;
        this.productoID = productoSolicitado;
        this.cantidadRecibida = cantidadRecibida;
        this.tipoNecesidad = tipoNecesidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntidadID() {
        return entidadID;
    }

    public void setEntidadID(String entidadID) {
        this.entidadID = entidadID;
    }

    public Integer getNivelDeUrgencia() {
        return nivelDeUrgencia;
    }

    public void setNivelDeUrgencia(Integer nivelDeUrgencia) {
        this.nivelDeUrgencia = nivelDeUrgencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidadObjetivo() {
        return cantidadObjetivo;
    }

    public void setCantidadObjetivo(Integer cantidadObjetivo) {
        this.cantidadObjetivo = cantidadObjetivo;
    }

    public String getProductoID() {
        return productoID;
    }

    public void setProductoID(String productoSolicitado) {
        this.productoID = productoSolicitado;
    }

    public Integer getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(Integer cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public TipoNecesidadMaterialEnum getTipoNecesidad() {
        return tipoNecesidad;
    }

    public void setTipoNecesidad(TipoNecesidadMaterialEnum tipoNecesidad) {
        this.tipoNecesidad = tipoNecesidad;
    }

}
