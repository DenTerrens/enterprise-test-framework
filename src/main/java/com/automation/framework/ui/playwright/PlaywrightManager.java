package com.automation.framework.ui.playwright;

import com.automation.framework.config.ConfigManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlaywrightManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightManager.class);
  private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
  private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
  private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
  private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

  private PlaywrightManager() {}

  public static void start() {
    String browserName = ConfigManager.get("browser");
    LOGGER.info(
        "Starting Playwright browser session with browser={} headless={}",
        browserName,
        ConfigManager.getBoolean("headless"));

    Playwright playwright = Playwright.create();
    PLAYWRIGHT.set(playwright);

    Browser browser =
        switch (browserName) {
          case "firefox" -> playwright.firefox().launch(new BrowserTypeOptionsFactory().options());
          case "webkit" -> playwright.webkit().launch(new BrowserTypeOptionsFactory().options());
          default -> playwright.chromium().launch(new BrowserTypeOptionsFactory().options());
        };
    BROWSER.set(browser);

    BrowserContext browserContext =
        browser.newContext(
            new Browser.NewContextOptions()
                .setViewportSize(1440, 960)
                .setRecordVideoDir(Path.of("reports", "videos")));
    CONTEXT.set(browserContext);
    PAGE.set(browserContext.newPage());
  }

  public static Page page() {
    return PAGE.get();
  }

  public static Path stop() {
    LOGGER.info("Stopping Playwright browser session");
    Path videoPath = extractVideoPath(PAGE.get());
    closeQuietly(PAGE.get());
    closeQuietly(CONTEXT.get());
    closeQuietly(BROWSER.get());
    closeQuietly(PLAYWRIGHT.get());
    PAGE.remove();
    CONTEXT.remove();
    BROWSER.remove();
    PLAYWRIGHT.remove();
    return videoPath;
  }

  private static Path extractVideoPath(Page page) {
    if (page == null || page.video() == null) {
      return null;
    }
    try {
      return page.video().path();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static void closeQuietly(Page page) {
    if (page != null) {
      page.close();
    }
  }

  private static void closeQuietly(BrowserContext context) {
    if (context != null) {
      context.close();
    }
  }

  private static void closeQuietly(Browser browser) {
    if (browser != null) {
      browser.close();
    }
  }

  private static void closeQuietly(Playwright playwright) {
    if (playwright != null) {
      playwright.close();
    }
  }

  private static final class BrowserTypeOptionsFactory {
    private BrowserTypeOptionsFactory() {}

    private com.microsoft.playwright.BrowserType.LaunchOptions options() {
      return new com.microsoft.playwright.BrowserType.LaunchOptions()
          .setHeadless(ConfigManager.getBoolean("headless"))
          .setSlowMo(Double.parseDouble(ConfigManager.get("ui.slowMoMillis")));
    }
  }
}
