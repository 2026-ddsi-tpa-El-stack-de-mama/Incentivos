package ar.edu.utn.dds.k3003.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricasNegocio {

  // Insignias
  public final Counter insigniasCreadas;
  public final Counter insigniasAsignadas;
  public final Counter insigniasEliminadas;

  // Misiones
  public final Counter misionesCreadas;
  public final Counter misionesAsignadas;
  public final Counter donadoresProcesados;

  // Errores de negocio
  public final Counter errores400;
  public final Counter errores404;
  public final Counter errores500;

  public MetricasNegocio(MeterRegistry registry) {
    this.insigniasCreadas = Counter.builder("insignias.creadas")
        .description("Insignias creadas").register(registry);
    this.insigniasAsignadas = Counter.builder("insignias.asignadas")
        .description("Insignias asignadas a donadores").register(registry);
    this.insigniasEliminadas = Counter.builder("insignias.eliminadas")
        .description("Insignias eliminadas").register(registry);

    this.misionesCreadas = Counter.builder("misiones.creadas")
        .description("Misiones creadas").register(registry);
    this.misionesAsignadas = Counter.builder("misiones.asignadas")
        .description("Misiones asignadas a donadores").register(registry);
    this.donadoresProcesados = Counter.builder("donadores.procesados")
        .description("Donadores procesados").register(registry);

    this.errores400 = Counter.builder("errores.400")
        .description("Errores 400").register(registry);

    this.errores404 = Counter.builder("errores.404")
      .description("Errores 404").register(registry);

  this.errores500 = Counter.builder("errores.500")
        .description("Errores 500").register(registry);
  }
}