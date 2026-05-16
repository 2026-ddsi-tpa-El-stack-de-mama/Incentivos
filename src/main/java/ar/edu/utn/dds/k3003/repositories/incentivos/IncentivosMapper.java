package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;

public final class IncentivosMapper {
  private IncentivosMapper() {}

  public static Insignia toEntity(InsigniaDTO dto) {
    if (dto == null) return null;
    return new Insignia(dto.id(), dto.nombre(), dto.descripcion());
  }

  public static InsigniaDTO toDto(Insignia entity) {
    if (entity == null) return null;
    return new InsigniaDTO(entity.getId(), entity.getNombre(), entity.getDescripcion());
  }

  public static Mision toEntity(MisionDTO dto) {
    if (dto == null) return null;
    String categoriaInicio = dto.categoriaInicio() != null ? dto.categoriaInicio().name() : null;
    String categoriaFin = dto.categoriaFin() != null ? dto.categoriaFin().name() : null;
    return new Mision(dto.id(), dto.nombre(), dto.insigniaID(), categoriaInicio, categoriaFin, dto.tipo());
  }

  public static MisionDTO toDto(Mision entity) {
    if (entity == null) return null;
    ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum categoriaInicio = null;
    ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum categoriaFin = null;
    try {
      if (entity.getCategoriaInicio() != null) {
        categoriaInicio = ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum.valueOf(entity.getCategoriaInicio());
      }
    } catch (IllegalArgumentException ignored) {}
    try {
      if (entity.getCategoriaFin() != null) {
        categoriaFin = ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum.valueOf(entity.getCategoriaFin());
      }
    } catch (IllegalArgumentException ignored) {}

    return new MisionDTO(entity.getId(), entity.getNombre(), entity.getInsigniaID(), categoriaInicio, categoriaFin, entity.getTipo());
  }
}

