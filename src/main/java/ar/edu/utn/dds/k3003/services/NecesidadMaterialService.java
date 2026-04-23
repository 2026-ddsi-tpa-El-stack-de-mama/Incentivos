package ar.edu.utn.dds.k3003.services;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;

public class NecesidadMaterialService {

    public boolean sePuedeEstablecerDonacion(NecesidadMaterialDTO necesidadMaterialDTO, Integer cantidadARecibir) {
        if(necesidadMaterialDTO.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) return true;
        return necesidadMaterialDTO.cantidadObjetivo() == cantidadARecibir;
    }

}
