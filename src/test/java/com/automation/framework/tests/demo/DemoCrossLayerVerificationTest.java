package com.automation.framework.tests.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.automation.framework.api.service.AuthApi;
import com.automation.framework.api.service.UploadsApi;
import com.automation.framework.api.service.UsersApi;
import com.automation.framework.db.DatabaseClient;
import com.automation.framework.demoapp.DemoAppSupport;
import com.automation.framework.tests.base.BaseDemoUiTest;
import com.automation.framework.ui.pages.LoginPage;
import com.automation.framework.ui.pages.UsersPage;
import com.automation.framework.utils.RetrySupport;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@DisplayName("Cross Layer Demo Verification")
@Tag("integration")
@Tag("demo")
@Tag("regression")
@ResourceLock("demoApp")
@Execution(ExecutionMode.SAME_THREAD)
class DemoCrossLayerVerificationTest extends BaseDemoUiTest {
  private UsersApi usersApi;
  private UploadsApi uploadsApi;
  private DatabaseClient databaseClient;

  @BeforeEach
  void setUpCrossLayerClients() {
    System.setProperty("api.auth.type", "none");
    String token = new AuthApi().login("admin", "secret123").jsonPath().getString("token");
    System.setProperty("api.auth.type", "bearer");
    System.setProperty("api.auth.token", token);
    usersApi = new UsersApi();
    uploadsApi = new UploadsApi();
    System.setProperty("db.url", DemoAppSupport.dbUrl());
    databaseClient = new DatabaseClient();
  }

  @AfterEach
  void tearDownCrossLayerClients() {
    if (databaseClient != null) {
      databaseClient.close();
    }
  }

  @Test
  @DisplayName("Create a user in the UI then verify it through API and database")
  void createInUiVerifyThroughApiAndDb() {
    Map<String, String> user = DemoUserDataFactory.newUser("CrossUi");

    new LoginPage(page).open().loginAs("admin", "secret123");
    UsersPage usersPage = new UsersPage(page);
    usersPage.createUser(user.get("name"), user.get("email"), user.get("role"), user.get("status"));
    int userId = Integer.parseInt(usersPage.operationMessage().replaceAll("\\D+", ""));

    assertThat(usersApi.getUser(userId).jsonPath().getString("email")).isEqualTo(user.get("email"));
    List<Map<String, Object>> rows =
        RetrySupport.until(
            () -> databaseClient.query("select email from managed_user where user_id = ?", userId),
            result -> !result.isEmpty(),
            Duration.ofSeconds(2),
            Duration.ofMillis(100));
    assertThat(rows)
        .singleElement()
        .extracting(row -> row.get("email"))
        .isEqualTo(user.get("email"));
  }

  @Test
  @DisplayName("Update a user through API then verify the UI and database reflect the change")
  void updateThroughApiVerifyUiAndDb() {
    Map<String, String> user = DemoUserDataFactory.newUser("CrossApiCreate");
    int userId = usersApi.createUser(user).jsonPath().getInt("userId");
    Map<String, String> updated = DemoUserDataFactory.updatedUser("CrossApiUpdate");

    usersApi.updateUser(userId, updated);

    new LoginPage(page).open().loginAs("admin", "secret123");
    UsersPage usersPage = new UsersPage(page).open();
    List<String> names =
        RetrySupport.until(
            usersPage::visibleUserNames,
            result -> result.contains(updated.get("name")),
            Duration.ofSeconds(2),
            Duration.ofMillis(100));
    assertThat(names).contains(updated.get("name"));
    List<Map<String, Object>> rows =
        RetrySupport.until(
            () -> databaseClient.query("select status from managed_user where user_id = ?", userId),
            result -> !result.isEmpty(),
            Duration.ofSeconds(2),
            Duration.ofMillis(100));
    assertThat(rows).singleElement().extracting(row -> row.get("status")).isEqualTo("DISABLED");
  }

  @Test
  @DisplayName("Upload a file in the UI then verify processing in database and API output")
  void uploadInUiVerifyDbAndApi() {
    new LoginPage(page).open().loginAs("admin", "secret123");
    UsersPage usersPage = new UsersPage(page);
    usersPage.uploadAndProcessFile(
        Path.of("src", "test", "resources", "data", "files", "demo-users.csv"));

    long uploadId = Long.parseLong(usersPage.operationMessage().replaceAll("\\D+", ""));
    assertThat(uploadsApi.getUpload(uploadId).jsonPath().getInt("rowCount")).isEqualTo(2);
    List<Map<String, Object>> rows =
        RetrySupport.until(
            () ->
                databaseClient.query(
                    "select processing_status, row_count from upload_audit where upload_id = ?",
                    uploadId),
            result -> !result.isEmpty(),
            Duration.ofSeconds(2),
            Duration.ofMillis(100));
    assertThat(rows)
        .singleElement()
        .satisfies(
            row -> {
              assertThat(row.get("processing_status")).isEqualTo("PROCESSED");
              assertThat(row.get("row_count")).isEqualTo(2);
            });
  }

  @Test
  @DisplayName("Reject an invalid UI submission and verify no database record is created")
  void invalidUiSubmissionDoesNotCreateDatabaseRecord() {
    String invalidName = "Invalid Ui User";

    new LoginPage(page).open().loginAs("admin", "secret123");
    UsersPage usersPage = new UsersPage(page);
    usersPage.createUser(invalidName, "", "ANALYST", "ACTIVE");

    assertThat(usersPage.operationMessage()).contains("Name and email are required");
    List<Map<String, Object>> rows =
        databaseClient.query("select user_id from managed_user where name = ?", invalidName);
    assertThat(rows).isEmpty();
  }
}
