package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;

public record MisionDonadorEvento(
    String accion,
    Long historicoId,
    String donadorId,
    String misionId,
    String nombre,
    TipoMisionEnum tipo,
    CategoriaDonadorEnum categoriaInicio,
    CategoriaDonadorEnum categoriaFin,
    String estado,
    String fechaInicio,
    String fechaFin,
    String ocurridoEn) {}
