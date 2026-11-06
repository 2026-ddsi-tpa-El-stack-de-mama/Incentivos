package ar.edu.utn.dds.k3003.queue;

public record EventoQueue(
    String id,
    String tipoEvento,
    Object descripcion) {}
