package com.automation.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

public final class ConfigManager {
  private static final Properties PROPERTIES = loadProperties();

  private ConfigManager() {}

  private static Properties loadProperties() {
    Properties properties = new Properties();
    loadInto(properties, "config/application.properties");
    loadInto(
        properties, "config/environments/" + System.getProperty("env", "local") + ".properties");
    return properties;
  }

  private static void loadInto(Properties target, String resourcePath) {
    try (InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalStateException("Missing configuration resource: " + resourcePath);
      }
      target.load(stream);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to load configuration resource: " + resourcePath, exception);
    }
  }

  public static String get(String key) {
    return System.getProperty(
        key,
        Objects.requireNonNull(
            PROPERTIES.getProperty(key), () -> "Missing configuration key: " + key));
  }

  public static boolean getBoolean(String key) {
    return Boolean.parseBoolean(get(key));
  }

  public static int getInt(String key) {
    return Integer.parseInt(get(key));
  }
}
