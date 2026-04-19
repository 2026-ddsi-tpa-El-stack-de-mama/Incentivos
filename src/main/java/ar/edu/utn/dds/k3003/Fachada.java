package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.DonadorYaExistenteException;
import ar.edu.utn.dds.k3003.repositories.DonadoresRepository;
import ar.edu.utn.dds.k3003.repositories.DonadoresYEntidadesDataMapper;
import ar.edu.utn.dds.k3003.repositories.InMemoryDonadoresRepo;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class Fachada implements FachadaDonadoresYEntidades {

  private DonadoresRepository donadoresRepository;
  private DonadoresYEntidadesDataMapper donadoresYEntidadesDataMapper =
      new DonadoresYEntidadesDataMapper();

  public Fachada() {
    /*
    Para que se ejecuten correctamente los tests, se necesita tener un constructor vacio
    Es decir, que no reciba parametros.
    Si necesitan un constructor con parametros
    Java permite tener varios constructores conviviendo sin conflictos.
    */

    this.donadoresRepository = new InMemoryDonadoresRepo();
  }

  @Override
  public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
    if (this.donadoresRepository.findById(donadorDTO.id()).isPresent()) {
      throw new DonadorYaExistenteException("Ya existe un donador con ese ID");
    }

    val donador = donadoresYEntidadesDataMapper.toDonador(donadorDTO);

    val donadorGuardado = this.donadoresRepository.save(donador);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorGuardado);
  }

  @Override
  public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
    val donadorOptional = this.donadoresRepository.findById(donadorID);

    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }
    val donadorFinal = donadorOptional.get();

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public DonadorDTO modificarEstado(String donadorID, EstadoDonadorEnum estado)
      throws NoSuchElementException {

    val donadorOptional = this.donadoresRepository.findById(donadorID);

    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }

    val donadorFinal = donadorOptional.get();
    donadorFinal.setEstado(estado);

    this.donadoresRepository.deleteById(donadorID);
    this.donadoresRepository.save(donadorFinal);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public DonadorDTO modifcarCategoria(String donadorID, String categoria)
      throws NoSuchElementException {
    val donadorOptional = this.donadoresRepository.findById(donadorID);
    if (donadorOptional.isEmpty()) {
      throw new DonadorNoEncontradoException("No existe un donador con ese ID");
    }
    val donadorFinal = donadorOptional.get();
    donadorFinal.setCategoria(categoria);

    this.donadoresRepository.deleteById(donadorID);
    this.donadoresRepository.save(donadorFinal);

    return donadoresYEntidadesDataMapper.toDonadorDTO(donadorFinal);
  }

  @Override
  public void setFachadaIncentivos(FachadaIncentivos fachadaIncentivos) {}

  @Override
  public Boolean puedeDonar(String donadorID) throws NoSuchElementException {
    // A implementar por el alumno
    return null;
  }

  @Override
  public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID) {
    // A implementar por el alumno
    return List.of();
  }

  @Override
  public List<QuejaDTO> obtenerQuejasDe(String donadorID) throws NoSuchElementException {
    // A implementar por el alumno
    return List.of();
  }

  @Override
  public NecesidadMaterialDTO satisfacerNecesidad(String necesidadID, Integer cantidad)
      throws NoSuchElementException {
    // A implementar por el alumno
    return null;
  }

  @Override
  public DonadorStatsDTO estadisticasDonador(String donadorID) {
    return null;
  }

  @Override
  public EntidadBeneficaDTO agregarEntidad(EntidadBeneficaDTO entidadBeneficaDTO) {
    // A implementar por el alumno
    return null;
  }

  @Override
  public EntidadBeneficaDTO buscarEntidadPorID(String entidadID) throws NoSuchElementException {
    // A implementar por el alumno
    return null;
  }

  @Override
  public NecesidadMaterialDTO registrarNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
    // A implementar por el alumno
    return null;
  }

  @Override
  public QuejaDTO agregarQueja(QuejaDTO quejaDTO) throws NoSuchElementException {
    // A implementar por el alumno
    return null;
  }
}
