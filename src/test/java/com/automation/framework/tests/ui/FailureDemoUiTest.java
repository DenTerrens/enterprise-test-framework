package com.automation.framework.tests.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseUiTest;
import com.automation.framework.ui.pages.SauceLoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@DisplayName("UI Failure Demo")
@Tag("ui")
@Tag("demo-failure")
@EnabledIfSystemProperty(named = "includeFailureDemos", matches = "true")
public class FailureDemoUiTest extends BaseUiTest {
  @Test
  @DisplayName("Show how Allure captures a UI failure with screenshot and DOM evidence")
  void failureDemoCapturesUiScreenshotAndDom() {
    SauceLoginPage loginPage = new SauceLoginPage(page).open();
    loginPage.loginAs("standard_user", "secret_sauce");

    assertThat(page.url())
        .as("Intentional demo failure for screenshot and DOM evidence")
        .contains("/checkout");
  }
}
