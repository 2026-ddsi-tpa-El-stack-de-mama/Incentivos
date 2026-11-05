package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.CambioCategoriaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CambioCategoriaHistoricoRepository extends JpaRepository<CambioCategoriaHistorico, Long> {
  List<CambioCategoriaHistorico> findByDonadorIdOrderByFechaDesc(UUID donadorId);
}