package ar.edu.utn.dds.k3003.model.incentivos;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import ar.edu.utn.dds.k3003.catedra.ClassFinder;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EstadoDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.CategoriaDonadorEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.InsigniaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.incentivos.MisionDTO;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ExtraIncentivosTest {

  FachadaIncentivos instancia;

  @Mock FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  @Mock FachadaDonaciones fachadaDonaciones;

  InsigniaDTO insigniaEjemplo;
  MisionDTO misionDonacionesExitosas;
  MisionDTO misionCompletitud;
  MisionDTO misionSiguiente;
  DonadorDTO donadorEjemplo;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    var clazz = ClassFinder.findClass();
    instancia = (FachadaIncentivos) clazz.getDeclaredConstructor().newInstance();

    instancia.setFachadaDonadoresYEntidades(fachadaDonadoresYEntidades);
    instancia.setFachadaDonaciones(fachadaDonaciones);

    insigniaEjemplo = new InsigniaDTO(null, "insignia1", "descripcion1");
    // misiones creadas en cada test usando el id real de la insignia
    misionDonacionesExitosas = null;
    misionCompletitud = null;
    misionSiguiente = null;

    donadorEjemplo = new DonadorDTO(
        "donador1",
        "nombre",
        "apellido",
        30,
        "email@test",
        "123",
        "domicilio",
        EstadoDonadorEnum.VERIFICADO,
        "OCASIONAL");
  }

  @Test
  void procesarDonador_otorgaInsignia_paraDonacionesExitosas() {
    String id = donadorEjemplo.id();
    when(fachadaDonadoresYEntidades.buscarDonadorPorID(id)).thenReturn(donadorEjemplo);

    InsigniaDTO i = instancia.agregarInsignia(insigniaEjemplo);
    // crear la mision usando el id real de la insignia
    MisionDTO m = new MisionDTO(null, "Donaciones Exitosas", i.id(), CategoriaDonadorEnum.COLABORADOR, CategoriaDonadorEnum.TRANSFORMADOR);
    m = instancia.agregarMision(m);
    instancia.asignarMisionADonador(id, m);

    // crear 20 donaciones aceptadas
    List<DonacionDTO> donaciones = new ArrayList<>();
    for (int k = 0; k < 20; k++) {
      donaciones.add(new DonacionDTO("d" + k, id, "dep1", "desc", "prod", 1, EstadoDonacionEnum.ACEPTADA));
    }

    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(id), any())).thenReturn(donaciones);

    instancia.procesarDonador(id);

    List<InsigniaDTO> resultado = instancia.getInsigniasDeDonador(id);
    Assertions.assertNotNull(resultado);
    Assertions.assertTrue(resultado.stream().anyMatch(x -> x.id().equals(i.id())));

    // mision debe haberse removido
    Assertions.assertNull(instancia.getMisionEnCursoDeDonador(id));
  }

  @Test
  void procesarDonador_otorgaInsignia_paraCompletitud_y_desbloqueaSiguiente() {
    String id = donadorEjemplo.id();

    // primer llamada devuelve donador con categoria OCASIONAL, segunda devuelve con COLABORADOR
    DonadorDTO donInicial = new DonadorDTO(donadorEjemplo.id(), donadorEjemplo.nombre(), donadorEjemplo.apellido(), donadorEjemplo.edad(), donadorEjemplo.email(), donadorEjemplo.nroDocumento(), donadorEjemplo.domicilio(), donadorEjemplo.estado(), "OCASIONAL");
    DonadorDTO donLuego = new DonadorDTO(donadorEjemplo.id(), donadorEjemplo.nombre(), donadorEjemplo.apellido(), donadorEjemplo.edad(), donadorEjemplo.email(), donadorEjemplo.nroDocumento(), donadorEjemplo.domicilio(), donadorEjemplo.estado(), "COLABORADOR");

    when(fachadaDonadoresYEntidades.buscarDonadorPorID(id)).thenReturn(donInicial, donLuego);

    InsigniaDTO i = instancia.agregarInsignia(insigniaEjemplo);
     // crear misiones con la id real de la insignia
    MisionDTO mA = new MisionDTO(null, "Completitud", i.id(), CategoriaDonadorEnum.OCASIONAL, CategoriaDonadorEnum.COLABORADOR);
    MisionDTO mB = new MisionDTO(null, "misionSiguiente", i.id(), CategoriaDonadorEnum.COLABORADOR, null);
    mA = instancia.agregarMision(mA);
    mB = instancia.agregarMision(mB);
    instancia.asignarMisionADonador(id, mA);

    // crear 3 donaciones aceptadas a depositos distintos
    List<DonacionDTO> donaciones = List.of(
        new DonacionDTO("d1", id, "dep1", "desc", "prod", 1, EstadoDonacionEnum.ACEPTADA),
        new DonacionDTO("d2", id, "dep2", "desc", "prod", 1, EstadoDonacionEnum.ACEPTADA),
        new DonacionDTO("d3", id, "dep3", "desc", "prod", 1, EstadoDonacionEnum.ACEPTADA)
    );

    when(fachadaDonaciones.buscarPorDonadorYFechaInicio(eq(id), any())).thenReturn(donaciones);

    instancia.procesarDonador(id);

    // despues de procesar, debe tener la mision siguiente (mB) asignada
    MisionDTO actual = instancia.getMisionEnCursoDeDonador(id);
    Assertions.assertNotNull(actual);
    Assertions.assertEquals(mB.id(), actual.id());
  }

  @Test
  void procesarDonador_lanzaSiDonadorInexistente() {
    when(fachadaDonadoresYEntidades.buscarDonadorPorID("Inexistente")).thenThrow(new DonadorNoEncontradoException("No existe"));

    Assertions.assertThrows(RuntimeException.class, () -> instancia.procesarDonador("Inexistente"));
  }

  @Test
  void asignarInsignia_noDuplica_siYaTiene() {
    String id = donadorEjemplo.id();
    when(fachadaDonadoresYEntidades.buscarDonadorPorID(id)).thenReturn(donadorEjemplo);

    InsigniaDTO i = instancia.agregarInsignia(insigniaEjemplo);
    instancia.asignarInsigniaADonador(id, i);
    // asignar de nuevo
    instancia.asignarInsigniaADonador(id, i);

    List<InsigniaDTO> res = instancia.getInsigniasDeDonador(id);
    Assertions.assertNotNull(res);
    // asegurar que no hay duplicados (solo un elemento con ese id)
    long count = res.stream().filter(x -> x.id().equals(i.id())).count();
    Assertions.assertEquals(1, count);
  }

  @Test
  void getInsigniasDeDonador_devuelveListaVacia_siSinInsignias() {
    String id = donadorEjemplo.id();
    when(fachadaDonadoresYEntidades.buscarDonadorPorID(id)).thenReturn(donadorEjemplo);

    List<InsigniaDTO> res = instancia.getInsigniasDeDonador(id);
    Assertions.assertNotNull(res);
    Assertions.assertTrue(res.isEmpty());
  }

  @Test
  void getMisionEnCursoDeDonador_devuelveNull_siSinMision() {
    String id = donadorEjemplo.id();
    when(fachadaDonadoresYEntidades.buscarDonadorPorID(id)).thenReturn(donadorEjemplo);

    MisionDTO res = instancia.getMisionEnCursoDeDonador(id);
    Assertions.assertNull(res);
  }
}

