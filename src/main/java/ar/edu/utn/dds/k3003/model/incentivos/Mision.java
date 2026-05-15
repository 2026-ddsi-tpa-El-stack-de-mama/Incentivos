package ar.edu.utn.dds.k3003.model.incentivos;

public class Mision {
  private String id;
  private String nombre;
  private String insigniaID;
  private String categoriaInicio;
  private String categoriaFin;

  public Mision() {}

  public Mision(String id, String nombre, String insigniaID, String categoriaInicio, String categoriaFin) {
    this.id = id;
    this.nombre = nombre;
    this.insigniaID = insigniaID;
    this.categoriaInicio = categoriaInicio;
    this.categoriaFin = categoriaFin;
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

}

