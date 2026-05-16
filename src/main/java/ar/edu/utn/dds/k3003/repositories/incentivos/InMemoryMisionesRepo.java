package ar.edu.utn.dds.k3003.repositories.incentivos;
import ar.edu.utn.dds.k3003.model.incentivos.Mision;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class InMemoryMisionesRepo {
  private static final InMemoryMisionesRepo INSTANCE = new InMemoryMisionesRepo();
  private final Map<String, Mision> misiones = new LinkedHashMap<>();
  private InMemoryMisionesRepo() {}
  public static InMemoryMisionesRepo getInstance() { return INSTANCE; }
  public synchronized void clear() { misiones.clear(); }
  public synchronized Mision save(Mision mision) {
    if (mision == null) throw new IllegalArgumentException("Mision nula");
    String id = mision.getId();
    if (id == null || id.isBlank()) { id = UUID.randomUUID().toString(); mision.setId(id); }
    misiones.put(id, mision);
    return misiones.get(id);
  }
  public synchronized Optional<Mision> findById(String id) { return Optional.ofNullable(misiones.get(id)); }
  public synchronized List<Mision> findAll() { return new ArrayList<>(misiones.values()); }
  public synchronized void deleteById(String id) {
    var removed = misiones.remove(id);
    if (removed == null) throw new NoSuchElementException("Mision inexistente");
  }
  public synchronized boolean existsById(String id) { return misiones.containsKey(id); }
}
