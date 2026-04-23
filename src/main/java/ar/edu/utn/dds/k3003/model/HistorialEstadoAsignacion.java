package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;

import java.time.LocalDateTime;

public class HistorialEstadoAsignacion {
        private String asignacionID; // Relación ManyToOne
        private EstadoAsginacionEnum estado;
        private LocalDateTime fechaCambio;

        public HistorialEstadoAsignacion(String asignacionID, EstadoAsginacionEnum estado, LocalDateTime fechaCambio) {
                this.asignacionID = asignacionID;
                this.estado = estado;
                this.fechaCambio = fechaCambio;
        }

}

