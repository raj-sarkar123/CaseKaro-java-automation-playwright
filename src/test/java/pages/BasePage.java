package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class BasePage {

    protected Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    public void navigateTo(String url) {
        page.navigate(url);
        page.waitForLoadState();
    }

    public String getPageTitle() {
        return page.title();
    }

    public void waitForVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void clickElement(Locator locator) {
        waitForVisible(locator);
        locator.click();
    }

    public void fillText(Locator locator, String text) {
        waitForVisible(locator);
        locator.fill(text);
    }

    public void clearText(Locator locator) {
        waitForVisible(locator);
        locator.fill("");
    }

    public void scrollToElement(Locator locator) {
        locator.scrollIntoViewIfNeeded();
    }
}
