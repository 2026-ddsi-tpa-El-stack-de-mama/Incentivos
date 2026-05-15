package ar.edu.utn.dds.k3003.services.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class misionService {


  private List<Mision> misionesList;

  public misionService() {
    this.misionesList = List.of(
        new Mision("1", "Mision 1", "Descripcion de la mision 1"),
        new Mision("2", "Mision 2", "Descripcion de la mision 2"),
        new Mision("3", "Mision 3", "Descripcion de la mision 3")
    );
  }

  public List<Mision> getMisiones() {
    return misionesList;
  }
}
