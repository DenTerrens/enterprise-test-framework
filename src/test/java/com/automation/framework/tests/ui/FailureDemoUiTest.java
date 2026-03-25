package com.automation.framework.tests.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseDemoUiTest;
import com.automation.framework.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@DisplayName("UI Failure Demo")
@Tag("ui")
@Tag("demo-failure")
@EnabledIfSystemProperty(named = "includeFailureDemos", matches = "true")
public class FailureDemoUiTest extends BaseDemoUiTest {
  @Test
  @DisplayName("Show how Allure captures a UI failure with screenshot and DOM evidence")
  void failureDemoCapturesUiScreenshotAndDom() {
    LoginPage loginPage = new LoginPage(page).open();
    loginPage.loginAs("admin", "secret123");

    assertThat(page.url())
        .as("Intentional demo failure for screenshot and DOM evidence")
        .contains("/reports");
  }
}
