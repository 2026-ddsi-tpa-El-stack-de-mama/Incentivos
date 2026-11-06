package ar.edu.utn.dds.k3003.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.clientes.DonacionesClient;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.jobs.RegresionMisionesJob;
import ar.edu.utn.dds.k3003.model.incentivos.DonadorMision;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import ar.edu.utn.dds.k3003.repositories.incentivos.CambioCategoriaHistoricoRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.DonadorInsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.DonadorMisionRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.MisionHistoricoRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.MisionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifica que cada cambio de estado publique su evento y solo cuando el cambio ocurrio. */
@ExtendWith(MockitoExtension.class)
class EventosDeNegocioTest {

  private static final UUID DONADOR = UUID.randomUUID();
  private static final String DONADOR_ID = DONADOR.toString();

  @Mock InsigniaRepository insigniaRepository;
  @Mock MisionRepository misionRepository;
  @Mock DonadorInsigniaRepository donadorInsigniaRepository;
  @Mock DonadorMisionRepository donadorMisionRepository;
  @Mock MisionHistoricoRepository misionHistoricoRepository;
  @Mock CambioCategoriaHistoricoRepository cambioCategoriaRepository;
  @Mock DonadoresYEntidadesClient donadoresClient;
  @Mock DonacionesClient donacionesClient;
  @Mock PublicadorEventos eventos;
  @Captor ArgumentCaptor<EventoQueue> publicados;

  private final MetricasNegocio metricas = new MetricasNegocio(new SimpleMeterRegistry());
  private Fachada fachada;
  private Insignia insignia;
  private Mision mision;
  private Mision siguiente;

  @BeforeEach
  void setUp() {
    fachada = new Fachada(insigniaRepository, misionRepository, donadorInsigniaRepository,
        donadorMisionRepository, misionHistoricoRepository, donadoresClient,
        cambioCategoriaRepository, donacionesClient, metricas, eventos);

    insignia = new Insignia(UUID.randomUUID().toString(), "Insignia", "Desc");
    mision = mision("Exitosas", "OCASIONAL", "COLABORADOR");
    mision.setInsignia(insignia);
    siguiente = mision("Siguiente", "COLABORADOR", "REVOLUCIONARIO");

    lenient().when(insigniaRepository.save(any())).thenAnswer(returnsFirstArg());
    lenient().when(misionRepository.save(any())).thenAnswer(returnsFirstArg());
    lenient().when(misionHistoricoRepository.save(any())).thenAnswer(inv -> {
      MisionHistorico historico = inv.getArgument(0);
      if (historico.getId() == null) historico.setId(77L);
      return historico;
    });
  }

  private static Mision mision(String nombre, String desde, String hasta) {
    return new Mision(UUID.randomUUID().toString(), nombre, null, desde, hasta,
        TipoMisionEnum.DONACIONES_EXITOSAS);
  }

  private static DonadorDTO donador(String categoria) {
    return new DonadorDTO(DONADOR_ID, "n", "a", 30, "e@t", "1", "d",
        EstadoDonadorEnum.VERIFICADO, categoria);
  }

  private static List<DonacionDTO> aceptadas(int cantidad) {
    return new ArrayList<>(IntStream.range(0, cantidad)
        .mapToObj(i -> new DonacionDTO("d" + i, DONADOR_ID, "dep", "x", "p", 1, EstadoDonacionEnum.ACEPTADA))
        .toList());
  }

  private static AccionEvento accion(EventoQueue evento) {
    return AccionEvento.valueOf(new ObjectMapper().valueToTree(evento.descripcion()).get("accion").asText());
  }

  private List<AccionEvento> tiposPublicados(int cantidad) {
    verify(eventos, times(cantidad)).publicar(publicados.capture());
    return publicados.getAllValues().stream().map(EventosDeNegocioTest::accion).toList();
  }

  private EventoQueue unicoEvento() {
    verify(eventos).publicar(publicados.capture());
    return publicados.getValue();
  }

  @Test
  void altaBajaYModificacionDeCatalogo() {
    InsigniaDTO creada = fachada.agregarInsignia(new InsigniaDTO(insignia.getId(), "A", "B"));
    when(insigniaRepository.existsById(any())).thenReturn(true);
    fachada.modificarInsignia(creada);
    fachada.eliminarInsignia(creada.id());

    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    MisionDTO nueva = new MisionDTO(null, "M", insignia.getId(),
        CategoriaDonadorEnum.OCASIONAL, CategoriaDonadorEnum.COLABORADOR,
        TipoMisionEnum.COMPLETITUD);
    fachada.agregarMision(nueva);
    when(misionRepository.existsById(any())).thenReturn(true);
    fachada.modificarMision(IncentivosDto.conId(nueva, mision.getId()));
    fachada.eliminarMision(mision.getId());

    assertEquals(List.of(
        AccionEvento.INSIGNIA_CREADA, AccionEvento.INSIGNIA_MODIFICADA, AccionEvento.INSIGNIA_ELIMINADA,
        AccionEvento.MISION_CREADA, AccionEvento.MISION_MODIFICADA, AccionEvento.MISION_ELIMINADA),
        tiposPublicados(6));
  }

  @Test
  void siFallaElGuardadoNoSePublica() {
    when(insigniaRepository.save(any())).thenThrow(new RuntimeException("db caida"));

    assertThrows(RuntimeException.class,
        () -> fachada.agregarInsignia(new InsigniaDTO(null, "A", "B")));

    verify(eventos, never()).publicar(any());
  }

  @Test
  void asignarMisionPublicaElHistoricoActivo() {
    when(misionRepository.findById(any())).thenReturn(Optional.of(mision));

    fachada.asignarMisionADonador(DONADOR_ID, new MisionDTO(mision.getId(), null, null, null, null, null));

    EventoQueue evento = unicoEvento();
    assertEquals("MisionDonadorEvento", evento.tipoEvento());
    MisionDonadorEvento payload = (MisionDonadorEvento) evento.descripcion();
    assertEquals("MISION_ASIGNADA", payload.accion());
    assertEquals(DONADOR_ID, payload.donadorId());
    assertEquals(CategoriaDonadorEnum.COLABORADOR, payload.categoriaFin());
    assertEquals(77L, payload.historicoId());
    assertEquals("ACTIVA", payload.estado());
    assertEquals(mision.getId(), payload.misionId());
    assertNotNull(payload.fechaInicio());
  }

  @Test
  void insigniaQueElDonadorYaTeniaNoPublica() {
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(donadorInsigniaRepository.existsByDonadorIdAndInsigniaId(any(), any())).thenReturn(true);

    assertEquals(false, fachada.asignarInsigniaADonador(DONADOR_ID,
        new InsigniaDTO(insignia.getId(), "A", "B")));

    verify(eventos, never()).publicar(any());
  }

  @Test
  void donadorSinMisionActivaPublicaLaAsignacion() {
    when(donadoresClient.obtenerDonador(DONADOR_ID)).thenReturn(donador("OCASIONAL"));
    when(donadorMisionRepository.findByDonadorId(DONADOR)).thenReturn(Optional.empty());
    when(misionRepository.findAll()).thenReturn(List.of(mision));

    fachada.procesarDonador(DONADOR_ID);

    assertEquals(AccionEvento.MISION_ASIGNADA, accion(unicoEvento()));
  }

  @Test
  void misionCompletadaPublicaTodaLaSecuenciaEnOrden() {
    MisionHistorico activa = new MisionHistorico(DONADOR, mision, MisionHistorico.EstadoMision.ACTIVA);
    activa.setId(5L);
    when(donadoresClient.obtenerDonador(DONADOR_ID))
        .thenReturn(donador("OCASIONAL"), donador("OCASIONAL"), donador("OCASIONAL"), donador("COLABORADOR"));
    when(donadorMisionRepository.findByDonadorId(DONADOR))
        .thenReturn(Optional.of(new DonadorMision(DONADOR, mision)));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString())).thenReturn(aceptadas(5));
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(misionHistoricoRepository.findByDonadorId(DONADOR)).thenReturn(List.of(activa));
    when(misionRepository.findAll()).thenReturn(List.of(mision, siguiente));
    when(misionRepository.findById(any())).thenReturn(Optional.of(mision));

    fachada.procesarDonador(DONADOR_ID);

    assertEquals(List.of(
        AccionEvento.INSIGNIA_ASIGNADA, AccionEvento.CATEGORIA_CAMBIADA,
        AccionEvento.MISION_COMPLETADA, AccionEvento.MISION_ASIGNADA), tiposPublicados(4));

    List<EventoQueue> todos = publicados.getAllValues();
    var categoria = (CategoriaDonadorEvento) todos.get(1).descripcion();
    assertEquals("OCASIONAL", categoria.categoriaAnterior());
    assertEquals("COLABORADOR", categoria.categoriaNueva());
    assertEquals(Evento.MOTIVO_PROGRESO, categoria.motivo());
    var completada = (MisionDonadorEvento) todos.get(2).descripcion();
    assertEquals(5L, completada.historicoId());
    assertEquals("COMPLETADA", completada.estado());
    assertNotNull(completada.fechaFin());
    assertEquals(siguiente.getId(), ((MisionDonadorEvento) todos.get(3).descripcion()).misionId());
  }

  @Test
  void siNoSeCumplioLaMisionNoHayEventos() {
    when(donadoresClient.obtenerDonador(DONADOR_ID)).thenReturn(donador("OCASIONAL"));
    when(donadorMisionRepository.findByDonadorId(DONADOR))
        .thenReturn(Optional.of(new DonadorMision(DONADOR, mision)));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString())).thenReturn(aceptadas(1));

    fachada.procesarDonador(DONADOR_ID);

    verify(eventos, never()).publicar(any());
  }

  @Test
  void siFallaElCambioDeCategoriaNoSePublica() {
    when(donadoresClient.obtenerDonador(DONADOR_ID)).thenReturn(donador("OCASIONAL"));
    doThrow(new RuntimeException("donadores caido"))
        .when(donadoresClient).modificarCategoria(anyString(), anyString());

    assertThrows(RuntimeException.class,
        () -> fachada.registrarCambioCategoriaEnDonador(DONADOR_ID, "COLABORADOR"));

    verify(eventos, never()).publicar(any());
  }

  @Test
  void regresionPublicaReversionInsigniaYCategoria() {
    MisionHistorico completada =
        new MisionHistorico(DONADOR, mision, MisionHistorico.EstadoMision.COMPLETADA);
    completada.setId(9L);
    when(misionHistoricoRepository.findByEstadoAndMision_Tipo(any(), any()))
        .thenReturn(List.of(completada));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString())).thenReturn(aceptadas(1));

    new RegresionMisionesJob(misionHistoricoRepository, donacionesClient, donadoresClient,
        donadorInsigniaRepository, metricas, eventos).revisarRegresiones();

    assertEquals(List.of(
        AccionEvento.MISION_REVERTIDA, AccionEvento.INSIGNIA_REVOCADA, AccionEvento.CATEGORIA_CAMBIADA),
        tiposPublicados(3));
    List<EventoQueue> todos = publicados.getAllValues();
    assertEquals("REGRESION", ((MisionDonadorEvento) todos.get(0).descripcion()).estado());
    assertEquals(insignia.getId(), ((InsigniaDonadorEvento) todos.get(1).descripcion()).insigniaId());
    var categoria = (CategoriaDonadorEvento) todos.get(2).descripcion();
    assertEquals("OCASIONAL", categoria.categoriaNueva());
    assertEquals(Evento.MOTIVO_REGRESION, categoria.motivo());
    assertEquals(DONADOR_ID, categoria.donadorId());
  }

  private static final class IncentivosDto {
    static MisionDTO conId(MisionDTO dto, String id) {
      return new MisionDTO(id, dto.nombre(), dto.insigniaID(), dto.categoriaInicio(),
          dto.categoriaFin(), dto.tipo());
    }
  }
}
