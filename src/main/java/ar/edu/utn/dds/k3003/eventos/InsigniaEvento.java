package ar.edu.utn.dds.k3003.eventos;

public record InsigniaEvento(
    String accion,
    String id,
    String nombre,
    String descripcion,
    String ocurridoEn) {}
