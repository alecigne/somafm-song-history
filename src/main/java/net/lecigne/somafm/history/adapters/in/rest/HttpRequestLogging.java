package net.lecigne.somafm.history.adapters.in.rest;

import io.javalin.http.Context;
import io.javalin.router.Endpoint;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public final class HttpRequestLogging {

  private static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final String REQUEST_ID = "request_id";
  private static final String START_NANOS = "request_start_nanos";

  private HttpRequestLogging() {
  }

  static void start(Context ctx) {
    String requestId = requestId(ctx.header(REQUEST_ID_HEADER));
    ctx.attribute(REQUEST_ID, requestId);
    ctx.attribute(START_NANOS, System.nanoTime());
    ctx.header(REQUEST_ID_HEADER, requestId);
    MDC.put(REQUEST_ID, requestId);
  }

  static void finish(Context ctx) {
    String requestId = ctx.attribute(REQUEST_ID);
    if (requestId != null) {
      MDC.put(REQUEST_ID, requestId);
    }
    log.atInfo()
        .addKeyValue("operation", "http.request")
        .addKeyValue(REQUEST_ID, requestId)
        .addKeyValue("method", String.valueOf(ctx.method()))
        .addKeyValue("path", ctx.path())
        .addKeyValue("route", route(ctx))
        .addKeyValue("status", ctx.statusCode())
        .addKeyValue("duration_ms", durationMs(ctx))
        .log("HTTP request completed");
    MDC.remove(REQUEST_ID);
  }

  private static String requestId(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return headerValue;
  }

  private static long durationMs(Context ctx) {
    Long startNanos = ctx.attribute(START_NANOS);
    if (startNanos == null) {
      return 0L;
    }
    return (System.nanoTime() - startNanos) / 1_000_000L;
  }

  private static String route(Context ctx) {
    try {
      Endpoint endpoint = ctx.endpoints().lastHttpEndpoint();
      if (endpoint != null) {
        return endpoint.path;
      }
    } catch (RuntimeException e) {
      log.atDebug()
          .addKeyValue("operation", "http.route.resolve")
          .setCause(e)
          .log("Could not resolve Javalin route for request");
    }
    return ctx.path();
  }

}
