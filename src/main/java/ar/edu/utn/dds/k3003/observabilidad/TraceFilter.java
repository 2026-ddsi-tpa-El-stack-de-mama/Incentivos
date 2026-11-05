package ar.edu.utn.dds.k3003.observabilidad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Abre el contexto de traza de cada request HTTP entrante y registra entrada y salida.
 * Reemplaza al RequestIdFilter anterior. Corre primero para que los logs de cualquier otro filtro
 * ya lleven traceId.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TraceFilter.class);

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Health checks y pingers golpean /actuator todo el tiempo: ruido y consumo de cuota.
    return request.getRequestURI().startsWith("/actuator");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    try (TraceContext.Scope ignored = TraceContext.desdeRequest(
        request.getHeader(TraceContext.TRACE_HEADER))) {
      // Se devuelve al cliente para poder buscar esta operacion en el log centralizado.
      response.setHeader(TraceContext.TRACE_HEADER, TraceContext.traceId());
      long inicio = System.currentTimeMillis();
      // Solo URI: nunca query string ni cuerpo (pueden contener datos personales).
      log.info("--> {} {}", request.getMethod(), request.getRequestURI());
      try {
        chain.doFilter(request, response);
      } finally {
        long ms = System.currentTimeMillis() - inicio;
        int status = response.getStatus();
        if (status >= 500) {
          log.warn("<-- {} {} status={} took={}ms",
              request.getMethod(), request.getRequestURI(), status, ms);
        } else {
          log.info("<-- {} {} status={} took={}ms",
              request.getMethod(), request.getRequestURI(), status, ms);
        }
      }
    }
  }
}
