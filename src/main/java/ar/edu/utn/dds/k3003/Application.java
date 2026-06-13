package ar.edu.utn.dds.k3003;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableFeignClients(basePackages = "ar.edu.utn.dds.k3003.clientes")
public class Application {

  private static final Logger log =
      LoggerFactory.getLogger(Application.class);

  @Value("${server.port:8080}")
  private String port;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onReady() {

    log.info("""
        
        =========================================
        Aplicación iniciada correctamente
        URL: http://localhost:{}{}
        =========================================
        """, port, contextPath);
  }
}