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
import ar.edu.utn.dds.k3003.model.incentivos.*;
import ar.edu.utn.dds.k3003.repositories.incentivos.*;
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

  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;

  private static final Logger logger = LoggerFactory.getLogger(Fachada.class);

  public Fachada() {
    // Constructor vacío para compatibilidad con tests
  }

  public Fachada(
      InsigniaRepository insigniaRepository,
      MisionRepository misionRepository,
      DonadorInsigniaRepository donadorInsigniaRepository,
      DonadorMisionRepository donadorMisionRepository,
      MisionHistoricoRepository misionHistoricoRepository) {
    this.insigniaRepository = insigniaRepository;
    this.misionRepository = misionRepository;
    this.donadorInsigniaRepository = donadorInsigniaRepository;
    this.donadorMisionRepository = donadorMisionRepository;
    this.misionHistoricoRepository = misionHistoricoRepository;
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
    if (mision == null) throw new RuntimeException("Mision nula");
    if (mision.id() != null) {
      UUID uuid = UUID.fromString(mision.id());
      if (misionRepository.existsById(uuid)) throw new RuntimeException("Mision ya existe");
    }
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
      try {
        var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
        if (don == null) throw new NoSuchElementException("Donador inexistente");
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
      return List.of();
    }
    return asignaciones.stream().map(DonadorInsignia::getInsignia).map(IncentivosMapper::toDto).collect(Collectors.toList());
  }

  @Override
  public MisionDTO getMisionEnCursoDeDonador(String donadorID) {
    UUID donadorUUID = UUID.fromString(donadorID);
    Optional<DonadorMision> actual = donadorMisionRepository.findByDonadorId(donadorUUID);
    if (actual.isEmpty()) {
      try {
        var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
        if (don == null) throw new NoSuchElementException("Donador inexistente");
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
      return null;
    }
    return IncentivosMapper.toDto(actual.get().getMision());
  }

  @Override
  public void asignarMisionADonador(String donadorID, MisionDTO misionDTO) {
    if (misionDTO == null) throw new RuntimeException("Mision nula");
    try {
      var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (don == null) throw new RuntimeException("Donador inexistente");
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
    UUID misionUUID = UUID.fromString(misionDTO.id());
    Mision mision = misionRepository.findById(misionUUID).orElseThrow(() -> new RuntimeException("Mision inexistente"));
    UUID donadorUUID = UUID.fromString(donadorID);
    DonadorMision dm = new DonadorMision(donadorUUID, mision);
    donadorMisionRepository.save(dm);
    MisionHistorico historico = new MisionHistorico(donadorUUID, mision, MisionHistorico.EstadoMision.ACTIVA);
    misionHistoricoRepository.save(historico);
  }

  @Override
  public void asignarInsigniaADonador(String donadorID, InsigniaDTO insigniaDTO) {
    if (insigniaDTO == null) throw new RuntimeException("Insignia nula");
    try {
      var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (don == null) throw new RuntimeException("Donador inexistente");
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
    UUID insigniaUUID = UUID.fromString(insigniaDTO.id());
    Insignia insignia = insigniaRepository.findById(insigniaUUID).orElseThrow(() -> new RuntimeException("Insignia inexistente"));
    UUID donadorUUID = UUID.fromString(donadorID);
    DonadorInsignia di = new DonadorInsignia(donadorUUID, insignia);
    donadorInsigniaRepository.save(di);
  }

  public void registrarCambioCategoriaEnDonador(String donadorID, String nuevaCategoria) {
    try {
      var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
      if (don == null) throw new RuntimeException("Donador inexistente");
      fachadaDonadoresYEntidades.modifcarCategoria(donadorID, nuevaCategoria);
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> historialCategorias(String donadorID) {
    return List.of();
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

  @Override
  public void procesarDonador(String donadorID) {
    try {
      fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    List<DonacionDTO> donaciones;
    try {
      donaciones = this.fachadaDonaciones.buscarPorDonadorYFechaInicio(donadorID, LocalDate.of(1970, 1, 1));
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    if (donaciones == null) donaciones = List.of();

    UUID donadorUUID = UUID.fromString(donadorID);
    Optional<DonadorMision> misionActual = donadorMisionRepository.findByDonadorId(donadorUUID);

    if (misionActual.isEmpty()) return;

    Mision mision = misionActual.get().getMision();
    boolean completada = evaluarMision(mision, donaciones);

    if (completada) {
      if (mision.getInsignia() != null) {
        try {
          asignarInsigniaADonador(donadorID, IncentivosMapper.toDto(mision.getInsignia()));
        } catch (RuntimeException e) {
          // ignored
        }
      }

      String categoriaFin = mision.getCategoriaFin();
      if (categoriaFin != null) {
        try {
          registrarCambioCategoriaEnDonador(donadorID, categoriaFin);
        } catch (RuntimeException e) {
          // ignored
        }
      }

      final String misionIdString = mision.getId();
      List<MisionHistorico> historial = misionHistoricoRepository.findByDonadorId(donadorUUID);
      for (MisionHistorico hist : historial) {
        if (hist.getEstado() == MisionHistorico.EstadoMision.ACTIVA && hist.getMision().getId().equals(misionIdString)) {
          hist.setEstado(MisionHistorico.EstadoMision.COMPLETADA);
          hist.setFechaFin(LocalDateTime.now());
          misionHistoricoRepository.save(hist);
          break;
        }
      }

      donadorMisionRepository.deleteById(donadorUUID);

      String nuevaCategoria = null;
      try {
        var don = fachadaDonadoresYEntidades.buscarDonadorPorID(donadorID);
        nuevaCategoria = don.categoria();
      } catch (RuntimeException e) {
        // ignored
      }

      if (nuevaCategoria != null) {
        final String catBuscada = nuevaCategoria;
        Optional<Mision> siguiente = misionRepository.findAll().stream()
            .filter(m -> !m.getId().equals(misionIdString))
            .filter(m -> m.getCategoriaInicio() != null && m.getCategoriaInicio().equals(catBuscada))
            .findFirst();

        siguiente.ifPresent(nextMision -> {
          DonadorMision dm = new DonadorMision(donadorUUID, nextMision);
          donadorMisionRepository.save(dm);
          MisionHistorico historico = new MisionHistorico(donadorUUID, nextMision, MisionHistorico.EstadoMision.ACTIVA);
          misionHistoricoRepository.save(historico);
        });
      }
    }
  }

  private boolean evaluarMision(Mision misionActual, List<DonacionDTO> donaciones) {
    var donacionesAceptadas = donaciones.stream()
        .filter(d -> d != null && d.estado() == EstadoDonacionEnum.ACEPTADA)
        .collect(Collectors.toList());

    TipoMisionEnum tipoMision = misionActual.getTipo();
    if (tipoMision == null) tipoMision = inferirTipoPorNombre(misionActual.getNombre());
    if (tipoMision == null) return contarDonacionesExitosas(donacionesAceptadas) >= 20;

    return switch (tipoMision) {
      case COMPLETITUD -> evaluarCompletitud(donacionesAceptadas);
      case DONACIONES_EXITOSAS ->contarDonacionesExitosas(donacionesAceptadas) >= 20;
      case DONACIONES_ASCENDENTES -> evaluarDonacionesAscendentes(donacionesAceptadas);
      case REVOLUCION_DONADORA -> contarDonacionesGrandes(donacionesAceptadas) > 10;
    };
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
    if (pudoResolverCategoria) return categorias.size() >= 3;
    long depositosDistintos = donacionesAceptadas.stream()
        .map(DonacionDTO::depositoID)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .count();
    return depositosDistintos >= 3;
  }

  private String obtenerCategoriaProducto(DonacionDTO donacion) {
    if (donacion == null || donacion.productoID() == null) return null;
    try {
      ProductoDTO producto = fachadaDonaciones.buscarProductoPorID(donacion.productoID());
      if (producto == null) return null;
      return producto.categoriaID();
    } catch (RuntimeException ex) {
      return null;
    }
  }

  private long contarDonacionesExitosas(List<DonacionDTO> donacionesAceptadas) {
    return donacionesAceptadas.size();
  }

  private boolean evaluarDonacionesAscendentes(List<DonacionDTO> donacionesAceptadas) {
    if (donacionesAceptadas.size() < 5) return false;
    List<DonacionDTO> ultimasCinco = donacionesAceptadas.subList(donacionesAceptadas.size() - 5, donacionesAceptadas.size());
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
    this.fachadaDonaciones = fachadaDonaciones;
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }
}


