package ar.edu.utn.dds.k3003.repositories.incentivos;

import ar.edu.utn.dds.k3003.model.incentivos.Insignia;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class InMemoryInsigniasRepo {

  private static final InMemoryInsigniasRepo INSTANCE = new InMemoryInsigniasRepo();

  private final Map<String, Insignia> insignias = new LinkedHashMap<>();

  private InMemoryInsigniasRepo() {}

  public static InMemoryInsigniasRepo getInstance() {
    return INSTANCE;
  }

  public synchronized void clear() {
    insignias.clear();
  }

  public synchronized Insignia save(Insignia insignia) {
    if (insignia == null) {
      throw new IllegalArgumentException("Insignia nula");
    }
    String id = insignia.getId();
    if (id == null || id.isBlank()) {
      id = UUID.randomUUID().toString();
      insignia.setId(id);
    }
    insignias.put(id, insignia);
    return insignias.get(id);
  }

  public synchronized Optional<Insignia> findById(String id) {
    return Optional.ofNullable(insignias.get(id));
  }

  public synchronized List<Insignia> findAll() {
    return new ArrayList<>(insignias.values());
  }

  public synchronized void deleteById(String id) {
    var removed = insignias.remove(id);
    if (removed == null) {
      throw new NoSuchElementException("Insignia inexistente");
    }
  }

  public synchronized boolean existsById(String id) {
    return insignias.containsKey(id);
  }
}


