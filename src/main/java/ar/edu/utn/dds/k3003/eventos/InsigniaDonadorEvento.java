package ar.edu.utn.dds.k3003.eventos;

public record InsigniaDonadorEvento(
    String accion,
    String donadorId,
    String insigniaId,
    String nombre,
    String descripcion,
    String ocurridoEn) {}
