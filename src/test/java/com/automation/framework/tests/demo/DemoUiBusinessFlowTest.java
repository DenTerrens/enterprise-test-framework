package com.automation.framework.tests.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.tests.base.BaseDemoUiTest;
import com.automation.framework.ui.pages.LoginPage;
import com.automation.framework.ui.pages.UsersPage;
import com.automation.framework.utils.RetrySupport;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@DisplayName("Demo UI Business Verification")
@Tag("ui")
@Tag("demo")
@Tag("regression")
@ResourceLock("demoApp")
class DemoUiBusinessFlowTest extends BaseDemoUiTest {
  @Test
  @DisplayName("Verify admin login succeeds and invalid credentials fail cleanly")
  void loginSuccessAndFailureAreHandled() {
    LoginPage loginPage = new LoginPage(page).open();
    loginPage.loginAs("admin", "wrong-password");
    assertThat(loginPage.errorMessage()).isEqualTo("Invalid credentials");

    loginPage.open().loginAs("admin", "secret123");
    assertThat(new UsersPage(page).isLoaded()).isTrue();
  }

  @Test
  @DisplayName("Verify the UI can create update and delete a managed user")
  void uiCrudFlowWorks() {
    Map<String, String> createdUser = DemoUserDataFactory.newUser("UiFlow");
    Map<String, String> updatedUser = DemoUserDataFactory.updatedUser("UiFlowUpdated");

    new LoginPage(page).open().loginAs("admin", "secret123");
    UsersPage usersPage = new UsersPage(page);
    usersPage.createUser(
        createdUser.get("name"),
        createdUser.get("email"),
        createdUser.get("role"),
        createdUser.get("status"));
    assertThat(usersPage.operationMessage()).contains("Created user");
    assertThat(
            RetrySupport.until(
                usersPage::visibleUserNames,
                result -> result.contains(createdUser.get("name")),
                Duration.ofSeconds(2),
                Duration.ofMillis(100)))
        .contains(createdUser.get("name"));

    String createdId = usersPage.operationMessage().replaceAll("\\D+", "");
    usersPage.updateUser(
        createdId,
        updatedUser.get("name"),
        updatedUser.get("email"),
        updatedUser.get("role"),
        updatedUser.get("status"));
    assertThat(usersPage.operationMessage()).contains("Updated user");
    assertThat(
            RetrySupport.until(
                usersPage::visibleUserNames,
                result -> result.contains(updatedUser.get("name")),
                Duration.ofSeconds(2),
                Duration.ofMillis(100)))
        .contains(updatedUser.get("name"));

    usersPage.deleteUser(createdId);
    assertThat(usersPage.operationMessage()).contains("Deleted");
    assertThat(
            RetrySupport.until(
                usersPage::visibleUserNames,
                result -> !result.contains(updatedUser.get("name")),
                Duration.ofSeconds(2),
                Duration.ofMillis(100)))
        .doesNotContain(updatedUser.get("name"));
  }
}
