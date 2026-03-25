package com.automation.framework.tests.base;

import com.automation.framework.reporting.AllureAttachments;
import com.automation.framework.ui.playwright.PlaywrightManager;
import com.microsoft.playwright.Page;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(FrameworkTestWatcher.class)
public abstract class BaseUiTest {
  protected Page page;

  @BeforeEach
  void setUpUiSession() {
    PlaywrightManager.start();
    page = PlaywrightManager.page();
  }

  @AfterEach
  void tearDownUiSession() {
    Path videoPath = PlaywrightManager.stop();
    if (FrameworkTestWatcher.currentTestFailed() && videoPath != null && Files.exists(videoPath)) {
      AllureAttachments.attachFile("Failure video", videoPath, "video/webm", ".webm");
    }
    FrameworkTestWatcher.reset();
  }
}
