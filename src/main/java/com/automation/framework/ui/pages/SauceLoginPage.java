package com.automation.framework.ui.pages;

import com.automation.framework.config.ConfigManager;
import com.microsoft.playwright.Page;

public class SauceLoginPage extends BasePage {
  public SauceLoginPage(Page page) {
    super(page);
  }

  public SauceLoginPage open() {
    page.navigate(ConfigManager.get("ui.baseUrl"));
    waitForVisible("[data-test='login-button']");
    return this;
  }

  public SauceLoginPage loginAs(String username, String password) {
    locator("[data-test='username']").fill(username);
    locator("[data-test='password']").fill(password);
    locator("[data-test='login-button']").click();
    return this;
  }

  public String errorMessage() {
    return locator("[data-test='error']").innerText();
  }
}
