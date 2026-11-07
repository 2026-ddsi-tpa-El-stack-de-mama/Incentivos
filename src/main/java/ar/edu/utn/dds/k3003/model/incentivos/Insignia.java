package ar.edu.utn.dds.k3003.model.incentivos;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "insignias")
public class Insignia {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String nombre;

  private String descripcion;

  public static final String DESCRIPCION_FUERA_DE_SERVICIO = "insignia fuera de servicio";

  public Insignia() {}

  public Insignia(String id, String nombre, String descripcion) {
    this.id = id != null ? UUID.fromString(id) : null;
    this.nombre = nombre;
    this.descripcion = descripcion;
  }

  public String getId() {
    return id != null ? id.toString() : null;
  }

  public void setId(String id) {
    this.id = id != null ? UUID.fromString(id) : null;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public boolean isFueraDeServicio() {
    return DESCRIPCION_FUERA_DE_SERVICIO.equals(descripcion);
  }
}

