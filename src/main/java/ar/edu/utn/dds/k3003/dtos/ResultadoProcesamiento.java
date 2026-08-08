package ar.edu.utn.dds.k3003.dtos;

public record ResultadoProcesamiento(
    Estado estado,
    String misionId,
    boolean insigniaAsignada,
    boolean categoriaActualizada,
    String siguienteMisionId // null si no aplica
) {
  public enum Estado {
    SIN_MISION_ACTIVA,
    MISION_NO_COMPLETADA,
    MISION_COMPLETADA
  }
}