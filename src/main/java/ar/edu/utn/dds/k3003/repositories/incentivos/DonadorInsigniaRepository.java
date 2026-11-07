package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.DonadorInsignia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface DonadorInsigniaRepository extends JpaRepository<DonadorInsignia, Long> {
  List<DonadorInsignia> findByDonadorId(UUID donadorId);

  // Con la insignia cargada: los jobs no tienen sesion de Hibernate para resolver la relacion lazy.
  @Query("SELECT di FROM DonadorInsignia di JOIN FETCH di.insignia WHERE di.donadorId = :donadorId")
  List<DonadorInsignia> findConInsigniaByDonadorId(@Param("donadorId") UUID donadorId);

  @Query("SELECT di.donadorId FROM DonadorInsignia di WHERE di.insignia.id = :insigniaId")
  List<UUID> findDonadorIdsByInsigniaId(@Param("insigniaId") UUID insigniaId);

  boolean existsByDonadorIdAndInsigniaId(UUID donadorId, UUID insigniaId);

  @Transactional
  void deleteByInsigniaId(UUID insigniaId);

  @Transactional
  void deleteByDonadorIdAndInsigniaId(UUID donadorId, UUID insigniaId);
}