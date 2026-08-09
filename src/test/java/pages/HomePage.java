package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import utils.TestData;

public class HomePage extends BasePage {

    private final Locator mobileCoversNavLink;
    private final Locator mainHeader;

    public HomePage(Page page) {
        super(page);
        this.mobileCoversNavLink = page.locator("a[href*='phone-cases-by-model'], a[href*='mobile-covers'], nav a:has-text('Mobile Covers'), a:has-text('Mobile covers')").first();
        this.mainHeader = page.locator("header, .header, #header").first();
    }

    public void navigateToHomePage() {
        navigateTo(TestData.HOME_URL);
    }

    public void verifyHomePageLoaded() {
        waitForVisible(mainHeader);
        String currentUrl = page.url();
        Assertions.assertTrue(currentUrl.contains("casekaro.com"), "CaseKaro home page URL validation failed! URL: " + currentUrl);
    }

    public void clickMobileCovers() {
        if (mobileCoversNavLink.count() > 0 && mobileCoversNavLink.isVisible()) {
            mobileCoversNavLink.click();
        } else {
            page.navigate(TestData.HOME_URL + "pages/phone-cases-by-model");
        }
        page.waitForLoadState();
    }
}
