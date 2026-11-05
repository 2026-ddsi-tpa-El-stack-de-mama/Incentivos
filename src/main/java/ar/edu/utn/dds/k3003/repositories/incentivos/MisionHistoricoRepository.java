package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MisionHistoricoRepository extends JpaRepository<MisionHistorico, Long> {

  @Query("""
      SELECT h FROM MisionHistorico h
      JOIN FETCH h.mision
      WHERE h.donadorId = :donadorId
      """)
  List<MisionHistorico> findByDonadorId(@Param("donadorId") UUID donadorId);

  @Query("""
      SELECT h FROM MisionHistorico h
      JOIN FETCH h.mision
      WHERE h.donadorId = :donadorId
      ORDER BY h.fechaInicio DESC
      """)
  List<MisionHistorico> findByDonadorIdOrderByFechaInicioDesc(@Param("donadorId") UUID donadorId);

  @Query("""
    SELECT h FROM MisionHistorico h
    JOIN FETCH h.mision m
    LEFT JOIN FETCH m.insignia
    WHERE h.estado = :estado AND m.tipo = :tipo
    """)
  List<MisionHistorico> findByEstadoAndMision_Tipo(
      @Param("estado") MisionHistorico.EstadoMision estado,
      @Param("tipo") TipoMisionEnum tipo);
}