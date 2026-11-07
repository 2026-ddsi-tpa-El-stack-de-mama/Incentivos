package ar.edu.utn.dds.k3003.eventos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.TipoMisionEnum;
import ar.edu.utn.dds.k3003.model.incentivos.DonadorInsignia;
import ar.edu.utn.dds.k3003.model.incentivos.DonadorMision;
import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.model.incentivos.MisionHistorico;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import ar.edu.utn.dds.k3003.repositories.incentivos.DonadorInsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.DonadorMisionRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.InsigniaRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.MisionHistoricoRepository;
import ar.edu.utn.dds.k3003.repositories.incentivos.MisionRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Los jobs corren sin sesion de Hibernate: sin transaccion en el test, una relacion lazy sin cargar
 * rompe igual que en produccion.
 */
@DataJpaTest(properties = {
    "DATABASE_URL=jdbc:h2:mem:incentivos",
    "DB_USER=sa",
    "DB_PASSWORD=",
    "DD_API_KEY=x",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@ContextConfiguration(classes = MisionInsigniaEventoJpaTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MisionInsigniaEventoJpaTest {

  @Configuration
  @EntityScan("ar.edu.utn.dds.k3003.model.incentivos")
  @EnableJpaRepositories("ar.edu.utn.dds.k3003.repositories.incentivos")
  static class Config {}

  @Autowired InsigniaRepository insigniaRepository;
  @Autowired MisionRepository misionRepository;
  @Autowired DonadorInsigniaRepository donadorInsigniaRepository;
  @Autowired DonadorMisionRepository donadorMisionRepository;
  @Autowired MisionHistoricoRepository misionHistoricoRepository;

  @Test
  void armaLaFotoDelDonadorSinSesionDeHibernate() {
    PublicadorEventos eventos = mock(PublicadorEventos.class);
    Fachada fachada = new Fachada(insigniaRepository, misionRepository, donadorInsigniaRepository,
        donadorMisionRepository, null, null, null, null, null, eventos);
    UUID donador = UUID.randomUUID();
    Insignia insignia = insigniaRepository.save(new Insignia(null, "Constante", "Desc"));
    Mision mision = new Mision(null, "Exitosas", null, "OCASIONAL", "COLABORADOR",
        TipoMisionEnum.DONACIONES_EXITOSAS);
    mision.setInsignia(insignia);
    mision = misionRepository.save(mision);
    donadorInsigniaRepository.save(new DonadorInsignia(donador, insignia));
    donadorMisionRepository.save(new DonadorMision(donador, mision));

    fachada.publicarMisionInsigniaDeDonador(donador);

    ArgumentCaptor<EventoQueue> publicado = ArgumentCaptor.forClass(EventoQueue.class);
    verify(eventos).publicar(publicado.capture());
    var foto = (MisionInsigniaEvento) publicado.getValue().descripcion();
    assertEquals(donador.toString(), foto.donadorId());
    assertEquals("Constante", foto.insignias().get(0).nombre());
    assertEquals("Exitosas", foto.misiones().get(0).nombre());
    assertEquals(insignia.getId(), foto.misiones().get(0).insigniaID());
    assertEquals(Integer.valueOf(0), foto.progresoActual());
    assertEquals(Integer.valueOf(5), foto.progresoRequerido());
    assertEquals(List.of(donador),
        donadorInsigniaRepository.findDonadorIdsByInsigniaId(UUID.fromString(insignia.getId())));
    assertEquals(List.of(donador),
        donadorMisionRepository.findDonadorIdsByMisionId(UUID.fromString(mision.getId())));
  }

  @Test
  void modificarMisionConservaLaInsigniaSinSesionDeHibernate() {
    Fachada fachada = new Fachada(insigniaRepository, misionRepository, donadorInsigniaRepository,
        donadorMisionRepository, null, null, null, null, null, mock(PublicadorEventos.class));
    Insignia insignia = insigniaRepository.save(new Insignia(null, "Constante", "Desc"));
    Mision mision = new Mision(null, "Exitosas", null, "OCASIONAL", "COLABORADOR",
        TipoMisionEnum.DONACIONES_EXITOSAS);
    mision.setInsignia(insignia);
    mision = misionRepository.save(mision);

    MisionDTO modificada = fachada.modificarMision(new MisionDTO(mision.getId(), "Otro nombre", null,
        CategoriaDonadorEnum.OCASIONAL, CategoriaDonadorEnum.COLABORADOR, TipoMisionEnum.DONACIONES_EXITOSAS));

    assertEquals(insignia.getId(), modificada.insigniaID());
    assertEquals(1, misionRepository.findAll().stream()
        .filter(m -> "Otro nombre".equals(m.getNombre())).count());
  }

  @Test
  void eliminarInsigniaEnUsoPasaSusMisionesAOtra() {
    Fachada fachada = new Fachada(insigniaRepository, misionRepository, donadorInsigniaRepository,
        donadorMisionRepository, null, null, null, null, null, mock(PublicadorEventos.class));
    UUID donador = UUID.randomUUID();
    Insignia insignia = insigniaRepository.save(new Insignia(null, "Vieja", "Desc"));
    Insignia otra = insigniaRepository.save(new Insignia(null, "Otra", "Desc"));
    Mision mision = new Mision(null, "Exitosas", null, "OCASIONAL", "COLABORADOR",
        TipoMisionEnum.DONACIONES_EXITOSAS);
    mision.setInsignia(insignia);
    mision = misionRepository.save(mision);
    donadorInsigniaRepository.save(new DonadorInsignia(donador, insignia));

    fachada.eliminarInsignia(insignia.getId());

    UUID id = UUID.fromString(insignia.getId());
    assertEquals(true, insigniaRepository.findById(id).orElseThrow().isFueraDeServicio());
    assertEquals(0, donadorInsigniaRepository.findDonadorIdsByInsigniaId(id).size());
    assertEquals(0, misionRepository.findByInsignia_Id(id).size());
    assertEquals(mision.getId(), misionRepository
        .findByInsignia_Id(UUID.fromString(otra.getId())).get(0).getId());
  }

  @Test
  void eliminarMisionEnCursoConservaLaFilaYElHistorial() {
    Fachada fachada = new Fachada(insigniaRepository, misionRepository, donadorInsigniaRepository,
        donadorMisionRepository, misionHistoricoRepository, null, null, null, null,
        mock(PublicadorEventos.class));
    UUID donador = UUID.randomUUID();
    Mision mision = misionRepository.save(new Mision(null, "X", null, "OCASIONAL", "COLABORADOR",
        TipoMisionEnum.DONACIONES_EXITOSAS));
    donadorMisionRepository.save(new DonadorMision(donador, mision));
    misionHistoricoRepository.save(new MisionHistorico(donador, mision, MisionHistorico.EstadoMision.ACTIVA));

    fachada.eliminarMision(mision.getId());

    UUID id = UUID.fromString(mision.getId());
    assertEquals(true, misionRepository.findById(id).orElseThrow().isFueraDeServicio());
    assertEquals(false, donadorMisionRepository.findById(donador).isPresent());
    assertEquals(1, misionHistoricoRepository
        .findByMision_IdAndEstado(id, MisionHistorico.EstadoMision.CANCELADA).size());
  }
}
