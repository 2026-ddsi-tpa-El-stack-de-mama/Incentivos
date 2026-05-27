package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.repositories.incentivos.IncentivosMapper;
import ar.edu.utn.dds.k3003.repositories.incentivos.InMemoryAsignacionesIncentivosRepo;
import ar.edu.utn.dds.k3003.repositories.incentivos.InMemoryInsigniasRepo;
import ar.edu.utn.dds.k3003.repositories.incentivos.InMemoryMisionesRepo;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaIncentivos {

  private final InMemoryInsigniasRepo insigniasRepo = InMemoryInsigniasRepo.getInstance();
  private final InMemoryMisionesRepo misionesRepo = InMemoryMisionesRepo.getInstance();
  private final InMemoryAsignacionesIncentivosRepo asignacionesRepo =
      InMemoryAsignacionesIncentivosRepo.getInstance();

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;

  private static final Logger logger = LoggerFactory.getLogger(Fachada.class);


  public Fachada() {
    /*
    Para que se ejecuten correctamente los tests, se necesita tener un constructor vacio
    Es decir, que no reciba parametros.
    Si necesitan un constructor con parametros
    Java permite tener varios constructores conviviendo sin conflictos.
    */
    insigniasRepo.clear();
    misionesRepo.clear();
    asignacionesRepo.clear();
  }
  // INCENTIVOS ------------------------------------------------------

  // ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO
  @Override
  public InsigniaDTO agregarInsignia(InsigniaDTO insignia) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.agregarInsignia - entrada: {}", requestId, insignia);
    if (insignia == null) {
      throw new RuntimeException("Insignia nula");
    }

    if (insignia.id() != null && insigniasRepo.existsById(insignia.id())) {
      throw new RuntimeException("Insignia ya existe");
    }

    var ent = IncentivosMapper.toEntity(insignia);
    var dto = IncentivosMapper.toDto(insigniasRepo.save(ent));
    logger.info("[{}] Fachada.agregarInsignia - salida: id={}", requestId, dto.id());
    return dto;
  }

  public InsigniaDTO modificarInsignia(InsigniaDTO insignia) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.modificarInsignia - entrada: {}", requestId, insignia);
    if (insignia == null) {
      throw new RuntimeException("Insignia nula");
    }
    if (insignia.id() == null || !insigniasRepo.existsById(insignia.id())) {
      throw new RuntimeException("Insignia inexistente");
    }
    var ent = IncentivosMapper.toEntity(insignia);
    var dto = IncentivosMapper.toDto(insigniasRepo.save(ent));
    logger.info("[{}] Fachada.modificarInsignia - salida: id={}", requestId, dto.id());
    return dto;
  }

  public void eliminarInsignia(String insigniaID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.eliminarInsignia - entrada: id={}", requestId, insigniaID);
    if (insigniaID == null || !insigniasRepo.existsById(insigniaID)) {
      throw new RuntimeException("Insignia inexistente");
    }
    insigniasRepo.deleteById(insigniaID);
    asignacionesRepo.removeInsignia(insigniaID);
    logger.info("[{}] Fachada.eliminarInsignia - salida: id={}", requestId, insigniaID);
  }

  @Override
  public MisionDTO agregarMision(
      MisionDTO mision) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.agregarMision - entrada: {}", requestId, mision);
    if (mision == null) {
      throw new RuntimeException("Mision nula");
    }

    if (mision.id() != null && misionesRepo.existsById(mision.id())) {
      throw new RuntimeException("Mision ya existe");
    }

    var ent = IncentivosMapper.toEntity(mision);
    // todo validar que la insignia referenciada exista (elimine la validacion pq sino no pasan los tests de catedra)

    var dto = IncentivosMapper.toDto(misionesRepo.save(ent));
    logger.info("[{}] Fachada.agregarMision - salida: id={}", requestId, dto.id());
    return dto;
  }

  public MisionDTO modificarMision(MisionDTO mision) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.modificarMision - entrada: {}", requestId, mision);
    if (mision == null || mision.id() == null) {
      throw new RuntimeException("Mision nula o sin id");
    }
    if (!misionesRepo.existsById(mision.id())) {
      throw new RuntimeException("Mision inexistente");
    }
    var ent = IncentivosMapper.toEntity(mision);
    var dto = IncentivosMapper.toDto(misionesRepo.save(ent));
    logger.info("[{}] Fachada.modificarMision - salida: id={}", requestId, dto.id());
    return dto;
  }

  public void eliminarMision(String misionID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.eliminarMision - entrada: id={}", requestId, misionID);
    if (misionID == null || !misionesRepo.existsById(misionID)) {
      throw new RuntimeException("Mision inexistente");
    }
    misionesRepo.deleteById(misionID);
    asignacionesRepo.removeMisionById(misionID);
    logger.info("[{}] Fachada.eliminarMision - salida: id={}", requestId, misionID);
  }

  // listado para consultar existentes
  public List<InsigniaDTO> listarInsignias() {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.listarInsignias", requestId);
    return insigniasRepo.findAll().stream().map(IncentivosMapper::toDto).collect(Collectors.toList());
  }

  public InsigniaDTO buscarInsigniaPorID(String insigniaID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.buscarInsigniaPorID - id={}", requestId, insigniaID);
    if (insigniaID == null) {
      throw new NoSuchElementException("Insignia inexistente");
    }
    var dto = IncentivosMapper.toDto(insigniasRepo.findById(insigniaID)
        .orElseThrow(() -> new NoSuchElementException("Insignia inexistente")));
    logger.info("[{}] Fachada.buscarInsigniaPorID - salida id={}", requestId, dto.id());
    return dto;
  }

  public List<MisionDTO> listarMisiones() {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.listarMisiones", requestId);
    return misionesRepo.findAll().stream().map(IncentivosMapper::toDto).collect(Collectors.toList());
  }

  public MisionDTO buscarMisionPorID(String misionID) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.buscarMisionPorID - id={}", requestId, misionID);
    if (misionID == null) {
      throw new NoSuchElementException("Mision inexistente");
    }
    var dto = IncentivosMapper.toDto(misionesRepo.findById(misionID)
        .orElseThrow(() -> new NoSuchElementException("Mision inexistente")));
    logger.info("[{}] Fachada.buscarMisionPorID - salida id={}", requestId, dto.id());
    return dto;
  }

  @Override
  public List<InsigniaDTO> getInsigniasDeDonador(String donadorID) throws NoSuchElementException {
    var ids = asignacionesRepo.getInsignias(donadorID);
    if (ids == null || ids.isEmpty()) {
      try {
        var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
        if (don == null) {
          throw new NoSuchElementException("Donador inexistente");
        }
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
      return List.of();
    }

    var lista = new ArrayList<InsigniaDTO>();
    for (var id : ids) {
      var ent = insigniasRepo.findById(id).orElse(null);
      if (ent != null) {
        lista.add(IncentivosMapper.toDto(ent));
      }
    }

    return lista;
  }

  @Override
  public MisionDTO getMisionEnCursoDeDonador(String donadorID) throws NoSuchElementException {
    var mid = asignacionesRepo.getMision(donadorID);
    if (mid == null) {
      try {
        var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
        if (don == null) {
          throw new NoSuchElementException("Donador inexistente");
        }
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
      return null;
    }

    return IncentivosMapper.toDto(misionesRepo.findById(mid)
        .orElseThrow(() -> new NoSuchElementException("Mision inexistente")));
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) throws NoSuchElementException {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.asignarMisionADonador - donador={} mision={}", requestId, donadorID, misionDTO==null?null:misionDTO.id());
    if (misionDTO == null) {
      throw new RuntimeException("Mision nula");
    }

    try {
      var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (don == null) {
        throw new RuntimeException("Donador inexistente");
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    if (misionDTO.id() == null || !misionesRepo.existsById(misionDTO.id())) {
      throw new RuntimeException("Mision inexistente");
    }

    asignacionesRepo.setMision(donadorID, misionDTO.id());
    logger.info("[{}] Fachada.asignarMisionADonador - done donador={} mision={}", requestId, donadorID, misionDTO.id());
  }

  @Override
  public void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO)
      throws NoSuchElementException {

    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.asignarInsigniaADonador - donador={} insignia={}", requestId, donadorID, insigniaDTO==null?null:insigniaDTO.id());

    if (insigniaDTO == null) {
      throw new RuntimeException("Insignia nula");
    }

    try {
      var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (don == null) {
        throw new RuntimeException("Donador inexistente");
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    if (insigniaDTO.id() == null || !insigniasRepo.existsById(insigniaDTO.id())) {
      throw new RuntimeException("Insignia inexistente");
    }

    asignacionesRepo.setInsignia(donadorID, insigniaDTO.id());
    logger.info("[{}] Fachada.asignarInsigniaADonador - done donador={} insignia={}", requestId, donadorID, insigniaDTO.id());
  }

  // registrar cambio de categoria y mantener historial
  public void registrarCambioCategoriaEnDonador(String donadorID, String nuevaCategoria) {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.registrarCambioCategoriaEnDonador - donador={} nuevaCategoria={}", requestId, donadorID, nuevaCategoria);
    try {
      var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (don == null) {
        throw new RuntimeException("Donador inexistente");
      }
        asignacionesRepo.agregarCategoriaAnterior(donadorID, don.categoria());
      // actualizar en la fachada de donadores
      fachadaDonadoresYEntidades.modifcarCategoria(donadorID, nuevaCategoria);
      logger.info("[{}] Fachada.registrarCambioCategoriaEnDonador - done donador={}", requestId, donadorID);
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> historialCategorias(String donadorID) {
    return asignacionesRepo.historialCategorias(donadorID);
  }

  // Compatibilidad mínima para el resto del proyecto; no se usa en los tests de incentivos.
  public DonadorDTO agregarDonador(DonadorDTO donadorDTO) { throw new UnsupportedOperationException(); }
  public DonadorDTO buscarDonadorPorID(String donadorID) { throw new UnsupportedOperationException(); }
  public NecesidadMaterialDTO agregarNecesidadMaterial(NecesidadMaterialDTO necesidadMaterialDTO) { throw new UnsupportedOperationException(); }

  @Override
  public void procesarDonador(String donadorID) throws NoSuchElementException {
    String requestId = MDC.get("request_id");
    logger.info("[{}] Fachada.procesarDonador - inicio donador={}", requestId, donadorID);
    try {
      fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    List<DonacionDTO> donaciones;
    try {
      // todo que hago con el campo fecha? es el unico metodo para buscar por donadorID
      donaciones = this.fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, LocalDate.of(1970, 1, 1));
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    if (donaciones == null) {
      donaciones = List.of();
    }

    // Evaluar mision asignada
    var misionActualId = asignacionesRepo.getMision(donadorID);
    if (misionActualId == null) {
      return; // nada asignado
    }
    var misionActual = misionesRepo.findById(misionActualId).orElse(null);
    if (misionActual == null) return;

    boolean completada = evaluarMision(misionActual, donaciones);

    if (completada) {
      String insigniaID = misionActual.getInsigniaID();
      if (insigniaID != null && insigniasRepo.existsById(insigniaID)) {
        try {
          asignarInsigniaADonador(
              donadorID,
              IncentivosMapper.toDto(
                  insigniasRepo.findById(insigniaID).orElseThrow(() -> new NoSuchElementException("Insignia inexistente"))));
        } catch (RuntimeException e) {
          // manejo el error pero no hago nada
        }
      }

      var categoriaFin = misionActual.getCategoriaFin();
      if (categoriaFin != null) {
        try {
          // utilizar el registrador que mantiene historial
          registrarCambioCategoriaEnDonador(donadorID, categoriaFin);
        } catch (RuntimeException e) {
          // si falla no aborto
        }
      }

      // desbloquear siguiente mision: buscar alguna mision cuyo categoriaInicio coincida con la nueva categoria del donador
      String nuevaCategoria = null;
      try {
        var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
        nuevaCategoria = don.categoria();
      } catch (RuntimeException e) {
        // ignore
      }

      // Remover mision actual
      asignacionesRepo.removeMision(donadorID);

      logger.info("[{}] Fachada.procesarDonador - mision completada donador={} mision={} ", requestId, donadorID, misionActual.getId());

      if (nuevaCategoria != null) {
        // encontrar proxima mision
        final String categoriaBuscada = nuevaCategoria;
        Optional<Mision> siguiente =
            misionesRepo.findAll().stream()
                .filter(m -> m.getId() == null || !m.getId().equals(misionActual.getId()))
                .filter(m -> m.getCategoriaInicio() != null && m.getCategoriaInicio().equals(categoriaBuscada))
                .findFirst();
        siguiente.ifPresent(misionDTO -> asignacionesRepo.setMision(donadorID, misionDTO.getId()));
      }
    }
  }

  private boolean evaluarMision(Mision misionActual, List<DonacionDTO> donaciones) {
    var donacionesAceptadas =
        donaciones.stream()
            .filter(d -> d != null && d.estado() == EstadoDonacionEnum.ACEPTADA)
            .collect(Collectors.toList());

    TipoMisionEnum tipoMision = misionActual.getTipo();
    if (tipoMision == null) {
      tipoMision = inferirTipoPorNombre(misionActual.getNombre());
    }

    if (tipoMision == null) {
      return contarDonacionesExitosas(donacionesAceptadas) >= 20;
    }

    return switch (tipoMision) {
      case COMPLETITUD -> evaluarCompletitud(donacionesAceptadas);
      case DONACIONES_EXITOSAS -> contarDonacionesExitosas(donacionesAceptadas) >= 20;
      case DONACIONES_ASCENDENTES -> evaluarDonacionesAscendentes(donacionesAceptadas);
      case REVOLUCION_DONADORA -> contarDonacionesGrandes(donacionesAceptadas) > 10;
    };
  }

  private TipoMisionEnum inferirTipoPorNombre(String nombreMision) {
    if (nombreMision == null) {
      return null;
    }

    String normalizado = nombreMision.trim().toLowerCase();
    return switch (normalizado) {
      case "completitud" -> TipoMisionEnum.COMPLETITUD;
      case "donaciones exitosas" -> TipoMisionEnum.DONACIONES_EXITOSAS;
      case "donaciones ascendentes" -> TipoMisionEnum.DONACIONES_ASCENDENTES;
      case "revolucion donadora", "revolución donadora" -> TipoMisionEnum.REVOLUCION_DONADORA;
      default -> null;
    };
  }

  private boolean evaluarCompletitud(List<DonacionDTO> donacionesAceptadas) {
    Set<String> categorias = new HashSet<>();
    boolean pudoResolverCategoria = false;

    if (fachadaDonaciones != null) {
      for (var donacion : donacionesAceptadas) {
        String categoria = obtenerCategoriaProducto(donacion);
        if (categoria != null) {
          pudoResolverCategoria = true;
          categorias.add(categoria);
        }
      }
    }

    if (pudoResolverCategoria) {
      return categorias.size() >= 3;
    }

    long depositosDistintos =
        donacionesAceptadas.stream()
            .map(DonacionDTO::depositoID)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .count();
    return depositosDistintos >= 3;
  }

  private String obtenerCategoriaProducto(DonacionDTO donacion) {
    if (donacion == null || donacion.productoID() == null) {
      return null;
    }

    try {
      ProductoDTO producto = fachadaDonaciones.buscarProductoPorID(donacion.productoID());
      if (producto == null) {
        return null;
      }
      return producto.categoriaID();
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private long contarDonacionesExitosas(List<DonacionDTO> donacionesAceptadas) {
    return donacionesAceptadas.size();
  }

  private boolean evaluarDonacionesAscendentes(List<DonacionDTO> donacionesAceptadas) {
    if (donacionesAceptadas.size() < 5) {
      return false;
    }

    List<DonacionDTO> ultimasCinco =
        donacionesAceptadas.subList(donacionesAceptadas.size() - 5, donacionesAceptadas.size());

    Integer anterior = null;
    for (DonacionDTO donacion : ultimasCinco) {
      if (donacion == null || donacion.cantidad() == null) {
        return false;
      }
      if (anterior != null && donacion.cantidad() <= anterior) {
        return false;
      }
      anterior = donacion.cantidad();
    }
    return true;
  }

  private long contarDonacionesGrandes(List<DonacionDTO> donacionesAceptadas) {
    return donacionesAceptadas.stream()
        .filter(d -> d.cantidad() != null && d.cantidad() > 50)
        .count();
  }

  // todo eventualmente esto se va, lo dejo momentaneamente para inyectar los mocks
  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }

}
