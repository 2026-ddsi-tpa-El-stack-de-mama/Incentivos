package ar.edu.utn.dds.k3003.observabilidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TraceContextTest {

  @Test
  void reutilizaTraceIdEntranteValido() {
    try (TraceContext.Scope ignored = TraceContext.desdeRequest("abc12345-xyz")) {
      assertEquals("abc12345-xyz", TraceContext.traceId());
      assertNotNull(TraceContext.requestId());
    }
    assertNull(TraceContext.traceId());
  }

  @Test
  void descartaTraceIdInvalidoYGeneraUnoNuevo() {
    String malicioso = "x\nFAKE LOG LINE";
    try (TraceContext.Scope ignored = TraceContext.desdeRequest(malicioso)) {
      assertNotEquals(malicioso, TraceContext.traceId());
      assertTrue(TraceContext.esTraceIdValido(TraceContext.traceId()));
    }
  }

  @Test
  void rechazaValoresCortosLargosYNulos() {
    assertFalse(TraceContext.esTraceIdValido(null));
    assertFalse(TraceContext.esTraceIdValido("corto"));
    assertFalse(TraceContext.esTraceIdValido("a".repeat(65)));
  }

  @Test
  void tareaHeredaLaTrazaActivaYNoLaLimpia() {
    try (TraceContext.Scope externo = TraceContext.tarea()) {
      String trace = TraceContext.traceId();
      try (TraceContext.Scope interno = TraceContext.tarea()) {
        assertEquals(trace, TraceContext.traceId());
      }
      assertEquals(trace, TraceContext.traceId());
    }
    assertNull(TraceContext.traceId());
  }
}
