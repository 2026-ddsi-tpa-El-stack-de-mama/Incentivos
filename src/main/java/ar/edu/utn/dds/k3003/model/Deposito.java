package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

import java.util.List;

public class Deposito {
    private String id;
    private String nombre;
    private String direccion;
    private Integer capacidadMaxima;
    private List<Paquete> stockActual;
    private TipoAlgoritmoEnum tipoAlgoritmo;

    public Deposito(String id, String nombre, String direccion, int capacidad, List<Paquete> stock) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidad;
        this.stockActual = stock;
        this.tipoAlgoritmo = null;
    }

    public Boolean addPaquete(Paquete paquete) {
        if(paquete.getCantidad()> this.getCapacidadDisponible()) return false;
        stockActual.add(paquete);
        return true;
    }

    public Integer getCapacidadDisponible() {
        return capacidadMaxima - stockActual.stream().mapToInt(Paquete::getCantidad).sum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public List<Paquete> getStockActual() {
        return stockActual;
    }

    public void setStockActual(List<Paquete> stockActual) {
        this.stockActual = stockActual;
    }


    public TipoAlgoritmoEnum getTipoAlgoritmo() {
        return tipoAlgoritmo;
    }

    public void setTipoAlgoritmo(TipoAlgoritmoEnum tipoAlgoritmo) {
        this.tipoAlgoritmo = tipoAlgoritmo;
    }
}
