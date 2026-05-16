package ar.edu.utn.dds.k3003.repositories.incentivos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class InMemoryAsignacionesIncentivosRepo {
  private static final InMemoryAsignacionesIncentivosRepo INSTANCE = new InMemoryAsignacionesIncentivosRepo();
  private final Map<String, List<String>> insigniasPorDonador = new HashMap<>();
  private final Map<String, String> misionPorDonador = new HashMap<>();
  private final Map<String, List<String>> categoriasPorDonador = new HashMap<>();
  private InMemoryAsignacionesIncentivosRepo() {}
  public static InMemoryAsignacionesIncentivosRepo getInstance() { return INSTANCE; }
  public synchronized void clear() {
    insigniasPorDonador.clear();
    misionPorDonador.clear();
    categoriasPorDonador.clear();
  }
  public synchronized List<String> getInsignias(String donadorID) {
    var lista = insigniasPorDonador.get(donadorID);
    return lista == null ? null : new ArrayList<>(lista);
  }
  public synchronized void setInsignia(String donadorID, String insigniaID) {
    var lista = insigniasPorDonador.computeIfAbsent(donadorID, k -> new ArrayList<>());
    if (!lista.contains(insigniaID)) {
      lista.add(insigniaID);
    }
  }
  public synchronized void removeInsignia(String insigniaID) {
    insigniasPorDonador.values().forEach(list -> list.removeIf(id -> id.equals(insigniaID)));
  }
  public synchronized String getMision(String donadorID) { return misionPorDonador.get(donadorID); }
  public synchronized void setMision(String donadorID, String misionID) { misionPorDonador.put(donadorID, misionID); }
  public synchronized void removeMision(String donadorID) { misionPorDonador.remove(donadorID); }
  public synchronized void removeMisionById(String misionID) { misionPorDonador.values().removeIf(id -> id.equals(misionID)); }
  public synchronized void agregarCategoriaAnterior(String donadorID, String categoriaAnterior) {
    if (categoriaAnterior == null) return;
    categoriasPorDonador.computeIfAbsent(donadorID, k -> new ArrayList<>()).add(categoriaAnterior);
  }
  public synchronized List<String> historialCategorias(String donadorID) {
    var lista = categoriasPorDonador.get(donadorID);
    return lista == null ? List.of() : List.copyOf(lista);
  }
}
