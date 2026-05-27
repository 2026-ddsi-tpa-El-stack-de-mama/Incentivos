package ar.edu.utn.dds.k3003.config;

import java.io.File;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogDirectoryInitializer {

  private static final Logger logger = LoggerFactory.getLogger(LogDirectoryInitializer.class);

  @PostConstruct
  public void ensureLogDirectoryExists() {
    try {
      File dir = new File("logs");
      if (!dir.exists()) {
        boolean ok = dir.mkdirs();
        if (ok) {
          logger.info("Created logs directory: {}", dir.getAbsolutePath());
        } else {
          logger.warn("Could not create logs directory: {}", dir.getAbsolutePath());
        }
      }
    } catch (Exception e) {
      logger.warn("Error ensuring logs directory exists: {}", e.getMessage());
    }
  }

}


