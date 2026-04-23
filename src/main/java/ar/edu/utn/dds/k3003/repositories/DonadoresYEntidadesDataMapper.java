package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DonadoresYEntidadesDataMapper {

  public DonadorDTO toDonadorDTO(Donador donador) {
    return new DonadorDTO(
        donador.getId(),
        donador.getNombre(),
        donador.getApellido(),
        donador.getEdad(),
        donador.getEmail(),
        donador.getNroDocumento(),
        donador.getDomicilio(),
        donador.getEstado(),
        donador.getCategoria());
  }

  public Donador toDonador(DonadorDTO donadorDTO) {
    return new Donador(
        donadorDTO.nombre(),
        donadorDTO.apellido(),
        donadorDTO.edad(),
        donadorDTO.email(),
        donadorDTO.nroDocumento(),
        donadorDTO.domicilio());
  }

  public PaqueteDTO toPaqueteDTO(Paquete paquete) {
    return new PaqueteDTO(
            paquete.getPaqueteID(),
            paquete.getDonacionID(),
            paquete.getProductoID(),
            paquete.getCantidad());
  }

  public Paquete toPaquete(PaqueteDTO paqueteDTO) {
    return new Paquete(
            paqueteDTO.id(),
            paqueteDTO.donacionID(),
            paqueteDTO.producto(),
            paqueteDTO.cantidad(),
            "");
  }

  public List<Paquete> toPaqueteList(List<PaqueteDTO> paqueteDTOList) {
    if (paqueteDTOList == null) {
      return null;
    }
    return paqueteDTOList.stream()
            .map(this::toPaquete)
            .toList();
  }

  public List<PaqueteDTO> toPaqueteDTOList(List<Paquete> paqueteList) {
    if (paqueteList == null) {
      return null;
    }
    return paqueteList.stream()
            .map(this::toPaqueteDTO)
            .toList();
  }


  public DepositoDTO toDepositoDTO(Deposito deposito) {
    return new DepositoDTO(
            deposito.getId(),
            deposito.getTipoAlgoritmo(),
            deposito.getNombre(),
            deposito.getDireccion(),
            deposito.getCapacidadMaxima(),
            toPaqueteDTOList(deposito.getStockActual()));
  }

  public Deposito toDeposito(DepositoDTO depositoDTO) {
    return new Deposito(
            depositoDTO.id(),
            depositoDTO.nombre(),
            depositoDTO.direccion(),
            depositoDTO.capacidadMaxima(),
            toPaqueteList(depositoDTO.stockActual()));
  }

  public AsignacionDTO toAsignacionDTO(Asignacion asignacion) {
    return new AsignacionDTO(
            asignacion.getId(),
            asignacion.getPaqueteID(),
            asignacion.getNecesidadID(),
            asignacion.getFecha(),
            asignacion.getEstado());
  }

  public Asignacion toAsignacion(AsignacionDTO asignacionDTO) {
    return new Asignacion(
            asignacionDTO.id(),
            asignacionDTO.paqueteID(),
            asignacionDTO.necesidadID(),
            asignacionDTO.fecha(),
            asignacionDTO.estado());
  }

  public NecesidadMaterialDTO toNecesidadDTO(Necesidad necesidad) {
    return new NecesidadMaterialDTO(
            necesidad.getId(),
            necesidad.getEntidadID(),
            necesidad.getNivelDeUrgencia(),
            necesidad.getDescripcion(),
            necesidad.getCantidadObjetivo(),
            necesidad.getProductoID(),
            necesidad.getTipoNecesidad());
  }

  public Necesidad toNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
    return new Necesidad(
            necesidadMaterialDTO.id(),
            necesidadMaterialDTO.entidadID(),
            necesidadMaterialDTO.nivelDeUrgencia(),
            necesidadMaterialDTO.descripcion(),
            necesidadMaterialDTO.cantidadObjetivo(),
            necesidadMaterialDTO.productoSolicitadoID(),
            null,
            necesidadMaterialDTO.tipo());
  }


}

  


