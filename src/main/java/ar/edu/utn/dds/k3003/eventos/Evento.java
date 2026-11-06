package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import ar.edu.utn.dds.k3003.observabilidad.TraceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sobre comun de los eventos que Incentivos publica para el modelo de lectura.
 *
 * <p>Los eventos llevan el estado resultante, no el delta: asi el consumidor puede hacer upsert por
 * id ignorando lo mas viejo que {@code ocurridoEn}, aunque lleguen repetidos o desordenados. Contrato
 * completo en docs/eventos_incentivos.md.
 */
public record Evento(
    String eventoId,
    TipoEvento tipo,
    String origen,
    String entidadId,
    Instant ocurridoEn,
    String traceId,
    Object payload) {

  public static final String ORIGEN = "incentivos";

  /** Motivos de {@link TipoEvento#CATEGORIA_CAMBIADA}. Mismos valores que la metrica de categoria. */
  public static final String MOTIVO_PROGRESO = "progreso";
  public static final String MOTIVO_REGRESION = "regresion";

  public record EntidadEliminada(String id) {}

  public record MisionDeDonador(
      Long historicoId,
      String donadorId,
      String misionId,
      String nombre,
      TipoMisionEnum tipo,
      String categoriaInicio,
      String categoriaFin,
      String estado,
      LocalDateTime fechaInicio,
      LocalDateTime fechaFin) {}

  public record InsigniaDeDonador(
      String donadorId, String insigniaId, String nombre, String descripcion) {}

  public record CategoriaDeDonador(
      String donadorId, String categoriaAnterior, String categoriaNueva, String motivo) {}

  public static Evento insignia(TipoEvento tipo, InsigniaDTO insignia) {
    return nuevo(tipo, insignia.id(), insignia);
  }

  public static Evento mision(TipoEvento tipo, MisionDTO mision) {
    return nuevo(tipo, mision.id(), mision);
  }

  public static Evento eliminacion(TipoEvento tipo, String id) {
    return nuevo(tipo, id, new EntidadEliminada(id));
  }

  public static Evento misionDeDonador(TipoEvento tipo, MisionHistorico historico) {
    Mision mision = historico.getMision();
    String donadorId = historico.getDonadorId().toString();
    return nuevo(tipo, donadorId, new MisionDeDonador(
        historico.getId(),
        donadorId,
        mision.getId(),
        mision.getNombre(),
        mision.getTipo(),
        mision.getCategoriaInicio(),
        mision.getCategoriaFin(),
        historico.getEstado().name(),
        historico.getFechaInicio(),
        historico.getFechaFin()));
  }

  public static Evento insigniaDeDonador(TipoEvento tipo, UUID donadorId, Insignia insignia) {
    String id = donadorId.toString();
    return nuevo(tipo, id, new InsigniaDeDonador(
        id, insignia.getId(), insignia.getNombre(), insignia.getDescripcion()));
  }

  public static Evento categoriaCambiada(
      String donadorId, String categoriaAnterior, String categoriaNueva, String motivo) {
    return nuevo(TipoEvento.CATEGORIA_CAMBIADA, donadorId,
        new CategoriaDeDonador(donadorId, categoriaAnterior, categoriaNueva, motivo));
  }

  private static Evento nuevo(TipoEvento tipo, String entidadId, Object payload) {
    return new Evento(
        UUID.randomUUID().toString(), tipo, ORIGEN, entidadId, Instant.now(),
        TraceContext.traceId(), payload);
  }
}
