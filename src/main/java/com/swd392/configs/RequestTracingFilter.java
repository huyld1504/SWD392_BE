package com.swd392.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID = "requestId";
  private static final String REQUEST_URI = "requestUri";
  private static final String REQUEST_METHOD = "requestMethod";

  private static final String LINE = "═══════════════════════════════════════════════════════════════";
  private static final String THIN_LINE = "───────────────────────────────────────────────────────────────";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Generate unique request ID
    String requestId = UUID.randomUUID().toString();

    // Store in MDC for automatic inclusion in all logs
    MDC.put(REQUEST_ID, requestId);
    MDC.put(REQUEST_URI, request.getRequestURI());
    MDC.put(REQUEST_METHOD, request.getMethod());

    // Store in request context
    RequestContext.setRequestId(requestId);
    RequestContext.setRequestUri(request.getRequestURI());
    RequestContext.setRequestMethod(request.getMethod());

    // Add to response header for client tracking
    response.setHeader("X-Request-ID", requestId);

    long startTime = System.currentTimeMillis();

    try {
      log.info(
          "\n{}\n  ► REQUEST START\n{}\n  Request ID : {}\n  Method     : {}\n  URI        : {}\n  Remote IP  : {}\n{}",
          LINE, THIN_LINE, requestId, request.getMethod(), request.getRequestURI(),
          request.getRemoteAddr(), LINE);

      filterChain.doFilter(request, response);

      long duration = System.currentTimeMillis() - startTime;
      log.info("\n{}\n  ◄ REQUEST END\n{}\n  Request ID : {}\n  Status     : {}\n  Duration   : {} ms\n{}",
          LINE, THIN_LINE, requestId, response.getStatus(), duration, LINE);

    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("\n{}\n  ✖ REQUEST FAILED\n{}\n  Request ID : {}\n  Duration   : {} ms\n  Error      : {}\n{}",
          LINE, THIN_LINE, requestId, duration, e.getMessage(), LINE, e);
      throw e;
    } finally {
      // Clean up MDC and RequestContext
      MDC.clear();
      RequestContext.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Skip tracing for static resources and actuator endpoints
    return path.startsWith("/actuator") ||
        path.startsWith("/static") ||
        path.startsWith("/webjars");
  }
}
