package com.automation.framework.tests.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseDemoUiTest;
import com.automation.framework.ui.pages.LoginPage;
import com.automation.framework.ui.pages.UsersPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("UI Login Smoke Verification")
@Tag("ui")
@Tag("smoke")
@Tag("regression")
class UiLoginSmokeTest extends BaseDemoUiTest {
  @Test
  @DisplayName("Verify an admin user can sign in to the integrated demo application")
  void standardUserCanLoginAndAddItemToCart() {
    LoginPage loginPage = new LoginPage(page).open();

    loginPage.loginAs("admin", "secret123");
    assertThat(new UsersPage(page).isLoaded()).isTrue();
  }

  @Test
  @DisplayName(
      "Verify invalid credentials show a clear login error in the integrated demo application")
  void lockedOutUserSeesFriendlyError() {
    LoginPage loginPage = new LoginPage(page).open();

    loginPage.loginAs("admin", "wrong-password");
    assertThat(loginPage.errorMessage()).contains("Invalid credentials");
  }
}
