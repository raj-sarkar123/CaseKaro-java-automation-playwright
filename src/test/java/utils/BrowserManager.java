package utils;

import com.microsoft.playwright.*;

import java.util.List;

public class BrowserManager {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    public static Page initBrowser() {
        String browserName = ConfigReader.getProperty("browser", "chromium").toLowerCase();
        boolean isHeadless = ConfigReader.getBooleanProperty("headless", false);

        playwright = Playwright.create();

        BrowserType browserType;
        switch (browserName) {
            case "firefox":
                browserType = playwright.firefox();
                break;
            case "webkit":
                browserType = playwright.webkit();
                break;
            case "chromium":
            default:
                browserType = playwright.chromium();
                break;
        }

        browser = browserType.launch(new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(List.of("--disable-http2", "--ignore-certificate-errors"))
                .setSlowMo(Double.parseDouble(ConfigReader.getProperty("slowMo", "0")))
        );

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
        );

        page = context.newPage();
        page.setDefaultTimeout(Double.parseDouble(ConfigReader.getProperty("timeout", "30000")));
        page.setDefaultNavigationTimeout(60000);

        return page;
    }

    public static Page getPage() {
        return page;
    }

    public static BrowserContext getContext() {
        return context;
    }

    public static void closeBrowser() {
        if (page != null) {
            page.close();
            page = null;
        }
        if (context != null) {
            context.close();
            context = null;
        }
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}
