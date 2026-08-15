package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MisionHistoricoRepository extends JpaRepository<MisionHistorico, Long> {
  List<MisionHistorico> findByDonadorId(UUID donadorId);
  List<MisionHistorico> findByDonadorIdOrderByFechaInicioDesc(UUID donadorId);
  List<MisionHistorico> findByEstadoAndMision_Tipo(
      MisionHistorico.EstadoMision estado,
      TipoMisionEnum tipo);
}

