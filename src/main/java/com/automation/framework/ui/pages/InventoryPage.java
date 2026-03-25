package com.automation.framework.ui.pages;

import com.microsoft.playwright.Page;

public class InventoryPage extends BasePage {
  public InventoryPage(Page page) {
    super(page);
  }

  public boolean isLoaded() {
    return locator(".inventory_list").isVisible();
  }

  public InventoryPage addBackpackToCart() {
    locator("[data-test='add-to-cart-sauce-labs-backpack']").click();
    return this;
  }

  public String cartBadgeCount() {
    return locator(".shopping_cart_badge").innerText();
  }
}
