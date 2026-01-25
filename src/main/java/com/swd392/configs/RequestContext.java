package com.swd392.configs;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestContext {

  private static final ThreadLocal<String> requestId = new ThreadLocal<>();
  private static final ThreadLocal<String> requestUri = new ThreadLocal<>();
  private static final ThreadLocal<String> requestMethod = new ThreadLocal<>();
  private static final ThreadLocal<String> currentLayer = new ThreadLocal<>();

  public static void setRequestId(String id) {
    requestId.set(id);
  }

  public static String getRequestId() {
    return requestId.get();
  }

  public static void setRequestUri(String uri) {
    requestUri.set(uri);
  }

  public static String getRequestUri() {
    return requestUri.get();
  }

  public static void setRequestMethod(String method) {
    requestMethod.set(method);
  }

  public static String getRequestMethod() {
    return requestMethod.get();
  }

  public static void setCurrentLayer(String layer) {
    currentLayer.set(layer);
  }

  public static String getCurrentLayer() {
    return currentLayer.get();
  }

  public static void clear() {
    requestId.remove();
    requestUri.remove();
    requestMethod.remove();
    currentLayer.remove();
  }
}
