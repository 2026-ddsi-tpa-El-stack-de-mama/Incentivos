package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Arma los eventos de Incentivos en el formato comun de la cola: {@code tipoEvento} es el nombre del
 * DTO y {@code descripcion} el DTO.
 *
 * <p>Los DTO llevan el estado resultante, no el delta: asi el consumidor puede hacer upsert por id
 * ignorando lo mas viejo que {@code ocurridoEn}, aunque lleguen repetidos o desordenados. Las fechas
 * van como String porque el consumidor las lee con un ObjectMapper sin soporte de java.time. Contrato
 * completo en docs/eventos_incentivos.md.
 */
public final class Evento {

  /** Motivos de {@link CategoriaDonadorEvento}. Mismos valores que la metrica de categoria. */
  public static final String MOTIVO_PROGRESO = "progreso";
  public static final String MOTIVO_REGRESION = "regresion";

  private Evento() {}

  public static EventoQueue insignia(AccionEvento accion, InsigniaDTO insignia) {
    return nuevo(new InsigniaEvento(
        accion.name(), insignia.id(), insignia.nombre(), insignia.descripcion(), ahora()));
  }

  public static EventoQueue insigniaEliminada(String id) {
    return nuevo(new InsigniaEvento(AccionEvento.INSIGNIA_ELIMINADA.name(), id, null, null, ahora()));
  }

  public static EventoQueue mision(AccionEvento accion, MisionDTO mision) {
    return nuevo(new MisionEvento(
        accion.name(), mision.id(), mision.nombre(), mision.insigniaID(),
        mision.categoriaInicio(), mision.categoriaFin(), mision.tipo(), ahora()));
  }

  public static EventoQueue misionEliminada(String id) {
    return nuevo(new MisionEvento(
        AccionEvento.MISION_ELIMINADA.name(), id, null, null, null, null, null, ahora()));
  }

  public static EventoQueue misionDeDonador(AccionEvento accion, MisionHistorico historico) {
    Mision mision = historico.getMision();
    return nuevo(new MisionDonadorEvento(
        accion.name(),
        historico.getId(),
        historico.getDonadorId().toString(),
        mision.getId(),
        mision.getNombre(),
        mision.getTipo(),
        categoria(mision.getCategoriaInicio()),
        categoria(mision.getCategoriaFin()),
        historico.getEstado().name(),
        fecha(historico.getFechaInicio()),
        fecha(historico.getFechaFin()),
        ahora()));
  }

  public static EventoQueue insigniaDeDonador(AccionEvento accion, UUID donadorId, Insignia insignia) {
    return nuevo(new InsigniaDonadorEvento(
        accion.name(), donadorId.toString(), insignia.getId(), insignia.getNombre(),
        insignia.getDescripcion(), ahora()));
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

  private static String fecha(LocalDateTime fecha) {
    return fecha != null ? fecha.toString() : null;
  }

  private static CategoriaDonadorEnum categoria(String categoria) {
    return categoria != null ? CategoriaDonadorEnum.valueOf(categoria) : null;
  }
}
