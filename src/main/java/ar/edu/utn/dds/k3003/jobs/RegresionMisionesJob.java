package ar.edu.utn.dds.k3003.jobs;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.clientes.DonacionesClient;
import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.eventos.Evento;
import ar.edu.utn.dds.k3003.eventos.PublicadorEventos;
import ar.edu.utn.dds.k3003.model.incentivos.CambioCategoriaHistorico;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import ar.edu.utn.dds.k3003.repositories.incentivos.CambioCategoriaHistoricoRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.DonadorInsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.MisionHistoricoRepository;
import ar.edu.utn.dds.k3003.observabilidad.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class RegresionMisionesJob {

  private static final Logger logger = LoggerFactory.getLogger(RegresionMisionesJob.class);

  // Tiene que coincidir con el minimo que pide Fachada.requeridoPara para DONACIONES_EXITOSAS.
  private static final int DONACIONES_MINIMAS = 5;

  private final MisionHistoricoRepository misionHistoricoRepository;
  private final DonacionesClient donacionesClient;
  private final DonadoresYEntidadesClient donadoresYEntidadesClient;
  private final DonadorInsigniaRepository donadorInsigniaRepository;
  private final CambioCategoriaHistoricoRepository cambioCategoriaHistoricoRepository;
  private final MetricasNegocio metricas;
  private final PublicadorEventos eventos;
  private final Fachada fachada;

  @Autowired
  public RegresionMisionesJob(
      MisionHistoricoRepository misionHistoricoRepository,
      DonacionesClient donacionesClient,
      DonadoresYEntidadesClient donadoresYEntidadesClient,
      DonadorInsigniaRepository donadorInsigniaRepository,
      CambioCategoriaHistoricoRepository cambioCategoriaHistoricoRepository,
      MetricasNegocio metricas,
      PublicadorEventos eventos,
      Fachada fachada) {
    this.misionHistoricoRepository = misionHistoricoRepository;
    this.donacionesClient = donacionesClient;
    this.donadoresYEntidadesClient = donadoresYEntidadesClient;
    this.donadorInsigniaRepository = donadorInsigniaRepository;
    this.cambioCategoriaHistoricoRepository = cambioCategoriaHistoricoRepository;
    this.metricas = metricas;
    this.eventos = eventos;
    this.fachada = fachada;
  }

  // Cada 5 minutos
  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void revisarRegresiones() {
    long inicio = System.nanoTime();
    String resultado = "ok";
    try (TraceContext.Scope ignored = TraceContext.tarea()) {
      ejecutarRevision();
    } catch (RuntimeException e) {
      resultado = "error";
      throw e;
    } finally {
      metricas.ejecucionJob("regresion_misiones", resultado, System.nanoTime() - inicio);
    }
  }

  private void ejecutarRevision() {
    String jobId = TraceContext.traceId();
    logger.info("[{}] revisarRegresiones - inicio", jobId);

    List<MisionHistorico> completadas = misionHistoricoRepository
        .findByEstadoAndMision_Tipo(
            MisionHistorico.EstadoMision.COMPLETADA,
            TipoMisionEnum.DONACIONES_EXITOSAS);

    logger.info("[{}] revisarRegresiones - {} misiones completadas a revisar", jobId, completadas.size());

    int revertidas = 0;
    for (MisionHistorico hist : completadas) {
      try {
        if (revisarYRevertirSiCorresponde(hist, jobId)) revertidas++;
      } catch (Exception e) {
        // Aislar el fallo de un donador para que no tumbe el resto del batch.
        logger.warn("[{}] revisarRegresiones - fallo procesando historico id={} donador={}: {}",
            jobId, hist.getId(), hist.getDonadorId(), e.getMessage(), e);
      }
    }

    logger.info("[{}] revisarRegresiones - fin, {} misiones revertidas", jobId, revertidas);
  }

  private boolean revisarYRevertirSiCorresponde(MisionHistorico hist, String jobId) {
    Mision mision = hist.getMision();
    UUID donadorUUID = hist.getDonadorId();
    String donadorID = donadorUUID.toString();

    List<DonacionDTO> donaciones = donacionesClient.buscarPorDonadorYFechaInicio(
        donadorID, hist.getFechaInicio().toLocalDate().toString());
    if (donaciones == null) donaciones = List.of();

    long aceptadasAhora = donaciones.stream()
        .filter(d -> d != null && d.estado() == EstadoDonacionEnum.ACEPTADA)
        .count();

    if (aceptadasAhora >= DONACIONES_MINIMAS) return false;

    logger.info("[{}] revisarRegresiones - donador {} bajo de {} aceptadas ({}), revirtiendo mision {}",
        jobId, donadorID, DONACIONES_MINIMAS, aceptadasAhora, mision.getId());

    hist.setEstado(MisionHistorico.EstadoMision.REGRESION);
    misionHistoricoRepository.save(hist);
    metricas.misionesRegresiones.increment();

    if (mision.getInsignia() != null) {
      UUID insigniaUUID = UUID.fromString(mision.getInsignia().getId());
      donadorInsigniaRepository.deleteByDonadorIdAndInsigniaId(donadorUUID, insigniaUUID);
      metricas.insigniasRevocadas.increment();
    }
    fachada.publicarMisionInsigniaDeDonador(donadorUUID);

    String categoriaInicio = mision.getCategoriaInicio();
    if (categoriaInicio != null) {
      try {
        donadoresYEntidadesClient.modificarCategoria(donadorID, categoriaInicio);
        registrarHistorialCategoria(donadorUUID, mision.getCategoriaFin(), categoriaInicio, jobId);
        metricas.categoriaCambiada(mision.getCategoriaFin(), categoriaInicio, "regresion");
        eventos.publicar(Evento.categoriaCambiada(
            donadorID, mision.getCategoriaFin(), categoriaInicio, Evento.MOTIVO_REGRESION));
      } catch (RuntimeException e) {
        logger.warn("[{}] revisarRegresiones - fallo al degradar categoria de donador {} a {}: {}",
            jobId, donadorID, categoriaInicio, e.getMessage(), e);
      }
    }

    return true;
  }

  // Igual que en Fachada: si falla el historial, el cambio de categoria ya quedo hecho.
  private void registrarHistorialCategoria(UUID donadorUUID, String anterior, String nueva, String jobId) {
    try {
      cambioCategoriaHistoricoRepository.save(new CambioCategoriaHistorico(donadorUUID, anterior, nueva));
    } catch (RuntimeException e) {
      logger.warn("[{}] revisarRegresiones - no se pudo registrar historico de categoria de donador {}: {}",
          jobId, donadorUUID, e.getMessage(), e);
    }
  }
}
