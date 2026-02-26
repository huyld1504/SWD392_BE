package com.swd392.exceptions;

import com.swd392.configs.RequestContext;
import org.springframework.http.HttpStatus;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AppException extends RuntimeException {
  private final boolean success;
  private final String message;
  private final HttpStatus statusCode;
  private final String requestId;
  private final String layer;
  private final LocalDateTime timestamp;

  public AppException(String message, HttpStatus statusCode) {
    super(message);
    this.success = false;
    this.message = message;
    this.statusCode = statusCode;
    this.requestId = RequestContext.getRequestId();
    this.layer = RequestContext.getCurrentLayer() != null ? RequestContext.getCurrentLayer() : "UNKNOWN";
    this.timestamp = LocalDateTime.now();
  }

  public AppException(String message) {
    this(message, HttpStatus.BAD_REQUEST);
  }
}
