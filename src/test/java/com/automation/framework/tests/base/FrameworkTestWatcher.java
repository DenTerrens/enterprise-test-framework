package com.automation.framework.tests.base;

import com.automation.framework.reporting.AllureAttachments;
import com.automation.framework.ui.playwright.PlaywrightManager;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;

public class FrameworkTestWatcher implements TestWatcher, TestExecutionExceptionHandler {
  private static final ThreadLocal<Boolean> FAILURE_CAPTURED = ThreadLocal.withInitial(() -> false);
  private static final ThreadLocal<Boolean> TEST_FAILED = ThreadLocal.withInitial(() -> false);

  @Override
  public void testSuccessful(ExtensionContext context) {
    Allure.label("result", "passed");
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    captureFailureArtifacts(context, cause);
  }

  @Override
  public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
      throws Throwable {
    captureFailureArtifacts(context, throwable);
    throw throwable;
  }

  private void captureFailureArtifacts(ExtensionContext context, Throwable cause) {
    if (Boolean.TRUE.equals(FAILURE_CAPTURED.get())) {
      return;
    }

    TEST_FAILED.set(true);
    Optional<Page> currentPage = Optional.ofNullable(safelyGetPage());
    currentPage.ifPresent(
        page -> {
          byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
          AllureAttachments.attachScreenshot("Failure screenshot", screenshot);
          AllureAttachments.attachPageSnapshot("Failure DOM", page);
        });
    AllureAttachments.attachText(
        "Failure reason", cause.getMessage() == null ? cause.toString() : cause.getMessage());
    FAILURE_CAPTURED.set(true);
  }

  private Page safelyGetPage() {
    try {
      return PlaywrightManager.page();
    } catch (Exception ignored) {
      return null;
    }
  }

  public static boolean currentTestFailed() {
    return Boolean.TRUE.equals(TEST_FAILED.get());
  }

  public static void reset() {
    FAILURE_CAPTURED.remove();
    TEST_FAILED.remove();
  }
}
