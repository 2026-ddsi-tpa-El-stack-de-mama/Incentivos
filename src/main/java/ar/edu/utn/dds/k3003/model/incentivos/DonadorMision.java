package ar.edu.utn.dds.k3003.model.incentivos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donador_mision")
public class DonadorMision {

  @Id
  private UUID donadorId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mision_id")
  private Mision mision;

  private LocalDateTime fechaAsignacion;

  public DonadorMision() {}

  public DonadorMision(UUID donadorId, Mision mision) {
    this.donadorId = donadorId;
    this.mision = mision;
    this.fechaAsignacion = LocalDateTime.now();
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

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }
}

