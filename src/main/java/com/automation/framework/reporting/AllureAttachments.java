package com.automation.framework.reporting;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AllureAttachments {
  private AllureAttachments() {}

  public static void attachText(String name, String content) {
    Allure.addAttachment(
        name,
        "text/plain",
        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
        ".txt");
  }

  public static void attachHtml(String name, String content) {
    Allure.addAttachment(
        name,
        "text/html",
        new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
        ".html");
  }

  public static void attachScreenshot(String name, byte[] screenshotBytes) {
    Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), ".png");
  }

  public static void attachPageSnapshot(String name, Page page) {
    attachHtml(name, page.content());
  }

  public static void attachFile(String name, Path filePath, String contentType, String extension) {
    try (InputStream inputStream = Files.newInputStream(filePath)) {
      Allure.addAttachment(name, contentType, inputStream, extension);
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to attach file: " + filePath, exception);
    }
  }
}
