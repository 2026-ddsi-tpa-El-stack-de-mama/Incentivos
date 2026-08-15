package ar.edu.utn.dds.k3003.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MantenimientoMisionesJob {

  private final RegresionMisionesJob regresionJob;
  private final ProcesarDonadoresJob procesarJob;

  @Autowired
  public MantenimientoMisionesJob(RegresionMisionesJob regresionJob, ProcesarDonadoresJob procesarJob) {
    this.regresionJob = regresionJob;
    this.procesarJob = procesarJob;
  }

  @Scheduled(fixedDelay = 30 * 60 * 1000)
  public void ejecutar() {
    regresionJob.revisarRegresiones(); // primero: revertir lo que corresponda
    procesarJob.procesarTodos();       // despues: evaluar progreso con el estado ya corregido
  }
}