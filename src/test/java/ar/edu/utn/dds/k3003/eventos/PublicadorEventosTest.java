package ar.edu.utn.dds.k3003.eventos;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.config.RabbitEventosConfig;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PublicadorEventosTest {

  private static final EventoQueue EVENTO =
      Evento.insignia(AccionEvento.INSIGNIA_CREADA, new InsigniaDTO("id-1", "Nombre", "Desc"));

  private final MeterRegistry registry = new SimpleMeterRegistry();
  private final MetricasNegocio metricas = new MetricasNegocio(registry);
  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

  // Mismo contexto de Rabbit que en produccion, sin la base ni el resto de la aplicacion.
  private ApplicationContextRunner contexto() {
    return new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            RabbitAutoConfiguration.class, JacksonAutoConfiguration.class))
        .withUserConfiguration(RabbitEventosConfig.class, PublicadorEventos.class)
        .withBean(MetricasNegocio.class, () -> metricas);
  }

  private PublicadorEventos publicador(String url, String cola) {
    return new PublicadorEventos(rabbitTemplate, new ObjectMapper()
        .findAndRegisterModules(), metricas, url, cola);
  }

  private double contador(String resultado) {
    return registry.counter("eventos.publicados",
        "tipo", "InsigniaEvento", "resultado", resultado).count();
  }

  @Test
  void sinConfiguracionNoPublicaNada() {
    publicador("", "").publicar(EVENTO);
    publicador("localhost", " ").publicar(EVENTO);
    publicador("", "cola").publicar(EVENTO);

    verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
    assertEquals(0, contador("ok"));
  }

  @Test
  void publicaEnLaColaPorElExchangePorDefecto() {
    MDC.put("traceId", "trace-del-test");
    try {
      EventoQueue conTraza = Evento.misionEliminada("id-1");
      publicador("localhost", "cola").publicar(conTraza);

      ArgumentCaptor<Message> mensaje = ArgumentCaptor.forClass(Message.class);
      verify(rabbitTemplate).send(eq(""), eq("cola"), mensaje.capture());
      var propiedades = mensaje.getValue().getMessageProperties();
      assertEquals("application/json", propiedades.getContentType());
      assertEquals(conTraza.id(), propiedades.getMessageId());
      assertEquals("MisionEvento", propiedades.getType());
      assertEquals("trace-del-test", propiedades.getHeader("X-Trace-Id"));
      assertNull(propiedades.getHeader("__TypeId__"));
      assertEquals(1, registry.counter("eventos.publicados",
          "tipo", "MisionEvento", "resultado", "ok").count());
    } finally {
      MDC.remove("traceId");
    }
  }

  @Test
  void unFalloDelBrokerNoLlegaAlNegocio() {
    doThrow(new AmqpConnectException(new RuntimeException("broker caido")))
        .when(rabbitTemplate).send(anyString(), anyString(), any(Message.class));

    assertDoesNotThrow(() -> publicador("localhost", "cola").publicar(EVENTO));

    assertEquals(1, contador("error"));
    assertEquals(0, contador("ok"));
  }

  @Test
  void conUnBrokerInalcanzableNoFallaNiTraba() {
    CachingConnectionFactory conexion = new CachingConnectionFactory("localhost", 1);
    conexion.setConnectionTimeout(3000);
    PublicadorEventos real = new PublicadorEventos(new RabbitTemplate(conexion),
        new ObjectMapper().findAndRegisterModules(), metricas, "localhost", "cola");

    assertTimeout(Duration.ofSeconds(10), () -> real.publicar(EVENTO));

    assertEquals(1, contador("error"));
    conexion.destroy();
  }

  @Test
  void laAplicacionLevantaSinRabbitConfigurado() {
    contexto()
        .withPropertyValues("spring.rabbitmq.host=", "spring.rabbitmq.username=", "spring.rabbitmq.password=",
            "spring.rabbitmq.virtual-host=", "eventos.rabbit.cola=")
        .run(ctx -> {
          assertNull(ctx.getStartupFailure());
          assertDoesNotThrow(() -> ctx.getBean(PublicadorEventos.class).publicar(EVENTO));
          assertEquals(0, contador("error"));
        });
  }

  @Test
  void usaHostUsuarioYVhostConfigurados() {
    contexto()
        .withPropertyValues(
            "spring.rabbitmq.host=broker.example.com",
            "spring.rabbitmq.username=usuario",
            "spring.rabbitmq.password=clave",
            "spring.rabbitmq.virtual-host=usuario",
            "eventos.rabbit.cola=cola")
        .run(ctx -> {
          assertNull(ctx.getStartupFailure());
          var conexion = ctx.getBean(CachingConnectionFactory.class);
          assertEquals("usuario", conexion.getUsername());
          assertEquals("broker.example.com", conexion.getHost());
          assertEquals("usuario", conexion.getVirtualHost());
        });
  }

  // Si un evento no se puede leer, EventoWorker lo devuelve a la cola para siempre.
  @Test
  void elWorkerPuedeLeerTodosLosEventos() {
    UUID donador = UUID.randomUUID();
    Insignia insignia = new Insignia(UUID.randomUUID().toString(), "I", "D");
    Mision mision = new Mision(UUID.randomUUID().toString(), "M", null, "OCASIONAL", null,
        TipoMisionEnum.COMPLETITUD);
    MisionHistorico historico =
        new MisionHistorico(donador, mision, MisionHistorico.EstadoMision.ACTIVA);
    historico.setId(1L);
    MisionDTO misionDto = new MisionDTO("id", "M", null, CategoriaDonadorEnum.OCASIONAL, null,
        TipoMisionEnum.COMPLETITUD);
    Map<EventoQueue, Class<?>> eventos = Map.of(
        EVENTO, InsigniaEvento.class,
        Evento.insigniaEliminada("id"), InsigniaEvento.class,
        Evento.mision(AccionEvento.MISION_CREADA, misionDto), MisionEvento.class,
        Evento.misionEliminada("id"), MisionEvento.class,
        Evento.misionDeDonador(AccionEvento.MISION_ASIGNADA, historico), MisionDonadorEvento.class,
        Evento.insigniaDeDonador(AccionEvento.INSIGNIA_ASIGNADA, donador, insignia),
        InsigniaDonadorEvento.class,
        Evento.categoriaCambiada("id", null, "COLABORADOR", Evento.MOTIVO_PROGRESO),
        CategoriaDonadorEvento.class);

    contexto().run(ctx -> {
      ObjectMapper publicacion = ctx.getBean(ObjectMapper.class);
      ObjectMapper worker = new ObjectMapper();
      for (var evento : eventos.entrySet()) {
        byte[] cuerpo = publicacion.writeValueAsBytes(evento.getKey());
        EventoQueue leido = worker.readValue(cuerpo, EventoQueue.class);
        assertEquals(evento.getValue().getSimpleName(), leido.tipoEvento());
        assertEquals(evento.getKey().descripcion(),
            worker.convertValue(leido.descripcion(), evento.getValue()));
      }
    });
  }

  @Test
  void elEventoLlegaALaColaDeclaradaConElContratoDocumentado() {
    contexto()
        .withBean(ConnectionFactory.class,
            () -> new CachingConnectionFactory(new MockConnectionFactory()))
        .withPropertyValues(
            "spring.rabbitmq.host=en-memoria", "eventos.rabbit.cola=cola-test")
        .run(ctx -> {
          ctx.getBean(PublicadorEventos.class).publicar(EVENTO);

          Message recibido = ctx.getBean(RabbitTemplate.class).receive("cola-test", 2000);

          assertNotNull(recibido);
          // Igual que EventoWorker: ObjectMapper pelado, que rechaza campos desconocidos.
          ObjectMapper worker = new ObjectMapper();
          EventoQueue leido = worker.readValue(recibido.getBody(), EventoQueue.class);
          assertEquals(EVENTO.id(), leido.id());
          assertEquals("InsigniaEvento", leido.tipoEvento());
          InsigniaEvento dto = worker.convertValue(leido.descripcion(), InsigniaEvento.class);
          assertEquals("INSIGNIA_CREADA", dto.accion());
          assertEquals("id-1", dto.id());
          assertEquals("Nombre", dto.nombre());
          assertNotNull(java.time.Instant.parse(dto.ocurridoEn()));
          assertEquals(1, contador("ok"));
        });
  }
}
