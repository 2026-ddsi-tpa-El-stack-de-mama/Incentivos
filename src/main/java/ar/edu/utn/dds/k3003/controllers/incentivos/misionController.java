package ar.edu.utn.dds.k3003.controllers.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import ar.edu.utn.dds.k3003.services.incentivos.misionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class misionController {

  private misionService misionService;

  @Autowired
  public misionController(misionService misionService) {
    this.misionService = misionService;
  }


  @GetMapping("/misiones")
  public List<Mision> getMisiones () {
    return misionService.getMisiones();
  }



}
