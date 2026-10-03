package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MetricasNegocio {

  private final MeterRegistry registry;

  // Insignias
  public final Counter insigniasCreadas;
  public final Counter insigniasAsignadas;
  public final Counter insigniasEliminadas;
  public final Counter insigniasRevocadas;

  // Misiones
  public final Counter misionesCreadas;
  public final Counter misionesAsignadas;
  public final Counter misionesRegresiones;

  // Procesamiento de donadores (API + job)
  /** Procesamientos exitosos en los que el donador tuvo algun cambio. */
  public final Counter donadoresProcesados;
  /** Procesamientos exitosos (con o sin cambios). */
  public final Counter donadoresEvaluados;
  /** Procesamientos que terminaron en excepcion. */
  public final Counter donadoresProcesamientoFallido;

  // Errores de negocio
  public final Counter errores400;
  public final Counter errores404;
  public final Counter errores500;

  public MetricasNegocio(MeterRegistry registry) {
    this.registry = registry;

    this.insigniasCreadas = Counter.builder("insignias.creadas")
        .description("Insignias creadas").register(registry);
    this.insigniasAsignadas = Counter.builder("insignias.asignadas")
        .description("Insignias asignadas a donadores").register(registry);
    this.insigniasEliminadas = Counter.builder("insignias.eliminadas")
        .description("Insignias eliminadas").register(registry);
    this.insigniasRevocadas = Counter.builder("insignias.revocadas")
        .description("Insignias quitadas a un donador por regresion de mision").register(registry);

    this.misionesCreadas = Counter.builder("misiones.creadas")
        .description("Misiones creadas").register(registry);
    this.misionesAsignadas = Counter.builder("misiones.asignadas")
        .description("Misiones asignadas a donadores").register(registry);
    this.misionesRegresiones = Counter.builder("misiones.regresiones")
        .description("Misiones completadas revertidas porque el donador bajo del umbral")
        .register(registry);

    this.donadoresProcesados = Counter.builder("donadores.procesados")
        .description("Donadores procesados con exito y con algun cambio (API y job)")
        .register(registry);
    this.donadoresEvaluados = Counter.builder("donadores.evaluados")
        .description("Donadores procesados sin error, hayan tenido cambios o no (API y job)")
        .register(registry);
    this.donadoresProcesamientoFallido = Counter.builder("donadores.procesamiento.fallidos")
        .description("Procesamientos de donador que terminaron en error (API y job)")
        .register(registry);

    this.errores400 = Counter.builder("errores.400")
        .description("Errores 400").register(registry);
    this.errores404 = Counter.builder("errores.404")
        .description("Errores 404").register(registry);
    this.errores500 = Counter.builder("errores.500")
        .description("Errores 500").register(registry);
  }

  /** Mision completada por un donador, por tipo de mision. */
  public void misionCompletada(String tipo) {
    registry.counter("misiones.completadas", "tipo", valorTag(tipo)).increment();
  }

  /** Cambio de categoria de un donador. motivo: "progreso" o "regresion". */
  public void categoriaCambiada(String desde, String hacia, String motivo) {
    registry.counter("donadores.categoria.cambios",
        "desde", valorTag(desde), "hacia", valorTag(hacia), "motivo", motivo).increment();
  }

  /** Registra una ejecucion de job (heartbeat) y su duracion. resultado: "ok" o "error". */
  public void ejecucionJob(String job, String resultado, long nanos) {
    registry.counter("jobs.ejecuciones", "job", job, "resultado", resultado).increment();
    Timer.builder("jobs.duracion").tag("job", job).register(registry)
        .record(nanos, java.util.concurrent.TimeUnit.NANOSECONDS);
  }

  private static String valorTag(String v) {
    return v == null || v.isBlank() ? "desconocido" : v;
  }
}
