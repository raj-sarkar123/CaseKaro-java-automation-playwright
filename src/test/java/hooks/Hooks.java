package hooks;

import com.microsoft.playwright.Page;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.BrowserManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hooks {

    @Before
    public void setUp() {
        BrowserManager.initBrowser();
    }

    @After
    public void tearDown(Scenario scenario) throws Exception {
        Page page = BrowserManager.getPage();

        if (scenario.isFailed() && page != null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String sanitizedScenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
            String screenshotName = sanitizedScenarioName + "_" + timestamp + ".png";

            Path screenshotsDir = Paths.get("target", "screenshots");
            if (!Files.exists(screenshotsDir)) {
                Files.createDirectories(screenshotsDir);
            }

            Path screenshotPath = screenshotsDir.resolve(screenshotName);
            byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath).setFullPage(true));

            scenario.attach(screenshotBytes, "image/png", screenshotName);
            System.out.println("Failure screenshot saved to: " + screenshotPath.toAbsolutePath());
        }

        BrowserManager.closeBrowser();
    }
}
