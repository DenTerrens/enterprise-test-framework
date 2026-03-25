package com.automation.framework.demoapp;

public final class DemoAppSupport {
  private static final String DEMO_DB_URL =
      "jdbc:h2:mem:integrated-demo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
  private static DemoAppServer server;

  private DemoAppSupport() {}

  public static synchronized void ensureStarted() {
    if (server == null) {
      server = new DemoAppServer(DEMO_DB_URL);
      server.start();
    }
    applySystemProperties();
  }

  public static synchronized void reset() {
    ensureStarted();
    server.resetData();
  }

  public static synchronized String uiBaseUrl() {
    ensureStarted();
    return server.uiBaseUrl();
  }

  public static synchronized String apiBaseUrl() {
    ensureStarted();
    return server.apiBaseUrl();
  }

  public static synchronized String dbUrl() {
    ensureStarted();
    return server.dbUrl();
  }

  public static void clearOverrides() {
    System.clearProperty("ui.baseUrl");
    System.clearProperty("api.baseUrl");
    System.clearProperty("db.url");
    System.clearProperty("api.auth.type");
    System.clearProperty("api.auth.token");
    System.clearProperty("api.auth.username");
    System.clearProperty("api.auth.password");
  }

  private static void applySystemProperties() {
    System.setProperty("ui.baseUrl", server.uiBaseUrl());
    System.setProperty("api.baseUrl", server.apiBaseUrl());
    System.setProperty("db.url", server.dbUrl());
  }
}
