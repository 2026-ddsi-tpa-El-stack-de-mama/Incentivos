package ar.edu.utn.dds.k3003.model.incentivos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mision_historico")
public class MisionHistorico {

  public enum EstadoMision {
    ACTIVA,
    COMPLETADA,
    CANCELADA
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private UUID donadorId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mision_id")
  private Mision mision;

  @Enumerated(EnumType.STRING)
  private EstadoMision estado;

  private LocalDateTime fechaInicio;

  private LocalDateTime fechaFin;

  public MisionHistorico() {}

  public MisionHistorico(UUID donadorId, Mision mision, EstadoMision estado) {
    this.donadorId = donadorId;
    this.mision = mision;
    this.estado = estado;
    this.fechaInicio = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UUID getDonadorId() {
    return donadorId;
  }

  public void setDonadorId(UUID donadorId) {
    this.donadorId = donadorId;
  }

  public Mision getMision() {
    return mision;
  }

  public void setMision(Mision mision) {
    this.mision = mision;
  }

  public EstadoMision getEstado() {
    return estado;
  }

  public void setEstado(EstadoMision estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }
}

