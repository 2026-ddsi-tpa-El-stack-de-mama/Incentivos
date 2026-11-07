package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.config.MetricasNegocio;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.clientes.DonacionesClient;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.dtos.CambioCategoriaDTO;
import ar.edu.utn.dds.k3003.dtos.MisionHistoricoDTO;
import ar.edu.utn.dds.k3003.dtos.ResultadoProcesamiento;
import ar.edu.utn.dds.k3003.eventos.Evento;
import ar.edu.utn.dds.k3003.eventos.PublicadorEventos;
import ar.edu.utn.dds.k3003.model.incentivos.*;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import ar.edu.utn.dds.k3003.repositories.incentivos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaIncentivos {

  private InsigniaRepository insigniaRepository;
  private MisionRepository misionRepository;
  private DonadorInsigniaRepository donadorInsigniaRepository;
  private DonadorMisionRepository donadorMisionRepository;
  private MisionHistoricoRepository misionHistoricoRepository;
  private DonadoresYEntidadesClient donadoresYEntidadesClient;
  private CambioCategoriaHistoricoRepository cambioCategoriaHistoricoRepository;
  private DonacionesClient donacionesClient;
  // Puede ser null (constructor sin args usado por los tests de catedra): siempre con null-check
  private MetricasNegocio metricas;
  // Igual que metricas: puede ser null con el constructor sin args, publicar solo via publicar()
  private PublicadorEventos eventos;

  private static final Logger logger = LoggerFactory.getLogger(Fachada.class);

  public Fachada() {}

  @Autowired
  public Fachada(
      InsigniaRepository insigniaRepository,
      MisionRepository misionRepository,
      DonadorInsigniaRepository donadorInsigniaRepository,
      DonadorMisionRepository donadorMisionRepository,
      MisionHistoricoRepository misionHistoricoRepository,
      DonadoresYEntidadesClient donadoresYEntidadesClient,
      CambioCategoriaHistoricoRepository cambioCategoriaHistoricoRepository,
      DonacionesClient donacionesClient,
      MetricasNegocio metricas,
      PublicadorEventos eventos) {
    this.insigniaRepository = insigniaRepository;
    this.misionRepository = misionRepository;
    this.donadorInsigniaRepository = donadorInsigniaRepository;
    this.donadorMisionRepository = donadorMisionRepository;
    this.misionHistoricoRepository = misionHistoricoRepository;
    this.donadoresYEntidadesClient = donadoresYEntidadesClient;
    this.cambioCategoriaHistoricoRepository = cambioCategoriaHistoricoRepository;
    this.donacionesClient = donacionesClient;
    this.metricas = metricas;
    this.eventos = eventos;
  }

  private void publicar(EventoQueue evento) {
    if (eventos != null) eventos.publicar(evento);
  }

  /**
   * Publica la foto de insignias y mision en curso del donador. Va despues de cada cambio y nunca
   * falla hacia el llamador: el cambio ya quedo guardado.
   */
  public void publicarMisionInsigniaDeDonador(UUID donadorUUID) {
    if (eventos == null) return;
    try {
      List<InsigniaDTO> insignias = donadorInsigniaRepository.findConInsigniaByDonadorId(donadorUUID).stream()
          .map(asignada -> IncentivosMapper.toDto(asignada.getInsignia()))
          .toList();
      Optional<DonadorMision> enCurso = donadorMisionRepository.findByDonadorId(donadorUUID);
      List<MisionDTO> misiones = enCurso
          .map(dm -> List.of(IncentivosMapper.toDto(dm.getMision())))
          .orElse(List.of());
      Integer progresoActual = enCurso.map(DonadorMision::getProgresoActual).orElse(null);
      Integer progresoRequerido = enCurso.map(dm -> requeridoPara(tipoDe(dm.getMision()))).orElse(null);
      eventos.publicar(Evento.misionInsignia(donadorUUID, insignias, misiones, progresoActual, progresoRequerido));
    } catch (RuntimeException e) {
      logger.warn("No se pudo publicar incentivos del donador {}: {}", donadorUUID, e.getMessage(), e);
    }
  }

  // Los datos de la ficha que no son de Incentivos los publica Donadores y Entidades. Si falla, la
  // mision ya quedo asignada y la ficha se completa con el proximo evento del donador.
  private void pedirEventoDeDonador(String donadorID) {
    try {
      donadoresYEntidadesClient.publicarDonador(donadorID);
    } catch (RuntimeException e) {
      logger.warn("No se pudo pedir el evento del donador {}: {}", donadorID, e.getMessage(), e);
    }
  }

  // Un cambio de catalogo modifica la foto de todos los donadores que tienen esa insignia o mision.
  private void publicarMisionInsigniaDeDonadores(Collection<UUID> donadores) {
    donadores.forEach(this::publicarMisionInsigniaDeDonador);
  }

  private List<UUID> donadoresConInsignia(UUID insigniaUUID) {
    return eventos == null ? List.of() : donadorInsigniaRepository.findDonadorIdsByInsigniaId(insigniaUUID);
  }

  private Insignia insigniaEnServicio(String insigniaID) {
    Insignia insignia = insigniaRepository.findById(UUID.fromString(insigniaID))
        .orElseThrow(() -> new NoSuchElementException("Insignia inexistente: " + insigniaID));
    if (insignia.isFueraDeServicio()) throw new IllegalStateException("Insignia fuera de servicio");
    return insignia;
  }

  // Prefiere una insignia que no otorgue ninguna otra mision. Falla si no queda ninguna en servicio.
  private Insignia insigniaDeReemplazo(UUID insigniaUUID) {
    List<Insignia> disponibles = insigniaRepository.findAll().stream()
        .filter(candidata -> !candidata.isFueraDeServicio())
        .filter(candidata -> !insigniaUUID.toString().equals(candidata.getId()))
        .toList();
    return disponibles.stream()
        .filter(candidata -> !misionRepository.existsByInsignia_Id(UUID.fromString(candidata.getId())))
        .findFirst()
        .or(() -> disponibles.stream().findFirst())
        .orElseThrow(() -> new IllegalStateException("No hay otra insignia en servicio para las misiones que la usan"));
  }

  private List<UUID> donadoresConMision(UUID misionUUID) {
    return eventos == null ? List.of() : donadorMisionRepository.findDonadorIdsByMisionId(misionUUID);
  }

  @Override
  public InsigniaDTO agregarInsignia(InsigniaDTO insignia) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.agregarInsignia - entrada: {}", requestId, insignia);
    if (insignia == null) {
      throw new RuntimeException("Insignia nula");
    }

    if (insignia.id() != null) {
      UUID uuid = UUID.fromString(insignia.id());
      if (insigniaRepository.existsById(uuid)) {
        throw new RuntimeException("Insignia ya existe");
      }
    }

    Insignia ent = new Insignia(insignia.id(), insignia.nombre(), insignia.descripcion());
    Insignia saved = insigniaRepository.save(ent);
    InsigniaDTO dto = IncentivosMapper.toDto(saved);
    logger.info("[{}] Fachada.agregarInsignia - salida: id={}", requestId, dto.id());
    return dto;
  }

  public InsigniaDTO modificarInsignia(InsigniaDTO insignia) {
    if (insignia == null) throw new RuntimeException("Insignia nula");
    if (insignia.id() == null) throw new RuntimeException("Insignia sin id");
    UUID uuid = UUID.fromString(insignia.id());
    Insignia existente = insigniaRepository.findById(uuid)
        .orElseThrow(() -> new RuntimeException("Insignia inexistente"));
    String descripcion = existente.isFueraDeServicio() ? existente.getDescripcion() : insignia.descripcion();
    Insignia ent = new Insignia(insignia.id(), insignia.nombre(), descripcion);
    Insignia saved = insigniaRepository.save(ent);
    InsigniaDTO dto = IncentivosMapper.toDto(saved);
    publicarMisionInsigniaDeDonadores(donadoresConInsignia(uuid));
    return dto;
  }

  public void eliminarInsignia(String insigniaID) {
    if (insigniaID == null) throw new RuntimeException("Insignia inexistente");
    UUID uuid = UUID.fromString(insigniaID);
    Insignia insignia = insigniaRepository.findById(uuid)
        .orElseThrow(() -> new RuntimeException("Insignia inexistente"));

    // No se borra la fila: queda fuera de servicio y sus misiones pasan a otorgar otra. El reemplazo
    // se busca primero para no tocar nada si no hay ninguna disponible.
    List<Mision> misiones = misionRepository.findByInsignia_Id(uuid);
    Insignia reemplazo = misiones.isEmpty() ? null : insigniaDeReemplazo(uuid);

    insignia.setDescripcion(Insignia.DESCRIPCION_FUERA_DE_SERVICIO);
    insigniaRepository.save(insignia);

    misiones.forEach(mision -> mision.setInsignia(reemplazo));
    misionRepository.saveAll(misiones);

    // Se buscan antes de desasignar: despues ya no queda registro de quien la tenia.
    Set<UUID> afectados = new LinkedHashSet<>(donadoresConInsignia(uuid));
    misiones.forEach(mision -> afectados.addAll(donadoresConMision(UUID.fromString(mision.getId()))));
    donadorInsigniaRepository.deleteByInsigniaId(uuid);
    publicarMisionInsigniaDeDonadores(afectados);
  }

  public List<InsigniaDTO> listarInsignias() {
    return insigniaRepository.findAll().stream().map(IncentivosMapper::toDto).collect(Collectors.toList());
  }

  public InsigniaDTO buscarInsigniaPorID(String insigniaID) {
    if (insigniaID == null) throw new NoSuchElementException("Insignia inexistente");
    UUID uuid = UUID.fromString(insigniaID);
    Insignia insignia = insigniaRepository.findById(uuid).orElseThrow(() -> new NoSuchElementException("Insignia inexistente"));
    return IncentivosMapper.toDto(insignia);
  }

  @Override
  public MisionDTO agregarMision(MisionDTO mision) {
    if (mision == null) {
      throw new IllegalArgumentException("Mision nula");
    }
    if (mision.id() != null) {
      UUID uuid = UUID.fromString(mision.id());
      if (misionRepository.existsById(uuid)) {
        throw new IllegalStateException("Mision ya existe");
      }
    }
    if (mision.insigniaID() == null) {
      throw new IllegalArgumentException("La mision debe tener una insignia asociada");
    }

    Insignia insignia = insigniaEnServicio(mision.insigniaID());

    Mision ent = new Mision(
        mision.id(),
        mision.nombre(),
        null, // parámetro muerto, no lo usa el constructor — no confundir con la asociación real
        mision.categoriaInicio() != null ? mision.categoriaInicio().name() : null,
        mision.categoriaFin() != null ? mision.categoriaFin().name() : null,
        mision.tipo()
    );
    ent.setInsignia(insignia); // esta es la única vía real de asociar la insignia

    Mision saved = misionRepository.save(ent);
    MisionDTO dto = IncentivosMapper.toDto(saved);
    return dto;
  }

  public MisionDTO modificarMision(MisionDTO mision) {
    if (mision == null || mision.id() == null) throw new RuntimeException("Mision nula o sin id");
    UUID uuid = UUID.fromString(mision.id());
    Mision existente = misionRepository.findConInsigniaById(uuid)
        .orElseThrow(() -> new RuntimeException("Mision inexistente"));
    Mision ent = new Mision(
        mision.id(),
        mision.nombre(),
        null,
        mision.categoriaInicio() != null ? mision.categoriaInicio().name() : null,
        mision.categoriaFin() != null ? mision.categoriaFin().name() : null,
        mision.tipo()
    );
    // Sin insigniaID se conserva la actual: el constructor de Mision no asocia la insignia.
    ent.setInsignia(mision.insigniaID() != null
        ? insigniaEnServicio(mision.insigniaID())
        : existente.getInsignia());
    ent.setDescripcion(existente.getDescripcion());
    misionRepository.save(ent);
    // ent ya tiene la insignia cargada; la copia que devuelve save la trae lazy.
    MisionDTO dto = IncentivosMapper.toDto(ent);
    publicarMisionInsigniaDeDonadores(donadoresConMision(uuid));
    return dto;
  }

  public void eliminarMision(String misionID) {
    if (misionID == null) throw new RuntimeException("Mision inexistente");
    UUID uuid = UUID.fromString(misionID);
    Mision mision = misionRepository.findById(uuid)
        .orElseThrow(() -> new RuntimeException("Mision inexistente"));

    // No se borra la fila: mision_historico la sigue referenciando. Queda fuera de servicio y se
    // desasigna, y buscarMisionParaCategoria ya no la ofrece.
    mision.setDescripcion(Mision.DESCRIPCION_FUERA_DE_SERVICIO);
    misionRepository.save(mision);

    List<MisionHistorico> activas =
        misionHistoricoRepository.findByMision_IdAndEstado(uuid, MisionHistorico.EstadoMision.ACTIVA);
    activas.forEach(hist -> {
      hist.setEstado(MisionHistorico.EstadoMision.CANCELADA);
      hist.setFechaFin(LocalDateTime.now());
    });
    misionHistoricoRepository.saveAll(activas);

    List<UUID> afectados = donadorMisionRepository.findDonadorIdsByMisionId(uuid);
    donadorMisionRepository.deleteByMisionId(uuid);
    publicarMisionInsigniaDeDonadores(afectados);
  }

  public List<MisionDTO> listarMisiones() {
    return misionRepository.findAll().stream().map(IncentivosMapper::toDto).collect(Collectors.toList());
  }

  public MisionDTO buscarMisionPorID(String misionID) {
    if (misionID == null) throw new NoSuchElementException("Mision inexistente");
    UUID uuid = UUID.fromString(misionID);
    Mision mision = misionRepository.findById(uuid).orElseThrow(() -> new NoSuchElementException("Mision inexistente"));
    return IncentivosMapper.toDto(mision);
  }

  @Override
  public List<InsigniaDTO> getInsigniasDeDonador(String donadorID) {
    UUID donadorUUID = UUID.fromString(donadorID);
    List<DonadorInsignia> asignaciones = donadorInsigniaRepository.findByDonadorId(donadorUUID);
    if (asignaciones.isEmpty()) {
      throw new NoSuchElementException("El donador no tiene insignias asignadas");
    }
    return asignaciones.stream()
        .map(DonadorInsignia::getInsignia)
        .map(IncentivosMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public MisionDTO getMisionEnCursoDeDonador(String donadorID) {
    UUID donadorUUID = UUID.fromString(donadorID);
    return donadorMisionRepository.findByDonadorId(donadorUUID)
        .map(dm -> IncentivosMapper.toDto(dm.getMision()))
        .orElseThrow(() -> new NoSuchElementException("El donador no tiene mision en curso"));
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) {
    if (misionDTO == null) throw new RuntimeException("Mision nula");

    // Valida existencia del donador en el microservicio externo
    donadoresYEntidadesClient.obtenerDonador(donadorID);

    UUID misionUUID = UUID.fromString(misionDTO.id());
    Mision mision = misionRepository.findById(misionUUID)
        .orElseThrow(() -> new RuntimeException("Mision inexistente"));
    if (mision.isFueraDeServicio()) throw new IllegalStateException("Mision fuera de servicio");
    UUID donadorUUID = UUID.fromString(donadorID);
    DonadorMision dm = new DonadorMision(donadorUUID, mision);
    donadorMisionRepository.save(dm);
    MisionHistorico historico = new MisionHistorico(donadorUUID, mision, MisionHistorico.EstadoMision.ACTIVA);
    misionHistoricoRepository.save(historico);
    publicarMisionInsigniaDeDonador(donadorUUID);
    pedirEventoDeDonador(donadorID);
  }

  @Override
  public boolean asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) {
    if (insigniaDTO == null) throw new RuntimeException("Insignia nula");

    donadoresYEntidadesClient.obtenerDonador(donadorID);

    UUID insigniaUUID = UUID.fromString(insigniaDTO.id());
    Insignia insignia = insigniaRepository.findById(insigniaUUID)
        .orElseThrow(() -> new NoSuchElementException("Insignia inexistente"));
    if (insignia.isFueraDeServicio()) throw new IllegalStateException("Insignia fuera de servicio");
    UUID donadorUUID = UUID.fromString(donadorID);

    if (donadorInsigniaRepository.existsByDonadorIdAndInsigniaId(donadorUUID, insigniaUUID)) {
      // TODO (negocio, sin confirmar con equipo): decidimos NO tratar esto como error.
      // Una insignia es única por donador (constraint DB). Si la mision activa otorga
      // una insignia que el donador ya tiene, el flujo de procesarDonador continua
      // igual (avanza categoria, se asigna siguiente mision); solo no hay insignia
      // nueva en ESTE ciclo. Alternativa descartada: bloquear la asignacion de la
      // mision desde el origen (filtrar en la busqueda de "siguiente mision" toda
      // mision cuya insignia ya este otorgada) — se descarta por ahora porque no
      // esta definido que hacer cuando no queda ninguna mision "limpia" disponible
      // para la categoria del donador.
      return false; // no fue una asignacion nueva
    }

    donadorInsigniaRepository.save(new DonadorInsignia(donadorUUID, insignia));
    // Unico punto de medicion: lo usan el endpoint y procesarDonador (job incluido).
    // Solo cuenta asignaciones nuevas (si el donador ya la tenia, devuelve false mas arriba).
    if (metricas != null) metricas.insigniasAsignadas.increment();
    publicarMisionInsigniaDeDonador(donadorUUID);
    return true;
  }
  public void registrarCambioCategoriaEnDonador(String donadorID, String nuevaCategoria) {
    DonadorDTO donador = donadoresYEntidadesClient.obtenerDonador(donadorID);
    String categoriaAnterior = donador.categoria();

    donadoresYEntidadesClient.modificarCategoria(donadorID, nuevaCategoria);

    UUID donadorUUID = UUID.fromString(donadorID);
    try {
      cambioCategoriaHistoricoRepository.save(new CambioCategoriaHistorico(donadorUUID, categoriaAnterior, nuevaCategoria));
    } catch (RuntimeException e) {
      logger.warn("No se pudo registrar historico de cambio de categoria para donador {}: {}", donadorID, e.getMessage(), e);
    }
    if (metricas != null) metricas.categoriaCambiada(categoriaAnterior, nuevaCategoria, "progreso");
    publicar(Evento.categoriaCambiada(donadorID, categoriaAnterior, nuevaCategoria, Evento.MOTIVO_PROGRESO));
  }

  public List<CambioCategoriaDTO> historialCategorias(String donadorID) {
    UUID donadorUUID = UUID.fromString(donadorID);
    List<CambioCategoriaHistorico> historial =
        cambioCategoriaHistoricoRepository.findByDonadorIdOrderByFechaDesc(donadorUUID);
    return historial.stream()
        .map(h -> new CambioCategoriaDTO(h.getCategoriaAnterior(), h.getCategoriaNueva(), h.getFecha()))
        .collect(Collectors.toList());
  }

  public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
    throw new UnsupportedOperationException();
  }

  public DonadorDTO buscarDonadorPorID(String donadorID) {
    throw new UnsupportedOperationException();
  }

  public NecesidadMaterialDTO agregarNecesidadMaterial(NecesidadMaterialDTO necesidadMaterialDTO) {
    throw new UnsupportedOperationException();
  }

  // NOTA (punto 7 del prompt): esta secuencia NO es atómica y deliberadamente no se
  // envuelve en @Transactional. Los pasos "asignar insignia" y "cambiar categoría"
  // llaman a donadoresYEntidadesClient (HTTP externo) — mantener una transacción de
  // DB abierta durante llamadas de red retiene conexiones del pool y no protege esos
  // efectos igual, porque no son recursos transaccionales de la DB. Clasificación:
  //   - best-effort (no abortan el proceso si fallan): asignar insignia, cambiar categoría.
  //   - crítico (si fallan, el estado queda inconsistente y se loguea como WARN):
  //     marcar historial COMPLETADA, borrar la misión activa, asignar siguiente misión.
  // Riesgo conocido y no resuelto en este fix: si el historial no llega a marcarse
  // COMPLETADA (break no alcanzado) pero el deleteById sí se ejecuta, queda un
  // registro ACTIVA huérfano sin misión activa asociada. Se decide loguear el caso
  // (ver punto 5.5) en vez de abortar, porque abortar dejaría al donador sin misión
  // activa y sin insignia/categoría ya otorgadas — peor resultado que un historial
  // desincronizado que se puede reconciliar después. Discutir con el equipo si esto
  // amerita @Transactional a nivel de los pasos críticos únicamente (excluyendo las
  // llamadas HTTP) o un patrón de outbox/saga.
  @Override
  public ResultadoProcesamiento procesarDonador(String donadorID) {
    // Punto unico de medicion: lo usan tanto el endpoint como ProcesarDonadoresJob.
    ResultadoProcesamiento resultado;
    try {
      resultado = procesarDonadorInterno(donadorID);
    } catch (RuntimeException e) {
      if (metricas != null) metricas.donadoresProcesamientoFallido.increment();
      throw e;
    }
    if (metricas != null) {
      metricas.donadoresEvaluados.increment();
      if (resultado.huboCambios()) metricas.donadoresProcesados.increment();
      if (resultado.estado() == ResultadoProcesamiento.Estado.MISION_COMPLETADA) {
        metricas.misionCompletada(tipoMisionDe(resultado.misionId()));
      }
    }
    return resultado;
  }

  private String tipoMisionDe(String misionId) {
    try {
      return misionRepository.findById(UUID.fromString(misionId))
          .map(m -> m.getTipo() != null ? m.getTipo().name() : null)
          .orElse(null);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private ResultadoProcesamiento procesarDonadorInterno(String donadorID) {
    String requestId = MDC.get("request_id");
    logger.info("Consultando con donadores y entidades para obtener el donador");
    donadoresYEntidadesClient.obtenerDonador(donadorID);
    logger.info("Donador existente: {}", donadorID);

    UUID donadorUUID = UUID.fromString(donadorID);

    logger.info("Consultando mision activa del donador");
    Optional<DonadorMision> misionActual = donadorMisionRepository.findByDonadorId(donadorUUID);
    logger.info("Se obtuvo mision activa del donador: {}", misionActual.map(dm -> dm.getMision().getId()).orElse(null));

    // Donador sin mision activa: en vez de cortar sin hacer nada, intentamos
    // engancharlo con la primera mision disponible para su categoria actual.
    // El estado que se devuelve sigue siendo SIN_MISION_ACTIVA (es cierto: no
    // tenia ninguna al entrar al metodo), pero si se le asigno una, se reporta
    // en siguienteMisionId. No se evalua en esta misma pasada si el donador ya
    // cumpliria la mision recien asignada con donaciones previas — eso queda
    // para el proximo procesarDonador, para no mezclar "alta" con "evaluacion".
    if (misionActual.isEmpty()) {
      logger.info("[{}] procesarDonador - donador {} sin mision activa", requestId, donadorID);

      String categoriaActual = null;
      try {
        categoriaActual = donadoresYEntidadesClient.obtenerDonador(donadorID).categoria();
      } catch (RuntimeException e) {
        logger.warn("[{}] procesarDonador - no se pudo obtener categoria del donador {}, "
                + "no se intentara asignar mision inicial: {}",
            requestId, donadorID, e.getMessage(), e);
      }

      String misionAsignadaId = null;
      if (categoriaActual != null) {
        Optional<Mision> misionParaAsignar = buscarMisionParaCategoria(categoriaActual, null);
        if (misionParaAsignar.isPresent()) {
          Mision nuevaMision = misionParaAsignar.get();
          donadorMisionRepository.save(new DonadorMision(donadorUUID, nuevaMision));
          misionHistoricoRepository.save(
              new MisionHistorico(donadorUUID, nuevaMision, MisionHistorico.EstadoMision.ACTIVA));
          publicarMisionInsigniaDeDonador(donadorUUID);
          pedirEventoDeDonador(donadorID);
          misionAsignadaId = nuevaMision.getId();
          logger.info("[{}] procesarDonador - donador {} sin mision activa, se le asigno la mision {}",
              requestId, donadorID, misionAsignadaId);
        } else {
          logger.info("[{}] procesarDonador - donador {} sin mision activa y sin mision disponible para categoria {}",
              requestId, donadorID, categoriaActual);
        }
      }

      return new ResultadoProcesamiento(
          ResultadoProcesamiento.Estado.SIN_MISION_ACTIVA, null, false, false, misionAsignadaId);
    }

    DonadorMision donadorMision = misionActual.get();
    Mision mision = donadorMision.getMision();
    LocalDate fechaAsignacion = donadorMision.getFechaAsignacion().toLocalDate();

    logger.info("Donaciones de donador {} a partir de fecha: {}", donadorID, fechaAsignacion);
    List<DonacionDTO> donaciones = donacionesClient.buscarPorDonadorYFechaInicio(donadorID, fechaAsignacion.toString());
    logger.info("Se obtuvo listado de donaciones");

    if (donaciones == null) donaciones = List.of();

    ProgresoMision progreso = calcularProgreso(mision, donaciones);

    if (!progreso.cumplida()) {
      logger.info("[{}] procesarDonador - donador {} no completo la mision {} ({}/{})",
          requestId, donadorID, mision.getId(), progreso.actual(), progreso.requerido());
      actualizarProgreso(donadorMision, progreso);
      return new ResultadoProcesamiento(
          ResultadoProcesamiento.Estado.MISION_NO_COMPLETADA, mision.getId(), false, false, null);
    }

    boolean insigniaAsignada = false;
    if (mision.getInsignia() != null) {
      try {
        insigniaAsignada = asignarInsigniaADonador(donadorID, IncentivosMapper.toDto(mision.getInsignia()));
      } catch (RuntimeException e) {
        logger.warn("[{}] procesarDonador - fallo al asignar insignia a donador {}: {}",
            requestId, donadorID, e.getMessage(), e);
      }
    }

    boolean categoriaActualizada = false;
    String categoriaFin = mision.getCategoriaFin();
    if (categoriaFin != null) {
      try {
        registrarCambioCategoriaEnDonador(donadorID, categoriaFin);
        categoriaActualizada = true;
      } catch (RuntimeException e) {
        logger.warn("[{}] procesarDonador - fallo al cambiar categoria de donador {} a {}: {}",
            requestId, donadorID, categoriaFin, e.getMessage(), e);
      }
    }

    // Marcar misión como completada en el historial
    final String misionIdString = mision.getId();
    List<MisionHistorico> historial = misionHistoricoRepository.findByDonadorId(donadorUUID);
    boolean historicoMarcado = false;
    for (MisionHistorico hist : historial) {
      if (hist.getEstado() == MisionHistorico.EstadoMision.ACTIVA
          && hist.getMision().getId().equals(misionIdString)) {
        hist.setEstado(MisionHistorico.EstadoMision.COMPLETADA);
        hist.setFechaFin(LocalDateTime.now());
        misionHistoricoRepository.save(hist);
        historicoMarcado = true;
        break;
      }
    }
    if (!historicoMarcado) {
      logger.warn("[{}] procesarDonador - no se encontro historico ACTIVA para donador {} y mision {}; "
              + "se continua con deleteById, el historial puede quedar desincronizado",
          requestId, donadorID, misionIdString);
    }

    // Quitar misión activa (crítico, ver nota de atomicidad arriba)
    donadorMisionRepository.deleteById(donadorUUID);

    // Buscar siguiente misión según la nueva categoría del donador
    String nuevaCategoria = null;
    try {
      nuevaCategoria = donadoresYEntidadesClient.obtenerDonador(donadorID).categoria();
    } catch (RuntimeException e) {
      logger.warn("[{}] procesarDonador - no se pudo obtener la nueva categoria del donador {}, "
              + "no se asignara siguiente mision: {}",
          requestId, donadorID, e.getMessage(), e);
    }

    String siguienteMisionId = null;
    if (nuevaCategoria != null) {
      Optional<Mision> siguienteMision = buscarMisionParaCategoria(nuevaCategoria, mision.getId());
      if (siguienteMision.isPresent()) {
        Mision nextMision = siguienteMision.get();
        donadorMisionRepository.save(new DonadorMision(donadorUUID, nextMision));
        misionHistoricoRepository.save(
            new MisionHistorico(donadorUUID, nextMision, MisionHistorico.EstadoMision.ACTIVA));
        siguienteMisionId = nextMision.getId();
      } else {
        logger.info("[{}] procesarDonador - no hay siguiente mision para la categoria {} (donador {})",
            requestId, nuevaCategoria, donadorID);
      }
    }

    // Una sola vez y al final: cubre la mision completada, con o sin siguiente mision.
    publicarMisionInsigniaDeDonador(donadorUUID);

    return new ResultadoProcesamiento(
        ResultadoProcesamiento.Estado.MISION_COMPLETADA, mision.getId(),
        insigniaAsignada, categoriaActualizada, siguienteMisionId);
  }

  // Busca una mision cuya categoriaInicio matchee la categoria dada.
// misionAExcluirId es opcional: se usa cuando venimos de completar una mision
// y no queremos reasignar la misma (uso al final de procesarDonador). En el
// alta de donador sin mision activa se pasa null.
  private Optional<Mision> buscarMisionParaCategoria(String categoria, String misionAExcluirId) {
    if (categoria == null) return Optional.empty();
    return misionRepository.findAll().stream()
        .filter(m -> !m.isFueraDeServicio())
        .filter(m -> misionAExcluirId == null || !m.getId().equals(misionAExcluirId))
        .filter(m -> categoria.equals(m.getCategoriaInicio()))
        .findFirst();
  }

  public List<MisionHistoricoDTO> historialMisiones(String donadorID) {
    UUID donadorUUID = UUID.fromString(donadorID);
    List<MisionHistorico> historial =
        misionHistoricoRepository.findByDonadorIdOrderByFechaInicioDesc(donadorUUID);
    if (historial.isEmpty()) {
      throw new NoSuchElementException("El donador no tiene historial de misiones");
    }
    return historial.stream()
        .map(h -> new MisionHistoricoDTO(
            h.getMision().getId(),
            h.getMision().getNombre(),
            h.getEstado().name(),
            h.getFechaInicio(),
            h.getFechaFin()))
        .collect(Collectors.toList());
  }

  // Punto 4 del prompt — DECISIÓN DE NEGOCIO PENDIENTE DE CONFIRMAR CON EL EQUIPO:
  // El enunciado justifica el filtro por ACEPTADA textualmente solo para
  // DONACIONES_EXITOSAS ("recibidas correctamente... sin quejas"). Para las otras 3
  // misiones no hay ese requisito explícito en el enunciado que tengo disponible.
  // Abajo dejo el filtro DESACOPLADO por tipo de misión en vez de aplicarlo una sola
  // vez de forma global, para que la decisión sea explícita y unitaria por tipo.
  // Elegí, como default conservador, mantener ACEPTADA también para COMPLETITUD,
  // DONACIONES_ASCENDENTES y REVOLUCION_DONADORA, porque evaluar sobre donaciones
  // en estado INGRESADA (no confirmadas) podría completar una misión con
  // donaciones que después se rechazan. PERO esto es una hipótesis mía, no un
  // hecho verificado contra el enunciado completo — hay que confirmarlo con el
  // equipo antes de mergear. Si se decide lo contrario para algún tipo, cambiar
  // solo esa rama.
  private ProgresoMision calcularProgreso(Mision misionActual, List<DonacionDTO> donaciones) {
    TipoMisionEnum tipoMision = tipoDe(misionActual);
    logger.info("tipo mision a evaluar: {}", tipoMision);
    //todo: cambiar conqueja a aceptada y =>20
    List<DonacionDTO> aceptadas = filtrarPorEstado(donaciones, EstadoDonacionEnum.ACEPTADA);

    logger.info("Evaluando aceptadas: {}", aceptadas);
    int actual = tipoMision == null ? contarDonacionesExitosas(aceptadas) : switch (tipoMision) {
      case DONACIONES_EXITOSAS -> contarDonacionesExitosas(aceptadas);
      // TODO: confirmar con el equipo si corresponde ACEPTADA o todas las donaciones
      case COMPLETITUD -> contarCategoriasDistintas(aceptadas);
      // TODO: confirmar con el equipo si corresponde ACEPTADA o todas las donaciones
      case DONACIONES_ASCENDENTES -> contarUltimasAscendentes(aceptadas);
      // TODO: confirmar con el equipo si corresponde ACEPTADA o todas las donaciones
      case REVOLUCION_DONADORA -> contarDonacionesGrandes(aceptadas);
    };
    return new ProgresoMision(actual, requeridoPara(tipoMision));
  }

  private record ProgresoMision(int actual, int requerido) {
    boolean cumplida() {
      return actual >= requerido;
    }
  }

  private TipoMisionEnum tipoDe(Mision mision) {
    return mision.getTipo() != null ? mision.getTipo() : inferirTipoPorNombre(mision.getNombre());
  }

  private int requeridoPara(TipoMisionEnum tipoMision) {
    if (tipoMision == null) return 5;
    return switch (tipoMision) {
      case DONACIONES_EXITOSAS, DONACIONES_ASCENDENTES -> 5;
      case COMPLETITUD -> 3;
      case REVOLUCION_DONADORA -> 4;
    };
  }

  // Se publica solo si cambio: procesarDonador corre para todos los donadores en cada pasada del job.
  private void actualizarProgreso(DonadorMision donadorMision, ProgresoMision progreso) {
    if (donadorMision.getProgresoActual() == progreso.actual()) return;
    donadorMision.setProgresoActual(progreso.actual());
    donadorMisionRepository.save(donadorMision);
    publicarMisionInsigniaDeDonador(donadorMision.getDonadorId());
  }

  private List<DonacionDTO> filtrarPorEstado(List<DonacionDTO> donaciones, EstadoDonacionEnum estado) {
    return donaciones.stream()
        .filter(d -> d != null && d.estado() == estado)
        .collect(Collectors.toList());
  }

  private TipoMisionEnum inferirTipoPorNombre(String nombreMision) {
    if (nombreMision == null) return null;
    String normalizado = nombreMision.trim().toLowerCase();
    return switch (normalizado) {
      case "completitud" -> TipoMisionEnum.COMPLETITUD;
      case "donaciones exitosas" -> TipoMisionEnum.DONACIONES_EXITOSAS;
      case "donaciones ascendentes" -> TipoMisionEnum.DONACIONES_ASCENDENTES;
      case "revolucion donadora", "revolución donadora" -> TipoMisionEnum.REVOLUCION_DONADORA;
      default -> null;
    };
  }

  // Punto 1 y 2 del prompt: se elimina el fallback por depositoID (no tiene relación
  // con el requisito de negocio de "3 categorías distintas") y se loguean los casos
  // en que no se pudo resolver la categoría de una donación, en vez de comerse el
  // error silenciosamente.
  private int contarCategoriasDistintas(List<DonacionDTO> donacionesAceptadas) {
    String requestId = MDC.get("request_id");
    Set<String> categorias = new HashSet<>();
    for (var donacion : donacionesAceptadas) {
      String categoria = obtenerCategoriaProducto(donacion);
      if (categoria != null) {
        categorias.add(categoria);
      } else if (donacion != null) {
        logger.warn("[{}] contarCategoriasDistintas - no se pudo resolver categoria para donacionID={} productoID={}",
            requestId, donacion.id(), donacion.productoID());
      }
    }
    return categorias.size();
  }

  private String obtenerCategoriaProducto(DonacionDTO donacion) {
    if (donacion == null || donacion.productoID() == null) return null;
    String requestId = MDC.get("request_id");
    try {
      ProductoDTO producto = donacionesClient.buscarProductoPorID(donacion.productoID());
      return producto != null ? producto.categoriaID() : null;
    } catch (RuntimeException ex) {
      logger.warn("[{}] obtenerCategoriaProducto - fallo al resolver producto donacionID={} productoID={}: {}",
          requestId, donacion.id(), donacion.productoID(), ex.getMessage(), ex);
      return null;
    }
  }

  private int contarDonacionesExitosas(List<DonacionDTO> donacionesAceptadas) {
    return donacionesAceptadas.size();
  }

  // Punto 3 del prompt — BLOQUEADO EN SERIO, no es un detalle menor:
  // Confirmaste que DonacionDTO NO tiene fecha:
  //   record DonacionDTO(String id, String donadorID, String depositoID,
  //       String descripcion, String productoID, Integer cantidad, EstadoDonacionEnum estado)
  // Sin un campo temporal en el DTO, NO HAY forma correcta de ordenar cronológicamente
  // acá. Descarté a propósito cualquier sustituto (ordenar por `id()`, o asumir que
  // `depositoID` o el orden de inserción en la lista correlacionan con el tiempo)
  // porque son heurísticas sin garantía — exactamente el mismo tipo de "criterio
  // sustituto no relacionado con el requisito real" que se eliminó en el punto 1.
  // Inventar un orden falso acá sería peor que el bug original: haría que la misión
  // se evalúe de forma "silenciosamente incorrecta" pero ahora con apariencia de
  // estar arreglada.
  //
  // El método queda funcionalmente IGUAL al original (sigue asumiendo que
  // `donacionesClient.buscarPorDonadorYFechaInicio` devuelve orden cronológico
  // ascendente, sin garantía de contrato), pero:
  //   1. Se loguea WARN cada vez que se evalúa esta misión, dejando explícito en
  //      producción que el resultado depende de un orden no garantizado.
  //   2. Queda documentado que el fix real requiere que el servicio de Donaciones
  //      agregue un campo de fecha (p.ej. `fechaCreacion`) al DTO y lo propague — hay
  //      que coordinarlo con el equipo que tiene ese servicio, no es algo que se
  //      resuelva solo del lado de Incentivos.
  // Devuelve cuantas de las ultimas donaciones, contando desde la mas reciente, vienen en alza.
  private int contarUltimasAscendentes(List<DonacionDTO> donacionesAceptadas) {
    String requestId = MDC.get("request_id");

    logger.warn("[{}] contarUltimasAscendentes - evaluando sin garantia de orden cronologico: "
            + "DonacionDTO no tiene campo de fecha, se asume que la lista ya viene ordenada "
            + "ascendentemente por donacionesClient.buscarPorDonadorYFechaInicio. Pendiente: agregar "
            + "campo de fecha al DTO en el servicio de Donaciones (ver punto 3 del review).",
        requestId);

    int racha = 0;
    Integer anterior = null;
    for (DonacionDTO donacion : donacionesAceptadas) {
      Integer cantidad = donacion.cantidad();
      racha = cantidad == null ? 0 : anterior != null && cantidad > anterior ? racha + 1 : 1;
      anterior = cantidad;
    }
    return racha;
  }

  private int contarDonacionesGrandes(List<DonacionDTO> donacionesAceptadas) {
    return (int) donacionesAceptadas.stream()
        .filter(d -> d.cantidad() != null && d.cantidad() > 50)
        .count();
  }

  private void cancelarYReasignarMisionActiva(UUID donadorUUID, String donadorID, String categoriaPostReversion, String jobId) {
    Optional<DonadorMision> misionActivaOpt = donadorMisionRepository.findByDonadorId(donadorUUID);

    if (misionActivaOpt.isPresent()) {
      DonadorMision misionActiva = misionActivaOpt.get();
      Mision misionCancelada = misionActiva.getMision();

      List<MisionHistorico> historial = misionHistoricoRepository.findByDonadorId(donadorUUID);
      for (MisionHistorico hist : historial) {
        if (hist.getEstado() == MisionHistorico.EstadoMision.ACTIVA
            && hist.getMision().getId().equals(misionCancelada.getId())) {
          hist.setEstado(MisionHistorico.EstadoMision.CANCELADA);
          hist.setFechaFin(LocalDateTime.now());
          misionHistoricoRepository.save(hist);
          break;
        }
      }

      donadorMisionRepository.deleteById(donadorUUID);
      logger.info("[{}] cancelarYReasignarMisionActiva - donador {} - se cancelo mision activa {} por regresion",
          jobId, donadorID, misionCancelada.getId());
    }

    Optional<Mision> misionParaAsignar = buscarMisionParaCategoria(categoriaPostReversion, null);
    if (misionParaAsignar.isPresent()) {
      Mision nueva = misionParaAsignar.get();
      donadorMisionRepository.save(new DonadorMision(donadorUUID, nueva));
      misionHistoricoRepository.save(new MisionHistorico(donadorUUID, nueva, MisionHistorico.EstadoMision.ACTIVA));
      logger.info("[{}] cancelarYReasignarMisionActiva - donador {} - reasignado a mision {} (categoria {})",
          jobId, donadorID, nueva.getId(), categoriaPostReversion);
    } else {
      logger.info("[{}] cancelarYReasignarMisionActiva - donador {} - sin mision disponible para categoria {} tras regresion",
          jobId, donadorID, categoriaPostReversion);
    }
  }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    // no-op: reemplazado por DonacionesClient
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    // no-op: reemplazado por DonadoresYEntidadesClient
  }
}