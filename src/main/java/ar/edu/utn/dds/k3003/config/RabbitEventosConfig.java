package ar.edu.utn.dds.k3003.config;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitEventosConfig {

  /**
   * Declara la cola al abrir la conexion, para que los eventos no se pierdan si el consumidor
   * todavia no la creo. Si el consumidor tambien la declara, debe usar los mismos atributos: durable,
   * no exclusiva y sin auto-delete. Sin cola configurada no se declara nada.
   */
  @Bean
  public Declarables colaEventos(@Value("${eventos.rabbit.cola:}") String cola) {
    return cola.isBlank() ? new Declarables() : new Declarables(new Queue(cola, true));
  }
}
