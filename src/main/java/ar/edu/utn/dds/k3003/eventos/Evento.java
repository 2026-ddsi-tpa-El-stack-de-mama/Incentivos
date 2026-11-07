package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Arma los eventos de Incentivos en el formato comun de la cola: {@code tipoEvento} es el nombre del
 * DTO y {@code descripcion} el DTO.
 *
 * <p>Los DTO llevan el estado resultante, no el delta: asi el consumidor puede hacer upsert
 * ignorando lo mas viejo que {@code ocurridoEn}, aunque lleguen repetidos o desordenados. Las fechas
 * van como String porque el consumidor las lee con un ObjectMapper sin soporte de java.time. Contrato
 * completo en docs/eventos_incentivos.md.
 */
public final class Evento {

  /** Motivos de {@link CategoriaDonadorEvento}. Mismos valores que la metrica de categoria. */
  public static final String MOTIVO_PROGRESO = "progreso";
  public static final String MOTIVO_REGRESION = "regresion";

  private Evento() {}

  public static EventoQueue misionInsignia(
      UUID donadorId, List<InsigniaDTO> insignias, List<MisionDTO> misiones,
      Integer progresoActual, Integer progresoRequerido) {
    return nuevo(new MisionInsigniaEvento(
        donadorId.toString(), insignias, misiones, progresoActual, progresoRequerido, ahora()));
  }

  public static EventoQueue categoriaCambiada(
      String donadorId, String categoriaAnterior, String categoriaNueva, String motivo) {
    return nuevo(new CategoriaDonadorEvento(
        AccionEvento.CATEGORIA_CAMBIADA.name(), donadorId, categoriaAnterior, categoriaNueva,
        motivo, ahora()));
  }

  private static EventoQueue nuevo(Object descripcion) {
    return new EventoQueue(
        UUID.randomUUID().toString(), descripcion.getClass().getSimpleName(), descripcion);
  }

  private static String ahora() {
    return Instant.now().toString();
  }
}
