package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.exceptions.DepositoNoEncotradoException;
import ar.edu.utn.dds.k3003.repositories.asignaciones.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.depositos.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.donaciones.DonacionRepository;
import ar.edu.utn.dds.k3003.repositories.necesidades.NecesidadesRepository;
import ar.edu.utn.dds.k3003.repositories.necesidades.PrioridadSubAtendidos;
import ar.edu.utn.dds.k3003.repositories.paquetes.PaquetesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FachadaLogisticaTest {

    @Mock
    FachadaDonaciones fachadaDonaciones;

    @Mock
    FachadaDonadoresYEntidades fachadaDonadoresYEntidades;

    private Fachada fachadaLogistica;
    private DepositoRepository mockDepositoRepo;
    private NecesidadesRepository mockNecesidadesRepo;
    private PaquetesRepository mockPaquetesRepo;
    private DonacionRepository mockDonacionRepo;
    private AsignacionRepository mockAsignacionRepo;

    @BeforeEach
    public void setUp() {
        mockDepositoRepo = mock(DepositoRepository.class);
        mockNecesidadesRepo = mock(NecesidadesRepository.class);
        mockPaquetesRepo = mock(PaquetesRepository.class);
        mockDonacionRepo = mock(DonacionRepository.class);
        mockAsignacionRepo = mock(AsignacionRepository.class);

        fachadaDonaciones = mock(FachadaDonaciones.class);
        fachadaDonadoresYEntidades = mock(FachadaDonadoresYEntidades.class);

        fachadaLogistica = new Fachada(mockDepositoRepo, mockNecesidadesRepo, mockPaquetesRepo, mockDonacionRepo, mockAsignacionRepo);
        this.fachadaLogistica.setFachadaDonaciones(fachadaDonaciones);
        this.fachadaLogistica.setFachadaDonadoresYEntidades(fachadaDonadoresYEntidades);
        this.fachadaLogistica.setAlgoritmoMM("", TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE);

    }

    @Test
    public void testAgregarDeposito_Success() {
        DepositoDTO depositoDTO = new DepositoDTO("123", null,  "depo1", "Address1", 30, null);
        when(mockDepositoRepo.findById(depositoDTO.id())).thenReturn(Optional.empty());
        when(mockDepositoRepo.save(any())).thenReturn(new Deposito("123", "depo1", "Address1", 30, null));

        DepositoDTO result = fachadaLogistica.agregarDeposito(depositoDTO);

        assertNotNull(result);
        assertEquals("123", result.id());
        verify(mockDepositoRepo, times(1)).save(any());
    }
    

    @Test
    public void testBuscarDepositoPorID_Success() {
        when(mockDepositoRepo.findById("123")).thenReturn(Optional.of(new Deposito("123", "depo1", "Address1", 30, null)));

        DepositoDTO result = fachadaLogistica.buscarDepositoPorID("123");

        assertNotNull(result);
        assertEquals("123", result.id());
    }

    @Test
    public void testBuscarDepositoPorID_NotFound() {
        when(mockDepositoRepo.findById("123")).thenReturn(Optional.empty());

        assertThrows(DepositoNoEncotradoException.class, () -> fachadaLogistica.buscarDepositoPorID("123"));
    }

    @Test
    public void testBuscarAsignacionPorPaqueteID_Success() {
        Asignacion mockAsignacion = new Asignacion("1", "paquete1", "necesidad1", LocalDateTime.now(), null);
        when(mockAsignacionRepo.findAsignacionByPaqueteId("paquete1")).thenReturn(Optional.of(mockAsignacion));

        AsignacionDTO result = fachadaLogistica.buscarAsignacionPorPaqueteID("paquete1");

        assertNotNull(result);
        assertEquals("paquete1", result.paqueteID());
    }

    @Test
    public void testBuscarAsignacionPorPaqueteID_NotFound() {
        AsignacionRepository mockAsignacionRepo = mock(AsignacionRepository.class);
        when(mockAsignacionRepo.findAsignacionByPaqueteId("paquete2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fachadaLogistica.buscarAsignacionPorPaqueteID("paquete2"));
    }

    @Test
    public void testGestionarDonacion_Success() {
        // 1. Datos de entrada
        String depoId = "depo1";
        String prodId = "prod1";
        Integer cant = 10;

        // 2. Mockear Necesidad (Matchmaking)
        Necesidad mockNec = new Necesidad("nec1", "12", 1, "desc", 30, prodId, 2, TipoNecesidadMaterialEnum.EXTRAORDINARIA);
        when(mockNecesidadesRepo.doesNecesityExist(prodId)).thenReturn(true);

        NecesidadMaterialDTO necesidadDeEntidadDTO = new NecesidadMaterialDTO(mockNec.getId(), mockNec.getEntidadID(), mockNec.getNivelDeUrgencia(), mockNec.getDescripcion(), mockNec.getCantidadObjetivo(), mockNec.getProductoID(), mockNec.getTipoNecesidad());

        when(fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(prodId))
                .thenReturn(List.of(new NecesidadMaterialDTO(mockNec.getId(), mockNec.getEntidadID(), mockNec.getNivelDeUrgencia(), mockNec.getDescripcion(), mockNec.getCantidadObjetivo(), mockNec.getProductoID(), mockNec.getTipoNecesidad())));

        // 3. Mockear Depósito
        Deposito mockDepo = new Deposito(depoId, "Nombre", "Direccion", 100, null);
        when(mockDepositoRepo.findById(depoId)).thenReturn(Optional.of(mockDepo));

        when(mockNecesidadesRepo.doesNecesityExist(prodId)).thenReturn(true);
        when(mockNecesidadesRepo.findById("nec1")).thenReturn(Optional.of(mockNec));

        // 4. Mockear persistencia de los nuevos objetos
        when(mockDonacionRepo.save(any())).thenReturn(new Donacion("don1", "", depoId, "", prodId, cant, null));
        when(mockPaquetesRepo.save(any())).thenReturn(new Paquete("paq1", "don1", prodId, cant, "entidad1"));
        when(mockAsignacionRepo.save(any())).thenReturn(new Asignacion("asig1", "paq1", "nec1", LocalDateTime.now(), EstadoAsginacionEnum.ASIGNADA));

        // 5. Ejecución
        DepositoDTO result = fachadaLogistica.gestionarDonacion(depoId, "don1", prodId, cant);

        // 6. Verificaciones
        assertNotNull(result);
        assertEquals(depoId, result.id());
        verify(mockDonacionRepo).save(any());
        verify(mockPaquetesRepo).save(any());
        verify(mockAsignacionRepo).save(any());
    }

    @Test
    public void testGestionarDonacion_InvalidQuantity() {
        assertThrows(RuntimeException.class, () -> fachadaLogistica.gestionarDonacion("depo1", "don1", "prod1", -1));

    }

    /*
    @Test
    public void testReportarEntrega_Success() {
        // 1. Setup de datos
        String paqId = "paq1";
        String donId = "don1";
        PaqueteDTO paqDto = new PaqueteDTO(paqId, donId, "prod1", 30);

        Asignacion mockAsig = new Asignacion("asig1", paqId, "nec1", LocalDateTime.now(), EstadoAsginacionEnum.ASIGNADA);
        Donacion mockDon = new Donacion(donId, "donador1", "depo1", "entidad1", "prod1", 10, EstadoDonacionEnum.INGRESADA);

        // 2. Mockear búsquedas
        when(mockAsignacionRepo.findAsignacionByPaqueteId(paqId)).thenReturn(Optional.of(mockAsig));
        //when(mockDonacionRepo.findById(donId)).thenReturn(Optional.of(mockDon));

        // 3. Ejecución
        fachadaLogistica.reportarEntrega(paqDto);

        // 4. Verificar cambios de estado y persistencia
        assertEquals(EstadoAsginacionEnum.COMPLETADA, mockAsig.getEstado());
        //assertEquals(EstadoDonacionEnum.ACEPTADA, mockDon.getEstado());

        verify(mockAsignacionRepo).modifyById(eq(mockAsig.getId()), any());
        //verify(mockDonacionRepo).modifyById(eq(mockDon.getId()), any());
    } */




    @Test
    public void testEjecutarAlgoritmoMatchmaking_Success() {
        // 1. Datos de entrada
        String prodId = "prod1";

        // 2. Mockear Necesidad
        Necesidad mockNec1 = new Necesidad("nec1", "ent1", 1, "desc1", 50, prodId, 30, TipoNecesidadMaterialEnum.EXTRAORDINARIA);
        Necesidad mockNec2 = new Necesidad("nec2", "ent2", 2, "desc2", 100, prodId, 70, TipoNecesidadMaterialEnum.EXTRAORDINARIA);
        when(mockNecesidadesRepo.findAll()).thenReturn(List.of(mockNec1, mockNec2));

        fachadaLogistica.setAlgoritmoMM("", TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE);

        NecesidadMaterialDTO necesidadDeEntidadDTO1 = new NecesidadMaterialDTO(
                mockNec1.getId(),
                mockNec1.getEntidadID(),
                mockNec1.getNivelDeUrgencia(),
                mockNec1.getDescripcion(),
                mockNec1.getCantidadObjetivo(),
                mockNec1.getProductoID(),
                mockNec1.getTipoNecesidad()

        );

        NecesidadMaterialDTO necesidadDeEntidadDTO2 = new NecesidadMaterialDTO(
                mockNec2.getId(),
                mockNec2.getEntidadID(),
                mockNec2.getNivelDeUrgencia(),
                mockNec2.getDescripcion(),
                mockNec2.getCantidadObjetivo(),
                mockNec2.getProductoID(),
                mockNec1.getTipoNecesidad()

        );



        Asignacion mockAsig = new Asignacion("asig1", "paq1", "nec1", LocalDateTime.now(), EstadoAsginacionEnum.ASIGNADA);
        when(mockAsignacionRepo.save(any())).thenReturn(mockAsig);

        PaqueteDTO paquete = new PaqueteDTO("1", "don1", prodId, 60);



        // 4. Ejecución
        AsignacionDTO asignacion = fachadaLogistica.ejecutarMatchmaking("", paquete, List.of(necesidadDeEntidadDTO1, necesidadDeEntidadDTO2));



        // 5. Verificaciones
        assertNotNull(asignacion);
        assertEquals("nec1", asignacion.necesidadID());
    }
}
