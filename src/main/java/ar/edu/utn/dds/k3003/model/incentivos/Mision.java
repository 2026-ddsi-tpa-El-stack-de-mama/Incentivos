package ar.edu.utn.dds.k3003.model.incentivos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;

public class Mision {
  private String id;
  private String nombre;
  private String insigniaID;
  private String categoriaInicio;
  private String categoriaFin;
  private TipoMisionEnum tipo;

  public Mision() {}

  public Mision(String id, String nombre, String insigniaID, String categoriaInicio, String categoriaFin) {
    this(id, nombre, insigniaID, categoriaInicio, categoriaFin, null);
  }

  public Mision(String id, String nombre, String insigniaID, String categoriaInicio, String categoriaFin, TipoMisionEnum tipo) {
    this.id = id;
    this.nombre = nombre;
    this.insigniaID = insigniaID;
    this.categoriaInicio = categoriaInicio;
    this.categoriaFin = categoriaFin;
    this.tipo = tipo;
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

  public String getInsigniaID() {
    return insigniaID;
  }

  public void setInsigniaID(String insigniaID) {
    this.insigniaID = insigniaID;
  }

  public String getCategoriaInicio() {
    return categoriaInicio;
  }

  public void setCategoriaInicio(String categoriaInicio) {
    this.categoriaInicio = categoriaInicio;
  }

  public String getCategoriaFin() {
    return categoriaFin;
  }

  public void setCategoriaFin(String categoriaFin) {
    this.categoriaFin = categoriaFin;
  }

  public TipoMisionEnum getTipo() {
    return tipo;
  }

  public void setTipo(TipoMisionEnum tipo) {
    this.tipo = tipo;
  }

}

