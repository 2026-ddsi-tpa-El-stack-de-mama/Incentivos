package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
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
import ar.edu.utn.dds.k3003.model.incentivos.*;
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
      DonacionesClient donacionesClient) {
    this.insigniaRepository = insigniaRepository;
    this.misionRepository = misionRepository;
    this.donadorInsigniaRepository = donadorInsigniaRepository;
    this.donadorMisionRepository = donadorMisionRepository;
    this.misionHistoricoRepository = misionHistoricoRepository;
    this.donadoresYEntidadesClient = donadoresYEntidadesClient;
    this.cambioCategoriaHistoricoRepository = cambioCategoriaHistoricoRepository;
    this.donacionesClient = donacionesClient;
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
    if (!insigniaRepository.existsById(uuid)) throw new RuntimeException("Insignia inexistente");
    Insignia ent = new Insignia(insignia.id(), insignia.nombre(), insignia.descripcion());
    Insignia saved = insigniaRepository.save(ent);
    return IncentivosMapper.toDto(saved);
  }

  public void eliminarInsignia(String insigniaID) {
    if (insigniaID == null) throw new RuntimeException("Insignia inexistente");
    UUID uuid = UUID.fromString(insigniaID);
    if (!insigniaRepository.existsById(uuid)) throw new RuntimeException("Insignia inexistente");
    donadorInsigniaRepository.deleteByInsigniaId(uuid);
    insigniaRepository.deleteById(uuid);
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

    Insignia insignia = insigniaRepository.findById(UUID.fromString(mision.insigniaID()))
        .orElseThrow(() -> new NoSuchElementException("Insignia inexistente: " + mision.insigniaID()));

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
    return IncentivosMapper.toDto(saved);
  }

  public MisionDTO modificarMision(MisionDTO mision) {
    if (mision == null || mision.id() == null) throw new RuntimeException("Mision nula o sin id");
    UUID uuid = UUID.fromString(mision.id());
    if (!misionRepository.existsById(uuid)) throw new RuntimeException("Mision inexistente");
    Mision ent = new Mision(
        mision.id(),
        mision.nombre(),
        null,
        mision.categoriaInicio() != null ? mision.categoriaInicio().name() : null,
        mision.categoriaFin() != null ? mision.categoriaFin().name() : null,
        mision.tipo()
    );
    Mision saved = misionRepository.save(ent);
    return IncentivosMapper.toDto(saved);
  }

  public void eliminarMision(String misionID) {
    if (misionID == null) throw new RuntimeException("Mision inexistente");
    UUID uuid = UUID.fromString(misionID);
    if (!misionRepository.existsById(uuid)) throw new RuntimeException("Mision inexistente");
    donadorMisionRepository.deleteByMisionId(uuid);
    misionRepository.deleteById(uuid);
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
    UUID donadorUUID = UUID.fromString(donadorID);
    DonadorMision dm = new DonadorMision(donadorUUID, mision);
    donadorMisionRepository.save(dm);
    MisionHistorico historico = new MisionHistorico(donadorUUID, mision, MisionHistorico.EstadoMision.ACTIVA);
    misionHistoricoRepository.save(historico);
  }

  @Override
  public boolean asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) {
    if (insigniaDTO == null) throw new RuntimeException("Insignia nula");

    donadoresYEntidadesClient.obtenerDonador(donadorID);

    UUID insigniaUUID = UUID.fromString(insigniaDTO.id());
    Insignia insignia = insigniaRepository.findById(insigniaUUID)
        .orElseThrow(() -> new NoSuchElementException("Insignia inexistente"));
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

    boolean completada = evaluarMision(mision, donaciones);

    if (!completada) {
      logger.info("[{}] procesarDonador - donador {} no completo la mision {}",
          requestId, donadorID, mision.getId());
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
        .filter(m -> misionAExcluirId == null || !m.getId().equals(misionAExcluirId))
        .filter(m -> categoria.equals(m.getCategoriaInicio()))
        .findFirst();
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
  private boolean evaluarMision(Mision misionActual, List<DonacionDTO> donaciones) {
    TipoMisionEnum tipoMision = misionActual.getTipo();
    logger.info("tipo mision a evaluar: {}", tipoMision);
    if (tipoMision == null) tipoMision = inferirTipoPorNombre(misionActual.getNombre());

    List<DonacionDTO> aceptadas = filtrarPorEstado(donaciones, EstadoDonacionEnum.ACEPTADA);

    if (tipoMision == null) return contarDonacionesExitosas(aceptadas) >= 20;

    logger.info("Evaluando aceptadas: {}", aceptadas);
    return switch (tipoMision) {
      case DONACIONES_EXITOSAS -> contarDonacionesExitosas(aceptadas) >= 20;
      // TODO: confirmar con el equipo si corresponde ACEPTADA o todas las donaciones
      case COMPLETITUD -> evaluarCompletitud(aceptadas);
      // TODO: confirmar con el equipo si corresponde ACEPTADA o todas las donaciones
      case DONACIONES_ASCENDENTES -> evaluarDonacionesAscendentes(aceptadas);
      // TODO: confirmar con el equipo si corresponde ACEPTADA o todas las donaciones
      case REVOLUCION_DONADORA -> contarDonacionesGrandes(aceptadas) > 10;
    };
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
  private boolean evaluarCompletitud(List<DonacionDTO> donacionesAceptadas) {
    String requestId = MDC.get("request_id");
    Set<String> categorias = new HashSet<>();
    for (var donacion : donacionesAceptadas) {
      String categoria = obtenerCategoriaProducto(donacion);
      if (categoria != null) {
        categorias.add(categoria);
      } else if (donacion != null) {
        logger.warn("[{}] evaluarCompletitud - no se pudo resolver categoria para donacionID={} productoID={}",
            requestId, donacion.id(), donacion.productoID());
      }
    }
    return categorias.size() >= 3;
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

  private long contarDonacionesExitosas(List<DonacionDTO> donacionesAceptadas) {
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
  private boolean evaluarDonacionesAscendentes(List<DonacionDTO> donacionesAceptadas) {
    String requestId = MDC.get("request_id");
    if (donacionesAceptadas.size() < 5) return false;

    logger.warn("[{}] evaluarDonacionesAscendentes - evaluando sin garantia de orden cronologico: "
            + "DonacionDTO no tiene campo de fecha, se asume que la lista ya viene ordenada "
            + "ascendentemente por donacionesClient.buscarPorDonadorYFechaInicio. Pendiente: agregar "
            + "campo de fecha al DTO en el servicio de Donaciones (ver punto 3 del review).",
        requestId);

    List<DonacionDTO> ultimasCinco =
        donacionesAceptadas.subList(donacionesAceptadas.size() - 5, donacionesAceptadas.size());
    Integer anterior = null;
    for (DonacionDTO donacion : ultimasCinco) {
      if (donacion == null || donacion.cantidad() == null) return false;
      if (anterior != null && donacion.cantidad() <= anterior) return false;
      anterior = donacion.cantidad();
    }
    return true;
  }

  private long contarDonacionesGrandes(List<DonacionDTO> donacionesAceptadas) {
    return donacionesAceptadas.stream()
        .filter(d -> d.cantidad() != null && d.cantidad() > 50)
        .count();
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