package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;

public record MisionEvento(
    String accion,
    String id,
    String nombre,
    String insigniaId,
    CategoriaDonadorEnum categoriaInicio,
    CategoriaDonadorEnum categoriaFin,
    TipoMisionEnum tipo,
    String ocurridoEn) {}
