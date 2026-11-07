package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import java.util.List;

/**
 * Foto completa de los incentivos de un donador: todas sus insignias y su mision en curso. La lista
 * de misiones viene vacia si no tiene ninguna activa, y en ese caso el progreso viene en null.
 */
public record MisionInsigniaEvento(
    String donadorId,
    List<InsigniaDTO> insignias,
    List<MisionDTO> misiones,
    Integer progresoActual,
    Integer progresoRequerido,
    String ocurridoEn) {}
