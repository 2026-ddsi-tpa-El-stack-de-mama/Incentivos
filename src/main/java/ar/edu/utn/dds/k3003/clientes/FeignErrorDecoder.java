package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.exceptions.ExternalBadRequestException;
import ar.edu.utn.dds.k3003.exceptions.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    return switch (response.status()) {
      case 400, 404 -> new ExternalBadRequestException(
          "Recurso no encontrado o solicitud inválida: " + response.status()
      );
      case 500, 503 -> new ExternalServiceException(
          "Error en servicio externo: " + response.status()
      );
      default -> new ExternalServiceException(
          "Error inesperado: " + response.status()
      );
    };
  }
}