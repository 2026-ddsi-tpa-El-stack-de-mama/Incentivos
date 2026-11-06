package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.observabilidad.TraceContext;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publica eventos en la cola del modelo de lectura.
 *
 * <p>Es best-effort: la operacion de negocio ya quedo guardada cuando se llama a {@link #publicar},
 * asi que un problema con el broker se loguea y se cuenta, pero nunca se propaga al llamador.
 *
 * <p>Se serializa a mano (sin Jackson2JsonMessageConverter) para no mandar el header
 * {@code __TypeId__}: el consumidor vive en otro repo y no tiene estas clases.
 */
@Component
public class PublicadorEventos {

  private static final Logger logger = LoggerFactory.getLogger(PublicadorEventos.class);

  // Exchange por defecto de Rabbit: enruta directo a la cola cuyo nombre es la routing key.
  private static final String EXCHANGE_POR_DEFECTO = "";

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final MetricasNegocio metricas;
  private final String cola;
  private final boolean habilitado;

  public PublicadorEventos(
      RabbitTemplate rabbitTemplate,
      ObjectMapper objectMapper,
      MetricasNegocio metricas,
      @Value("${spring.rabbitmq.host:}") String host,
      @Value("${eventos.rabbit.cola:}") String cola) {
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
    this.metricas = metricas;
    this.cola = cola;
    this.habilitado = !host.isBlank() && !cola.isBlank();
    if (!habilitado) {
      logger.warn("Publicacion de eventos deshabilitada: faltan E_QUEUE_HOST y/o E_QUEUE_NAME");
    }
  }

  public void publicar(EventoQueue evento) {
    if (!habilitado) {
      logger.debug("Evento {} descartado: publicacion deshabilitada", evento.tipoEvento());
      return;
    }
    try {
      rabbitTemplate.send(EXCHANGE_POR_DEFECTO, cola, aMensaje(evento));
      metricas.eventoPublicado(evento.tipoEvento(), "ok");
      logger.info("Evento publicado: tipo={} id={}", evento.tipoEvento(), evento.id());
    } catch (JsonProcessingException | RuntimeException e) {
      metricas.eventoPublicado(evento.tipoEvento(), "error");
      logger.warn("No se pudo publicar evento: tipo={} id={}: {}",
          evento.tipoEvento(), evento.id(), e.getMessage(), e);
    }
  }

  private Message aMensaje(EventoQueue evento) throws JsonProcessingException {
    MessageBuilderSupport<Message> mensaje = MessageBuilder.withBody(objectMapper.writeValueAsBytes(evento))
        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
        .setContentEncoding("UTF-8")
        .setMessageId(evento.id())
        .setType(evento.tipoEvento());
    String traceId = TraceContext.traceId();
    if (traceId != null) {
      mensaje.setHeader(TraceContext.TRACE_HEADER, traceId);
    }
    return mensaje.build();
  }
}
