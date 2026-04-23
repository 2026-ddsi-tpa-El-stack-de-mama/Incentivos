package ar.edu.utn.dds.k3003.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entidad {
    private String id;
    private String razonSocial;
    private String domicilio;
    private String telefono;
    private String email;
    private List<Necesidad> necesidades;





}
