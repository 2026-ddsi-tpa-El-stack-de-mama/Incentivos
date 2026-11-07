package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MisionRepository extends JpaRepository<Mision, UUID> {

  @Query("SELECT m FROM Mision m LEFT JOIN FETCH m.insignia WHERE m.id = :id")
  Optional<Mision> findConInsigniaById(@Param("id") UUID id);

  List<Mision> findByInsignia_Id(UUID insigniaId);

  boolean existsByInsignia_Id(UUID insigniaId);
}

