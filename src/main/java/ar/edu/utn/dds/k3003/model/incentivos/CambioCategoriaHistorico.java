package ar.edu.utn.dds.k3003.model.incentivos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cambio_categoria_historico")
public class CambioCategoriaHistorico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private UUID donadorId;
  private String categoriaAnterior;
  private String categoriaNueva;
  private LocalDateTime fecha;

  public CambioCategoriaHistorico() {}

  public CambioCategoriaHistorico(UUID donadorId, String categoriaAnterior, String categoriaNueva) {
    this.donadorId = donadorId;
    this.categoriaAnterior = categoriaAnterior;
    this.categoriaNueva = categoriaNueva;
    this.fecha = LocalDateTime.now();
  }

  // getters y setters
  public Long getId() { return id; }
  public UUID getDonadorId() { return donadorId; }
  public String getCategoriaAnterior() { return categoriaAnterior; }
  public String getCategoriaNueva() { return categoriaNueva; }
  public LocalDateTime getFecha() { return fecha; }
}