package ar.edu.utn.dds.k3003.dtos;

public record ResultadoProcesamiento(
    Estado estado,
    String misionId,
    boolean insigniaAsignada,
    boolean categoriaActualizada,
    String siguienteMisionId // null si no aplica
) {
  /**
   * Indica si el procesamiento modifico algo del donador: completo una mision
   * (historial, insignia, categoria y/o siguiente mision) o, sin tener mision
   * activa, se le enganchó una mision inicial. MISION_NO_COMPLETADA y
   * SIN_MISION_ACTIVA sin mision disponible no cambian nada.
   */
  public boolean huboCambios() {
    return estado == Estado.MISION_COMPLETADA
        || (estado == Estado.SIN_MISION_ACTIVA && siguienteMisionId != null);
  }

  public enum Estado {
    SIN_MISION_ACTIVA,
    MISION_NO_COMPLETADA,
    MISION_COMPLETADA
  }
}