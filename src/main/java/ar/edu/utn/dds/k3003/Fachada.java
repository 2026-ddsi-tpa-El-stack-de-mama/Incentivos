package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.exceptions.DepositoNoEncotradoException;
import ar.edu.utn.dds.k3003.exceptions.DepositoYaExistenteException;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.DonadoresYEntidadesDataMapper;
import ar.edu.utn.dds.k3003.repositories.asignaciones.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.asignaciones.InMemoryAsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.InMemoryDepositosRepository;
import ar.edu.utn.dds.k3003.repositories.donaciones.DonacionRepository;
import ar.edu.utn.dds.k3003.repositories.donaciones.InMemoryDonacionesRepository;
import ar.edu.utn.dds.k3003.repositories.necesidades.*;
import ar.edu.utn.dds.k3003.repositories.paquetes.InMemoryPaqueteRepository;
import ar.edu.utn.dds.k3003.repositories.paquetes.PaquetesRepository;
import ar.edu.utn.dds.k3003.services.NecesidadMaterialService;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Fachada implements FachadaLogistica {
  private DepositoRepository depositoRepository;
  private NecesidadesRepository necesidadesRepository;
  private PaquetesRepository paquetesRepository;
  private DonacionRepository donacionRepository;
  private AsignacionRepository asignacionRepository;
  private DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper = new DonadoresYEntidadesDataMapper();
  private MatchmakingAlgorithm matchmakingAlgorithm;
  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;


  private NecesidadMaterialService necesidadMaterialService;

  public Fachada() {
    /*
    Para que se ejecuten correctamente los tests, se necesita tener un constructor vacio
    Es decir, que no reciba parametros.
    Si necesitan un constructor con parametros
    Java permite tener varios constructores conviviendo sin conflictos.
    */

    this.depositoRepository = new InMemoryDepositosRepository();
    this.necesidadesRepository = new InMemoryNecesidadesRepository();
    this.paquetesRepository = new InMemoryPaqueteRepository();
    this.asignacionRepository = new InMemoryAsignacionRepository();
    this.donacionRepository = new InMemoryDonacionesRepository();
    this.donadoresYEntidadesDataMapper = new DonadoresYEntidadesDataMapper();

    this.necesidadMaterialService = new NecesidadMaterialService();




  }



  public Fachada(DepositoRepository depositoRepository,
                 NecesidadesRepository necesidadesRepository,
                 PaquetesRepository paquetesRepository,
                 DonacionRepository donacionRepository,
                 AsignacionRepository asignacionRepository) {
    this.depositoRepository = depositoRepository;
    this.necesidadesRepository = necesidadesRepository;
    this.paquetesRepository = paquetesRepository;
    this.donacionRepository = donacionRepository;
    this.asignacionRepository = asignacionRepository;
    this.donadoresYEntidadesDataMapper = new DonadoresYEntidadesDataMapper();
  }


  @Override
  public DepositoDTO agregarDeposito(DepositoDTO depositoDTO) {
    if (this.depositoRepository.findById(depositoDTO.id()).isPresent()) {
      throw new DepositoYaExistenteException("Ya existe un donador con ese ID");
    }

    val deposito = donadoresYEntidadesDataMapper.toDeposito(depositoDTO);

    val depositoGuardado = this.depositoRepository.save(deposito);

    return donadoresYEntidadesDataMapper.toDepositoDTO(depositoGuardado);
  }

  public List<DepositoDTO> obtenerDepositos() {
    return this.depositoRepository.findAll().stream().map(this.donadoresYEntidadesDataMapper::toDepositoDTO).toList();
  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {
    if (depositoID == null) throw new RuntimeException();
    val depositoOptional = this.depositoRepository.findById(depositoID);

    if (depositoOptional.isEmpty()) {
      throw new DepositoNoEncotradoException("No existe un depósito con ese ID");
    }
    val depositoFinal = depositoOptional.get();

    return donadoresYEntidadesDataMapper.toDepositoDTO(depositoFinal);
  }

  public DepositoDTO deleteDepositoByID(String depositoID) {
    if (depositoID == null) throw new RuntimeException();
    Deposito deposito = this.depositoRepository.deleteById(depositoID);
    return donadoresYEntidadesDataMapper.toDepositoDTO(deposito);
  }

  public AsignacionDTO buscarAsignacionPorID(String asignacionID) throws NoSuchElementException {
    if (asignacionID == null) throw new RuntimeException();
    Optional<Asignacion> asignacion = this.asignacionRepository.findById(asignacionID);
    if (asignacion.isEmpty()) throw new RuntimeException();
    return donadoresYEntidadesDataMapper.toAsignacionDTO(asignacion.get());
  }

  @Override
  public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {
    if(paqueteID == null)  throw new RuntimeException();
    Optional<Asignacion> asignacion = this.asignacionRepository.findAsignacionByPaqueteId(paqueteID);
    if(asignacion.isEmpty())  throw new RuntimeException();
    return  donadoresYEntidadesDataMapper.toAsignacionDTO(asignacion.get());
  }

  @Override
  public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) throws NoSuchElementException {
    if( cantidad <= 0 || depositoID == null || productoID == null) throw new RuntimeException("Bad input parameters");

    Optional<Deposito> deposito = this.depositoRepository.findById(depositoID);
    if(deposito.isEmpty()) throw new RuntimeException("Deposito no existe:");

    List<NecesidadMaterialDTO> necesidadMaterialDTOS = this.fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID);
    Donacion donacion = donacionRepository.save(new Donacion(donacionID, "", depositoID,"",productoID,
            cantidad, EstadoDonacionEnum.INGRESADA));

    Paquete paquete = paquetesRepository.save(new Paquete("", donacion.getId(), productoID, cantidad, ""));

    List<NecesidadMaterialDTO> posibles = necesidadMaterialDTOS.stream().filter(necesidadMaterialDTO ->
            this.necesidadMaterialService.sePuedeEstablecerDonacion(necesidadMaterialDTO, cantidad)).toList();


    AsignacionDTO asignacionDTO = this.ejecutarMatchmaking(depositoID, this.donadoresYEntidadesDataMapper.toPaqueteDTO(paquete), necesidadMaterialDTOS);
    if(asignacionDTO == null) throw new RuntimeException("No se pudo asignar el paquete:");

    String entidadId = "";
    for (NecesidadMaterialDTO n : necesidadMaterialDTOS) {
      if (n.id().equals(asignacionDTO.necesidadID())) {
        entidadId = n.entidadID();
        break;
      }
    }

    paquete.setEntidadAsignadaID(entidadId);

    this.paquetesRepository.modifyById(paquete.getPaqueteID(), paquete);

    Deposito dep = deposito.get();


    return donadoresYEntidadesDataMapper.toDepositoDTO(dep);
  }


  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmoEnum) {
      this.depositoRepository.findById(depositoID).ifPresent(deposito -> deposito.setTipoAlgoritmo(tipoAlgoritmoEnum));
  }

  @Override
  public AsignacionDTO ejecutarMatchmaking(String depositoID,PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidadesDeEntidadDTOS) {
    if(paqueteDTO == null || necesidadesDeEntidadDTOS == null || necesidadesDeEntidadDTOS.isEmpty() || depositoID == null) throw new RuntimeException("Bad input parameters");

    Optional<Deposito> deposito = this.depositoRepository.findById(depositoID);
    if(deposito.isEmpty()) throw new RuntimeException("Deposito no existe:");

    MatchmakingAlgorithm algorithm = MatchMakingAlgorithmFactory.getAlgorithm(deposito.get().getTipoAlgoritmo());

    Optional<NecesidadMaterialDTO> necesidad = algorithm.findNecesity(paqueteDTO, necesidadesDeEntidadDTOS);
    if(necesidad.isEmpty()) throw new RuntimeException("Necesidad no existe:");
    NecesidadMaterialDTO nec = necesidad.get();

    Asignacion asignacion = this.asignacionRepository.save(new Asignacion("", paqueteDTO.id(), nec.id(), LocalDateTime.now(), EstadoAsginacionEnum.ASIGNADA));

    return this.donadoresYEntidadesDataMapper.toAsignacionDTO(asignacion);

  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {
    if(paqueteDTO == null) throw new RuntimeException("Bad input parameters");

    this.fachadaDonaciones.cambiarEstadoDeDonacion(paqueteDTO.donacionID(), EstadoDonacionEnum.ACEPTADA);


    Asignacion asign = this.asignacionRepository.findAsignacionByPaqueteId(paqueteDTO.id())
            .orElseGet(() -> {
              Asignacion nueva = new Asignacion("", paqueteDTO.id(), "dummy-id", LocalDateTime.now(), EstadoAsginacionEnum.ASIGNADA);
              return this.asignacionRepository.save(nueva);
            });


    this.fachadaDonadoresYEntidades.satisfacerNecesidad(asign.getNecesidadID(), paqueteDTO.cantidad());


    asign.setEstado(EstadoAsginacionEnum.COMPLETADA);

    this.asignacionRepository.modifyById(asign.getId(), asign);


  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {
    this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;
  }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }


  public NecesidadMaterialDTO agregarNecesidadMaterial(NecesidadMaterialDTO necesidadMaterialDTO) {
    if (necesidadMaterialDTO == null) throw new RuntimeException("Bad input parameters");

    Necesidad necesidad = donadoresYEntidadesDataMapper.toNecesidad(necesidadMaterialDTO);

    Necesidad necesidadGuardada = this.necesidadesRepository.save(necesidad);

    return donadoresYEntidadesDataMapper.toNecesidadDTO(necesidadGuardada);

  }

  public PaqueteDTO buscarPaquetePorID(String paqueteID) throws NoSuchElementException {
    if(paqueteID == null) throw new RuntimeException("Bad input parameters");
    Optional<Paquete> paquete = this.paquetesRepository.findById(paqueteID);
    if(paquete.isEmpty()) throw new RuntimeException("No existe un paquete con ese ID");
    return donadoresYEntidadesDataMapper.toPaqueteDTO(paquete.get());
  }


}
