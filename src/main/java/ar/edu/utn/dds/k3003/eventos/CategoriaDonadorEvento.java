package ar.edu.utn.dds.k3003.eventos;

public record CategoriaDonadorEvento(
    String accion,
    String donadorId,
    String categoriaAnterior,
    String categoriaNueva,
    String motivo,
    String ocurridoEn) {}
