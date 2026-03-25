package com.automation.framework.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class BasePage {
  protected final Page page;

  protected BasePage(Page page) {
    this.page = page;
  }

  protected Locator locator(String selector) {
    return page.locator(selector);
  }

  protected void waitForVisible(String selector) {
    locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
  }
}
