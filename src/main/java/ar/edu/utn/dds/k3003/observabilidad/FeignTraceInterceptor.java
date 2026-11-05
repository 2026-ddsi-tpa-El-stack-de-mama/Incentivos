package ar.edu.utn.dds.k3003.observabilidad;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Copia el traceId del hilo actual al header de toda llamada saliente hecha con Feign.
 * Spring Cloud OpenFeign aplica los beans RequestInterceptor a todos los clientes, asi que
 * no hay que tocar cada interfaz @FeignClient.
 */
@Component
public class FeignTraceInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    String traceId = TraceContext.traceId();
    if (traceId != null) {
      template.header(TraceContext.TRACE_HEADER, traceId);
    }
  }
}
