package com.automation.framework.ui.pages;

import com.automation.framework.config.ConfigManager;
import com.microsoft.playwright.Page;
import java.nio.file.Path;
import java.util.List;

public class UsersPage extends BasePage {
  public UsersPage(Page page) {
    super(page);
  }

  public UsersPage open() {
    page.navigate(ConfigManager.get("ui.baseUrl") + "/users");
    waitForVisible("[data-test='user-table']");
    return this;
  }

  public boolean isLoaded() {
    return locator("[data-test='user-table']").isVisible();
  }

  public UsersPage createUser(String name, String email, String role, String status) {
    locator("[data-test='create-name']").fill(name);
    locator("[data-test='create-email']").fill(email);
    locator("[data-test='create-role']").fill(role);
    locator("[data-test='create-status']").fill(status);
    page.waitForResponse(
        response ->
            response.url().endsWith("/api/users")
                && "POST".equalsIgnoreCase(response.request().method()),
        () -> locator("[data-test='create-user']").click());
    waitForVisible("[data-test='operation-message']");
    return this;
  }

  public UsersPage updateUser(
      String userId, String name, String email, String role, String status) {
    locator("[data-test='update-id']").fill(userId);
    locator("[data-test='update-name']").fill(name);
    locator("[data-test='update-email']").fill(email);
    locator("[data-test='update-role']").fill(role);
    locator("[data-test='update-status']").fill(status);
    page.waitForResponse(
        response ->
            response.url().contains("/api/users/")
                && "PUT".equalsIgnoreCase(response.request().method()),
        () -> locator("[data-test='update-user']").click());
    return this;
  }

  public UsersPage deleteUser(String userId) {
    locator("[data-test='delete-id']").fill(userId);
    page.waitForResponse(
        response ->
            response.url().contains("/api/users/")
                && "DELETE".equalsIgnoreCase(response.request().method()),
        () -> locator("[data-test='delete-user']").click());
    return this;
  }

  public UsersPage uploadAndProcessFile(Path filePath) {
    locator("[data-test='upload-file']").setInputFiles(filePath);
    page.waitForResponse(
        response ->
            response.url().endsWith("/api/uploads/process")
                && "POST".equalsIgnoreCase(response.request().method()),
        () -> locator("[data-test='process-upload']").click());
    return this;
  }

  public String operationMessage() {
    return locator("[data-test='operation-message']").innerText();
  }

  public List<String> visibleUserNames() {
    return locator("[data-test='user-name']").allInnerTexts();
  }

  public String firstVisibleUserId() {
    return locator("[data-test='user-id']").first().innerText();
  }
}
