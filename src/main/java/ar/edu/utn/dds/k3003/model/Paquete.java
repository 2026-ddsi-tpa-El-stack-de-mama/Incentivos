package ar.edu.utn.dds.k3003.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paquete {
    private String paqueteID;
    private String donacionID;
    private String productoID;
    private Integer cantidad;
    private String entidadAsignadaID;
}
