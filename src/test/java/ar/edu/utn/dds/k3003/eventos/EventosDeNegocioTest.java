package ar.edu.utn.dds.k3003.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.clientes.DonacionesClient;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.dtos.ResultadoProcesamiento;
import ar.edu.utn.dds.k3003.jobs.RegresionMisionesJob;
import ar.edu.utn.dds.k3003.model.incentivos.CambioCategoriaHistorico;
import ar.edu.utn.dds.k3003.model.incentivos.DonadorInsignia;
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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
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

  private List<String> tiposPublicados(int cantidad) {
    verify(eventos, times(cantidad)).publicar(publicados.capture());
    return publicados.getAllValues().stream().map(EventoQueue::tipoEvento).toList();
  }

  private MisionInsigniaEvento foto(EventoQueue evento) {
    assertEquals("MisionInsigniaEvento", evento.tipoEvento());
    return (MisionInsigniaEvento) evento.descripcion();
  }

  private EventoQueue unicoEvento() {
    verify(eventos).publicar(publicados.capture());
    return publicados.getValue();
  }

  @Test
  void crearEnElCatalogoNoPublica() {
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));

    fachada.agregarInsignia(new InsigniaDTO(insignia.getId(), "A", "B"));
    fachada.agregarMision(misionDto(null));

    verify(eventos, never()).publicar(any());
  }

  @Test
  void modificarInsigniaPublicaUnaFotoPorDonadorAfectado() {
    UUID otro = UUID.randomUUID();
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(donadorInsigniaRepository.findDonadorIdsByInsigniaId(any())).thenReturn(List.of(DONADOR, otro));

    fachada.modificarInsignia(new InsigniaDTO(insignia.getId(), "A", "B"));

    tiposPublicados(2);
    assertEquals(List.of(DONADOR_ID, otro.toString()),
        publicados.getAllValues().stream().map(e -> foto(e).donadorId()).toList());
  }

  @Test
  void eliminarInsigniaLaDejaFueraDeServicioYLeDaOtraALaMision() {
    Insignia usada = new Insignia(UUID.randomUUID().toString(), "Usada", "Desc");
    Insignia libre = new Insignia(UUID.randomUUID().toString(), "Libre", "Desc");
    UUID enCurso = UUID.randomUUID();
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(insigniaRepository.findAll()).thenReturn(List.of(insignia, usada, libre));
    when(misionRepository.findByInsignia_Id(any())).thenReturn(List.of(mision));
    when(misionRepository.existsByInsignia_Id(UUID.fromString(usada.getId()))).thenReturn(true);
    when(donadorInsigniaRepository.findDonadorIdsByInsigniaId(any())).thenReturn(List.of(DONADOR));
    when(donadorMisionRepository.findDonadorIdsByMisionId(any())).thenReturn(List.of(DONADOR, enCurso));

    fachada.eliminarInsignia(insignia.getId());

    InOrder orden = inOrder(donadorInsigniaRepository, eventos);
    orden.verify(donadorInsigniaRepository).deleteByInsigniaId(any());
    orden.verify(eventos, times(2)).publicar(publicados.capture());
    assertEquals(List.of(DONADOR_ID, enCurso.toString()),
        publicados.getAllValues().stream().map(e -> foto(e).donadorId()).toList());
    verify(insigniaRepository, never()).deleteById(any());
    assertEquals("insignia fuera de servicio", insignia.getDescripcion());
    assertEquals(libre.getId(), mision.getInsignia().getId());
  }

  @Test
  void sinReemplazoNoSeTocaNiLaInsigniaNiLaMision() {
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(insigniaRepository.findAll()).thenReturn(List.of(insignia));
    when(misionRepository.findByInsignia_Id(any())).thenReturn(List.of(mision));

    assertThrows(IllegalStateException.class, () -> fachada.eliminarInsignia(insignia.getId()));

    assertEquals("Desc", insignia.getDescripcion());
    assertEquals(insignia, mision.getInsignia());
    verify(donadorInsigniaRepository, never()).deleteByInsigniaId(any());
    verify(eventos, never()).publicar(any());
  }

  @Test
  void unaInsigniaSinMisionesNoNecesitaReemplazo() {
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));

    fachada.eliminarInsignia(insignia.getId());

    assertEquals(true, insignia.isFueraDeServicio());
  }

  @Test
  void unaInsigniaFueraDeServicioNoSeOtorgaNiSeAsociaAMisiones() {
    insignia.setDescripcion(Insignia.DESCRIPCION_FUERA_DE_SERVICIO);
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));

    assertThrows(IllegalStateException.class, () -> fachada.asignarInsigniaADonador(DONADOR_ID,
        new InsigniaDTO(insignia.getId(), "A", "B")));
    assertThrows(IllegalStateException.class, () -> fachada.agregarMision(misionDto(null)));

    verify(donadorInsigniaRepository, never()).save(any());
    verify(misionRepository, never()).save(any());
  }

  @Test
  void modificarUnaInsigniaFueraDeServicioNoLaReactiva() {
    insignia.setDescripcion(Insignia.DESCRIPCION_FUERA_DE_SERVICIO);
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));

    InsigniaDTO modificada = fachada.modificarInsignia(new InsigniaDTO(insignia.getId(), "Otro", "Nueva"));

    assertEquals("Otro", modificada.nombre());
    assertEquals(Insignia.DESCRIPCION_FUERA_DE_SERVICIO, modificada.descripcion());
  }

  @Test
  void modificarMisionPublicaLaFotoDeQuienesLaTienenEnCurso() {
    when(misionRepository.findConInsigniaById(any())).thenReturn(Optional.of(mision));
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(donadorMisionRepository.findDonadorIdsByMisionId(any())).thenReturn(List.of(DONADOR));
    when(donadorMisionRepository.findByDonadorId(DONADOR))
        .thenReturn(Optional.of(new DonadorMision(DONADOR, mision)));

    fachada.modificarMision(misionDto(mision.getId()));

    assertEquals(mision.getId(), foto(unicoEvento()).misiones().get(0).id());
  }

  @Test
  void modificarMisionSinInsigniaConservaLaActual() {
    when(misionRepository.findConInsigniaById(any())).thenReturn(Optional.of(mision));

    MisionDTO modificada = fachada.modificarMision(new MisionDTO(mision.getId(), "Otro nombre", null,
        CategoriaDonadorEnum.OCASIONAL, CategoriaDonadorEnum.COLABORADOR, TipoMisionEnum.COMPLETITUD));

    assertEquals("Otro nombre", modificada.nombre());
    assertEquals(insignia.getId(), modificada.insigniaID());
  }

  @Test
  void modificarMisionConInsigniaInexistenteNoGuarda() {
    when(misionRepository.findConInsigniaById(any())).thenReturn(Optional.of(mision));
    when(insigniaRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> fachada.modificarMision(misionDto(mision.getId())));

    verify(misionRepository, never()).save(any());
    verify(eventos, never()).publicar(any());
  }

  @Test
  void eliminarMisionLaDejaFueraDeServicioYLaDesasigna() {
    MisionHistorico activa = new MisionHistorico(DONADOR, mision, MisionHistorico.EstadoMision.ACTIVA);
    when(misionRepository.findById(any())).thenReturn(Optional.of(mision));
    when(misionHistoricoRepository.findByMision_IdAndEstado(any(), any())).thenReturn(List.of(activa));
    when(donadorMisionRepository.findDonadorIdsByMisionId(any())).thenReturn(List.of(DONADOR));

    fachada.eliminarMision(mision.getId());

    InOrder orden = inOrder(donadorMisionRepository, eventos);
    orden.verify(donadorMisionRepository).deleteByMisionId(any());
    orden.verify(eventos).publicar(publicados.capture());
    assertEquals(0, foto(publicados.getValue()).misiones().size());
    verify(misionRepository, never()).deleteById(any());
    assertEquals("misión fuera de servicio", mision.getDescripcion());
    assertEquals(MisionHistorico.EstadoMision.CANCELADA, activa.getEstado());
    assertNotNull(activa.getFechaFin());
  }

  @Test
  void unaMisionFueraDeServicioNoSeAsignaAutomaticamente() {
    mision.setDescripcion(Mision.DESCRIPCION_FUERA_DE_SERVICIO);
    when(donadoresClient.obtenerDonador(DONADOR_ID)).thenReturn(donador("OCASIONAL"));
    when(donadorMisionRepository.findByDonadorId(DONADOR)).thenReturn(Optional.empty());
    when(misionRepository.findAll()).thenReturn(List.of(mision));

    fachada.procesarDonador(DONADOR_ID);

    verify(donadorMisionRepository, never()).save(any());
    verify(eventos, never()).publicar(any());
  }

  @Test
  void unaMisionFueraDeServicioNoSeAsignaAMano() {
    mision.setDescripcion(Mision.DESCRIPCION_FUERA_DE_SERVICIO);
    when(misionRepository.findById(any())).thenReturn(Optional.of(mision));
    MisionDTO pedida = new MisionDTO(mision.getId(), null, null, null, null, null);

    assertThrows(IllegalStateException.class, () -> fachada.asignarMisionADonador(DONADOR_ID, pedida));

    verify(donadorMisionRepository, never()).save(any());
  }

  @Test
  void modificarUnaMisionFueraDeServicioNoLaReactiva() {
    mision.setDescripcion(Mision.DESCRIPCION_FUERA_DE_SERVICIO);
    when(misionRepository.findConInsigniaById(any())).thenReturn(Optional.of(mision));
    ArgumentCaptor<Mision> guardada = ArgumentCaptor.forClass(Mision.class);

    fachada.modificarMision(new MisionDTO(mision.getId(), "Otro nombre", null,
        CategoriaDonadorEnum.OCASIONAL, CategoriaDonadorEnum.COLABORADOR, TipoMisionEnum.COMPLETITUD));

    verify(misionRepository).save(guardada.capture());
    assertEquals(true, guardada.getValue().isFueraDeServicio());
  }

  @Test
  void siFallaElGuardadoNoSePublica() {
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(insigniaRepository.save(any())).thenThrow(new RuntimeException("db caida"));

    assertThrows(RuntimeException.class,
        () -> fachada.modificarInsignia(new InsigniaDTO(insignia.getId(), "A", "B")));

    verify(eventos, never()).publicar(any());
  }

  @Test
  void asignarMisionPublicaLaFotoDelDonador() {
    when(misionRepository.findById(any())).thenReturn(Optional.of(mision));
    when(donadorMisionRepository.findByDonadorId(DONADOR))
        .thenReturn(Optional.of(new DonadorMision(DONADOR, mision)));
    when(donadorInsigniaRepository.findConInsigniaByDonadorId(DONADOR))
        .thenReturn(List.of(new DonadorInsignia(DONADOR, insignia)));

    fachada.asignarMisionADonador(DONADOR_ID, new MisionDTO(mision.getId(), null, null, null, null, null));

    MisionInsigniaEvento payload = foto(unicoEvento());
    assertEquals(DONADOR_ID, payload.donadorId());
    assertEquals(mision.getId(), payload.misiones().get(0).id());
    assertEquals(insignia.getId(), payload.insignias().get(0).id());
    assertEquals(Integer.valueOf(0), payload.progresoActual());
    assertEquals(Integer.valueOf(5), payload.progresoRequerido());
    assertNotNull(payload.ocurridoEn());
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

    assertEquals(0, foto(unicoEvento()).insignias().size());
  }

  @Test
  void misionCompletadaTerminaConLaFotoActualizada() {
    MisionHistorico activa = new MisionHistorico(DONADOR, mision, MisionHistorico.EstadoMision.ACTIVA);
    activa.setId(5L);
    when(donadoresClient.obtenerDonador(DONADOR_ID))
        .thenReturn(donador("OCASIONAL"), donador("OCASIONAL"), donador("OCASIONAL"), donador("COLABORADOR"));
    when(donadorMisionRepository.findByDonadorId(DONADOR)).thenReturn(
        Optional.of(new DonadorMision(DONADOR, mision)),
        Optional.of(new DonadorMision(DONADOR, mision)),
        Optional.of(new DonadorMision(DONADOR, siguiente)));
    when(donadorInsigniaRepository.findConInsigniaByDonadorId(DONADOR))
        .thenReturn(List.of(new DonadorInsignia(DONADOR, insignia)));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString())).thenReturn(aceptadas(5));
    when(insigniaRepository.findById(any())).thenReturn(Optional.of(insignia));
    when(misionHistoricoRepository.findByDonadorId(DONADOR)).thenReturn(List.of(activa));
    when(misionRepository.findAll()).thenReturn(List.of(mision, siguiente));
    when(misionRepository.findById(any())).thenReturn(Optional.of(mision));

    fachada.procesarDonador(DONADOR_ID);

    assertEquals(List.of(
        "MisionInsigniaEvento", "CategoriaDonadorEvento", "MisionInsigniaEvento"),
        tiposPublicados(3));

    List<EventoQueue> todos = publicados.getAllValues();
    var categoria = (CategoriaDonadorEvento) todos.get(1).descripcion();
    assertEquals("OCASIONAL", categoria.categoriaAnterior());
    assertEquals("COLABORADOR", categoria.categoriaNueva());
    assertEquals(Evento.MOTIVO_PROGRESO, categoria.motivo());
    MisionInsigniaEvento ultima = foto(todos.get(2));
    assertEquals(siguiente.getId(), ultima.misiones().get(0).id());
    assertEquals(insignia.getId(), ultima.insignias().get(0).id());
  }

  @Test
  void siLaMisionAvanzoPublicaElProgreso() {
    DonadorMision enCurso = misionEnCurso(mision, aceptadas(3));

    fachada.procesarDonador(DONADOR_ID);

    verify(donadorMisionRepository).save(enCurso);
    MisionInsigniaEvento payload = foto(unicoEvento());
    assertEquals(Integer.valueOf(3), payload.progresoActual());
    assertEquals(Integer.valueOf(5), payload.progresoRequerido());
  }

  @Test
  void siElProgresoNoCambioNoHayEventos() {
    misionEnCurso(mision, aceptadas(3)).setProgresoActual(3);

    fachada.procesarDonador(DONADOR_ID);

    verify(eventos, never()).publicar(any());
    verify(donadorMisionRepository, never()).save(any());
  }

  @Test
  void elProgresoTambienBajaSiSeRechazaUnaDonacion() {
    misionEnCurso(mision, aceptadas(2)).setProgresoActual(3);

    fachada.procesarDonador(DONADOR_ID);

    assertEquals(Integer.valueOf(2), foto(unicoEvento()).progresoActual());
  }

  @Test
  void revolucionDonadoraCuentaLasDonacionesGrandes() {
    misionEnCurso(misionDeTipo(TipoMisionEnum.REVOLUCION_DONADORA), conCantidades(60, 10, 70));

    fachada.procesarDonador(DONADOR_ID);

    MisionInsigniaEvento payload = foto(unicoEvento());
    assertEquals(Integer.valueOf(2), payload.progresoActual());
    assertEquals(Integer.valueOf(4), payload.progresoRequerido());
  }

  @Test
  void donacionesAscendentesCuentaLaRachaFinal() {
    misionEnCurso(misionDeTipo(TipoMisionEnum.DONACIONES_ASCENDENTES), conCantidades(9, 1, 2, 3, 4));

    fachada.procesarDonador(DONADOR_ID);

    MisionInsigniaEvento payload = foto(unicoEvento());
    assertEquals(Integer.valueOf(4), payload.progresoActual());
    assertEquals(Integer.valueOf(5), payload.progresoRequerido());
  }

  @Test
  void donacionesAscendentesSeCumpleConCincoEnAlza() {
    Mision ascendente = misionDeTipo(TipoMisionEnum.DONACIONES_ASCENDENTES);
    misionEnCurso(ascendente, conCantidades(1, 2, 3, 4, 5));
    when(misionHistoricoRepository.findByDonadorId(DONADOR)).thenReturn(List.of());

    assertEquals(ResultadoProcesamiento.Estado.MISION_COMPLETADA,
        fachada.procesarDonador(DONADOR_ID).estado());
  }

  @Test
  void completitudCuentaLasCategoriasDistintas() {
    misionEnCurso(misionDeTipo(TipoMisionEnum.COMPLETITUD), conCantidades(1, 1, 1));
    ProductoDTO primera = mock(ProductoDTO.class);
    ProductoDTO segunda = mock(ProductoDTO.class);
    when(primera.categoriaID()).thenReturn("alimentos");
    when(segunda.categoriaID()).thenReturn("ropa");
    when(donacionesClient.buscarProductoPorID(anyString())).thenReturn(primera, primera, segunda);

    fachada.procesarDonador(DONADOR_ID);

    MisionInsigniaEvento payload = foto(unicoEvento());
    assertEquals(Integer.valueOf(2), payload.progresoActual());
    assertEquals(Integer.valueOf(3), payload.progresoRequerido());
  }

  private DonadorMision misionEnCurso(Mision enCurso, List<DonacionDTO> donaciones) {
    DonadorMision asignada = new DonadorMision(DONADOR, enCurso);
    when(donadoresClient.obtenerDonador(DONADOR_ID)).thenReturn(donador("OCASIONAL"));
    when(donadorMisionRepository.findByDonadorId(DONADOR)).thenReturn(Optional.of(asignada));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString())).thenReturn(donaciones);
    return asignada;
  }

  private Mision misionDeTipo(TipoMisionEnum tipo) {
    return new Mision(UUID.randomUUID().toString(), "Tipo", null, "OCASIONAL", "COLABORADOR", tipo);
  }

  private static List<DonacionDTO> conCantidades(int... cantidades) {
    return IntStream.of(cantidades)
        .mapToObj(c -> new DonacionDTO("d" + c, DONADOR_ID, "dep", "x", "p", c, EstadoDonacionEnum.ACEPTADA))
        .toList();
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
  void regresionPublicaLaFotoYLaCategoria() {
    MisionHistorico completada =
        new MisionHistorico(DONADOR, mision, MisionHistorico.EstadoMision.COMPLETADA);
    completada.setId(9L);
    when(misionHistoricoRepository.findByEstadoAndMision_Tipo(any(), any()))
        .thenReturn(List.of(completada));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString())).thenReturn(aceptadas(1));

    job().revisarRegresiones();

    assertEquals(List.of("MisionInsigniaEvento", "CategoriaDonadorEvento"), tiposPublicados(2));
    List<EventoQueue> todos = publicados.getAllValues();
    assertEquals(DONADOR_ID, foto(todos.get(0)).donadorId());
    var categoria = (CategoriaDonadorEvento) todos.get(1).descripcion();
    assertEquals("OCASIONAL", categoria.categoriaNueva());
    assertEquals(Evento.MOTIVO_REGRESION, categoria.motivo());
    assertEquals(DONADOR_ID, categoria.donadorId());
  }

  @Test
  void regresionGuardaElCambioEnElHistorial() {
    regresionCon(4);

    job().revisarRegresiones();

    ArgumentCaptor<CambioCategoriaHistorico> guardado = ArgumentCaptor.forClass(CambioCategoriaHistorico.class);
    verify(cambioCategoriaRepository).save(guardado.capture());
    assertEquals(DONADOR, guardado.getValue().getDonadorId());
    assertEquals("COLABORADOR", guardado.getValue().getCategoriaAnterior());
    assertEquals("OCASIONAL", guardado.getValue().getCategoriaNueva());
  }

  @Test
  void regresionNoRevierteConElMismoMinimoQueOtorgaLaMision() {
    MisionHistorico completada = regresionCon(5);

    job().revisarRegresiones();

    assertEquals(MisionHistorico.EstadoMision.COMPLETADA, completada.getEstado());
    verify(donadoresClient, never()).modificarCategoria(anyString(), anyString());
    verify(cambioCategoriaRepository, never()).save(any());
    verify(eventos, never()).publicar(any());
  }

  @Test
  void siFallaElHistorialLaRegresionPublicaIgual() {
    regresionCon(4);
    when(cambioCategoriaRepository.save(any())).thenThrow(new RuntimeException("db caida"));

    job().revisarRegresiones();

    assertEquals(List.of("MisionInsigniaEvento", "CategoriaDonadorEvento"), tiposPublicados(2));
  }

  private RegresionMisionesJob job() {
    return new RegresionMisionesJob(misionHistoricoRepository, donacionesClient, donadoresClient,
        donadorInsigniaRepository, cambioCategoriaRepository, metricas, eventos, fachada);
  }

  private MisionHistorico regresionCon(int aceptadas) {
    MisionHistorico completada =
        new MisionHistorico(DONADOR, mision, MisionHistorico.EstadoMision.COMPLETADA);
    completada.setId(9L);
    when(misionHistoricoRepository.findByEstadoAndMision_Tipo(any(), any()))
        .thenReturn(List.of(completada));
    when(donacionesClient.buscarPorDonadorYFechaInicio(anyString(), anyString()))
        .thenReturn(aceptadas(aceptadas));
    return completada;
  }

  private MisionDTO misionDto(String id) {
    return new MisionDTO(id, "M", insignia.getId(), CategoriaDonadorEnum.OCASIONAL,
        CategoriaDonadorEnum.COLABORADOR, TipoMisionEnum.COMPLETITUD);
  }
}
