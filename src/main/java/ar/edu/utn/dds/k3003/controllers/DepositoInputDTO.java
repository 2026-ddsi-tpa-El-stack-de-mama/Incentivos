package ar.edu.utn.dds.k3003.controllers;


public record DepositoInputDTO(
    String nombre,
    String direccion,
    Integer capacidadMaxima
) {}
