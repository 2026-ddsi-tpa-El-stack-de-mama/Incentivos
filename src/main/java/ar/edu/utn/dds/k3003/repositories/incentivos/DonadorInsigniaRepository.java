package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.DonadorInsignia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DonadorInsigniaRepository extends JpaRepository<DonadorInsignia, Long> {
  List<DonadorInsignia> findByDonadorId(UUID donadorId);
  boolean existsByDonadorIdAndInsigniaId(UUID donadorId, UUID insigniaId);
  void deleteByInsigniaId(UUID insigniaId);
}

