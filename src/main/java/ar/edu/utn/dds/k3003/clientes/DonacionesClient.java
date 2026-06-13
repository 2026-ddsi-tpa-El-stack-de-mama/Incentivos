package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "donaciones", url = "${FACHADA_DONACIONES}")
public interface DonacionesClient {

  @GetMapping("/donaciones/search")
  List<DonacionDTO> buscarPorDonadorYFechaInicio(
      @RequestParam String donadorID,
      @RequestParam LocalDate fecha
  );

  @GetMapping("/productos/{id}")
  ProductoDTO buscarProductoPorID(@PathVariable("id") String productoID);
}