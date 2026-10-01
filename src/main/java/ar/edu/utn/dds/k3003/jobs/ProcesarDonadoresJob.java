package ar.edu.utn.dds.k3003.jobs;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.model.incentivos.DonadorMision;
import ar.edu.utn.dds.k3003.repositories.incentivos.DonadorMisionRepository;
import ar.edu.utn.dds.k3003.observabilidad.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProcesarDonadoresJob {

  private static final Logger logger = LoggerFactory.getLogger(ProcesarDonadoresJob.class);

  private final Fachada fachada;
  private final DonadorMisionRepository donadorMisionRepository;
  private final DonadoresYEntidadesClient donadoresYEntidadesClient;

  @Autowired
  public ProcesarDonadoresJob(Fachada fachada, DonadorMisionRepository donadorMisionRepository,
                              DonadoresYEntidadesClient donadoresYEntidadesClient) {
    this.fachada = fachada;
    this.donadorMisionRepository = donadorMisionRepository;
    this.donadoresYEntidadesClient = donadoresYEntidadesClient;
  }

  //5 min
  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void procesarTodos() {
    // Sin request entrante: la ejecucion del job es el origen de la traza (o hereda la del que lo llama).
    try (TraceContext.Scope ignored = TraceContext.tarea()) {
      ejecutarProcesamiento();
    }
  }

  private void ejecutarProcesamiento() {
    String jobId = TraceContext.traceId();

    Set<UUID> todosLosDonadores = donadoresYEntidadesClient.obtenerDonadores().stream()
        .map(d -> UUID.fromString(d.id()))
        .collect(Collectors.toSet());

    logger.info("[{}] procesarTodos - {} donadores a procesar", jobId, todosLosDonadores.size());

    int errores = 0;
    for (UUID donadorUUID : todosLosDonadores) {
      try {
        fachada.procesarDonador(donadorUUID.toString());
      } catch (Exception e) {
        errores++;
        logger.warn("[{}] procesarTodos - fallo procesando donador {}: {}",
            jobId, donadorUUID, e.getMessage(), e);
      }
    }
    logger.info("[{}] procesarTodos - fin, {} errores de {} donadores", jobId, errores, todosLosDonadores.size());
  }
}