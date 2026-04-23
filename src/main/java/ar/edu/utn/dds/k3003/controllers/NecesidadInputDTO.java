package ar.edu.utn.dds.k3003.controllers;

public record NecesidadInputDTO(String entidadID, Integer nivelDeUrgencia, String descripcion, String productoSolicitado, Integer cantidadObjetivo) {
}
