package ar.edu.utn.dds.k3003.model.incentivos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "misiones")
public class Mision {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String nombre;

  @Enumerated(EnumType.STRING)
  private TipoMisionEnum tipo;

  @Enumerated(EnumType.STRING)
  private CategoriaDonadorEnum categoriaInicio;

  @Enumerated(EnumType.STRING)
  private CategoriaDonadorEnum categoriaFin;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insignia_id")
  private Insignia insignia;

  public Mision() {}

  public Mision(String id, String nombre, String insigniaID, String categoriaInicio, String categoriaFin) {
    this(id, nombre, insigniaID, categoriaInicio, categoriaFin, null);
  }

  public Mision(String id, String nombre, String insigniaID, String categoriaInicio, String categoriaFin, TipoMisionEnum tipo) {
    this.id = id != null ? UUID.fromString(id) : null;
    this.nombre = nombre;
    this.tipo = tipo;
    this.categoriaInicio = categoriaInicio != null ? CategoriaDonadorEnum.valueOf(categoriaInicio) : null;
    this.categoriaFin = categoriaFin != null ? CategoriaDonadorEnum.valueOf(categoriaFin) : null;
    // insigniaID se setea mediante setInsigniaID si es necesario
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

  public String getInsigniaID() {
    return insignia != null ? insignia.getId() : null;
  }

  public void setInsigniaID(String insigniaID) {
    // Este método se mantiene por compatibilidad; la relación real es con la entidad Insignia
  }

  public String getCategoriaInicio() {
    return categoriaInicio != null ? categoriaInicio.name() : null;
  }

  public void setCategoriaInicio(String categoriaInicio) {
    this.categoriaInicio = categoriaInicio != null ? CategoriaDonadorEnum.valueOf(categoriaInicio) : null;
  }

  public String getCategoriaFin() {
    return categoriaFin != null ? categoriaFin.name() : null;
  }

  public void setCategoriaFin(String categoriaFin) {
    this.categoriaFin = categoriaFin != null ? CategoriaDonadorEnum.valueOf(categoriaFin) : null;
  }

  public TipoMisionEnum getTipo() {
    return tipo;
  }

  public void setTipo(TipoMisionEnum tipo) {
    this.tipo = tipo;
  }

  public Insignia getInsignia() {
    return insignia;
  }

  public void setInsignia(Insignia insignia) {
    this.insignia = insignia;
  }
}

