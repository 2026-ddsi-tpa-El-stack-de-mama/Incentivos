package ar.edu.utn.dds.k3003.model.incentivos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "donador_insignia",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"donador_id", "insignia_id"})
    })
public class DonadorInsignia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private UUID donadorId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insignia_id")
  private Insignia insignia;

  private LocalDateTime fechaAsignacion;

  public DonadorInsignia() {}

  public DonadorInsignia(UUID donadorId, Insignia insignia) {
    this.donadorId = donadorId;
    this.insignia = insignia;
    this.fechaAsignacion = LocalDateTime.now();
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

  public Insignia getInsignia() {
    return insignia;
  }

  public void setInsignia(Insignia insignia) {
    this.insignia = insignia;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }
}

