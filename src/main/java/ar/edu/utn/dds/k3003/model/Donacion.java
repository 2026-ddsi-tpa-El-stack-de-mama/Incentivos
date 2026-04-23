package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;

public class Donacion {
    private String id;
    private String donadorID;
    private String depositoID;
    private String descripcion;
    private String productoID;
    private Integer cantidad;
    private EstadoDonacionEnum estado;

    public Donacion(String id, String donadorID, String depositoID, String descripcion, String productoID, Integer cantidad, EstadoDonacionEnum estado) {
        this.id = id;
        this.donadorID = donadorID;
        this.depositoID = depositoID;
        this.descripcion = descripcion;
        this.productoID = productoID;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDonadorID() {
        return donadorID;
    }

    public void setDonadorID(String donadorID) {
        this.donadorID = donadorID;
    }

    public String getDepositoID() {
        return depositoID;
    }

    public void setDepositoID(String depositoID) {
        this.depositoID = depositoID;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getProductoID() {
        return productoID;
    }

    public void setProductoID(String productoID) {
        this.productoID = productoID;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public EstadoDonacionEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoDonacionEnum estado) {
        this.estado = estado;
    }
}
