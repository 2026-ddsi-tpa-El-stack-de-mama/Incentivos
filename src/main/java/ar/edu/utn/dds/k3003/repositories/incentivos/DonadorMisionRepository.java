package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.DonadorMision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonadorMisionRepository extends JpaRepository<DonadorMision, UUID> {
  Optional<DonadorMision> findByDonadorId(UUID donadorId);
  void deleteByMisionId(UUID misionId);
}

