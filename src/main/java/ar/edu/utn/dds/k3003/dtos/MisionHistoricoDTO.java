package ar.edu.utn.dds.k3003.dtos;

import java.time.LocalDateTime;

public record MisionHistoricoDTO(
    String misionID,
    String misionNombre,
    String estado,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin
) {}