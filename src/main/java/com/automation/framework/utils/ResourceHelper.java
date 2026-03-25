package com.automation.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceHelper {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private ResourceHelper() {}

  public static String readClasspathFile(String resourcePath) {
    try (InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to read classpath resource: " + resourcePath, exception);
    }
  }

  public static <T> T readJson(String resourcePath, Class<T> targetType) {
    try (InputStream stream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
      }
      return OBJECT_MAPPER.readValue(stream, targetType);
    } catch (IOException exception) {
      throw new UncheckedIOException(
          "Unable to deserialize JSON resource: " + resourcePath, exception);
    }
  }

  public static Path writeFile(Path destination, String content) {
    try {
      Files.createDirectories(destination.getParent());
      return Files.writeString(destination, content, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to write file: " + destination, exception);
    }
  }
}
