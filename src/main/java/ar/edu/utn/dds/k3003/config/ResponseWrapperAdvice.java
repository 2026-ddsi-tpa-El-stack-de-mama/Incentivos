package ar.edu.utn.dds.k3003.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true; // apply to all responses
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    String requestId = MDC.get("request_id");
    if (requestId != null && !response.getHeaders().containsKey("X-Request-Id")) {
      response.getHeaders().add("X-Request-Id", requestId);
    }

    // If body already contains request_id, leave it
    if (body instanceof Map) {
      Map map = (Map) body;
      if (map.containsKey("request_id")) {
        return body;
      }
      map.put("request_id", requestId);
      return map;
    }

    // Special case for String responses: return JSON string
    if (body instanceof String) {
      try {
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("data", body);
        wrapper.put("request_id", requestId);
        return mapper.writeValueAsString(wrapper);
      } catch (Exception e) {
        return body; // fallback
      }
    }

    Map<String, Object> wrapper = new LinkedHashMap<>();
    wrapper.put("data", body);
    wrapper.put("request_id", requestId);
    return wrapper;
  }
}


