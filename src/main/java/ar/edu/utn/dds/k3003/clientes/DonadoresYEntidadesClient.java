package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.DonadorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "donadoresYEntidades", url = "${FACHADA_DYE}")
public interface DonadoresYEntidadesClient {

  @GetMapping("/donadores/{id}")
  DonadorDTO obtenerDonador(@PathVariable String id);

  @PatchMapping("/donadores/{id}/categoria")
  void modificarCategoria(@PathVariable String id, @RequestParam String categoria);
}