package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.DonadorMision;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonadorMisionRepository extends JpaRepository<DonadorMision, UUID> {

  @Query("""
      SELECT dm FROM DonadorMision dm
      JOIN FETCH dm.mision m
      LEFT JOIN FETCH m.insignia
      WHERE dm.donadorId = :donadorId
      """)
  Optional<DonadorMision> findByDonadorId(@Param("donadorId") UUID donadorId);

  void deleteByMisionId(UUID misionId);
}

