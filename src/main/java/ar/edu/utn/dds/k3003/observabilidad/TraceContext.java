package ar.edu.utn.dds.k3003.observabilidad;

import org.slf4j.MDC;
import java.net.InetAddress;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Contexto de trazabilidad compartido por los componentes de DonaTrack.
 *
 * <p>Contrato comun (los cuatro modulos deben cumplirlo):
 * <ul>
 *   <li>{@code traceId}: identifica una operacion de negocio completa. Viaja en el header
 *       {@value #TRACE_HEADER} entre servicios. Nace en el punto de entrada.</li>
 *   <li>{@code requestId}: identifica un unico salto (una request en un servicio).</li>
 *   <li>{@code instanceId}: identifica el proceso (RENDER_INSTANCE_ID / INSTANCE_NAME / hostname).</li>
 * </ul>
 *
 * <p>El MDC es thread-local: solo es visible en el hilo que atiende la request. No se propaga
 * solo a hilos nuevos, colas ni tareas asincronas.
 */
public final class TraceContext {

  public static final String TRACE_HEADER = "X-Trace-Id";
  public static final String TRACE_ID = "traceId";
  public static final String REQUEST_ID = "requestId";
  public static final String INSTANCE_ID = "instanceId";

  /**
   * Clave historica de Incentivos ({@code MDC.get("request_id")} se usa en controllers, Fachada y
   * en el cuerpo de las respuestas). Se mantiene con el mismo valor que {@code requestId} para no
   * tocar ese codigo. Los otros modulos no la necesitan.
   */
  public static final String LEGACY_REQUEST_ID = "request_id";

  // Acepta ids de 8 a 64 caracteres seguros. Rechaza saltos de linea, espacios y valores enormes
  // (evita inyeccion de lineas falsas en el log y explosion de cardinalidad).
  private static final Pattern TRACE_ID_VALIDO = Pattern.compile("^[A-Za-z0-9_-]{8,64}$");

  private static final String INSTANCIA = resolverInstancia();

  private TraceContext() {
  }

  public static String nuevoId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }

  public static boolean esTraceIdValido(String id) {
    return id != null && TRACE_ID_VALIDO.matcher(id).matches();
  }

  public static String traceId() {
    return MDC.get(TRACE_ID);
  }

  public static String requestId() {
    return MDC.get(REQUEST_ID);
  }

  /**
   * Abre el contexto para una request HTTP entrante. Siempre inicia contexto nuevo en este hilo:
   * reutiliza el traceId entrante si es valido; si falta o es invalido, genera uno.
   */
  public static Scope desdeRequest(String traceIdEntrante) {
    String trace = esTraceIdValido(traceIdEntrante) ? traceIdEntrante : nuevoId();
    poner(trace);
    return new Scope(true);
  }

  /**
   * Abre el contexto para una tarea sin request entrante (jobs programados). Si el hilo ya tiene
   * una traza activa (por ejemplo, un job que llama a otro), la hereda y no la limpia al cerrar.
   */
  public static Scope tarea() {
    if (MDC.get(TRACE_ID) != null) {
      return new Scope(false);
    }
    poner(nuevoId());
    return new Scope(true);
  }

  private static void poner(String trace) {
    String requestId = nuevoId().substring(0, 8);
    MDC.put(TRACE_ID, trace);
    MDC.put(REQUEST_ID, requestId);
    MDC.put(LEGACY_REQUEST_ID, requestId);
    MDC.put(INSTANCE_ID, INSTANCIA);
  }

  /** Cierra el contexto abierto por {@link #desdeRequest} o {@link #tarea}. */
  public static final class Scope implements AutoCloseable {

    private final boolean limpiar;

    private Scope(boolean limpiar) {
      this.limpiar = limpiar;
    }

    @Override
    public void close() {
      if (limpiar) {
        MDC.remove(TRACE_ID);
        MDC.remove(REQUEST_ID);
        MDC.remove(LEGACY_REQUEST_ID);
        MDC.remove(INSTANCE_ID);
      }
    }
  }

  private static String resolverInstancia() {
    String id = primeroNoVacio(System.getenv("RENDER_INSTANCE_ID"), System.getenv("INSTANCE_NAME"));
    if (id == null) {
      try {
        id = InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
        id = "unknown-host";
      }
    }
    return id;
  }

  private static String primeroNoVacio(String a, String b) {
    if (a != null && !a.isBlank()) {
      return a;
    }
    if (b != null && !b.isBlank()) {
      return b;
    }
    return null;
  }
}
