package utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

public class DomInspector {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            System.out.println("=== 1. Opening CaseKaro Home ===");
            page.navigate("https://casekaro.com/");
            System.out.println("Page Title: " + page.title());

            System.out.println("=== 2. Finding Mobile Covers menu item ===");
            Locator mobileCovers = page.locator("a:has-text('Mobile Covers')");
            System.out.println("Mobile Covers links found: " + mobileCovers.count());
            for (int i = 0; i < mobileCovers.count(); i++) {
                System.out.println("MC [" + i + "]: href=" + mobileCovers.nth(i).getAttribute("href") + " text=" + mobileCovers.nth(i).innerText());
            }

            System.out.println("=== 3. Navigating to Mobile Covers Page ===");
            page.navigate("https://casekaro.com/pages/mobile-covers");
            page.waitForLoadState();

            System.out.println("=== 4. Searching for search input on Mobile Covers page ===");
            Locator searchInputs = page.locator("input");
            for (int i = 0; i < searchInputs.count(); i++) {
                Locator input = searchInputs.nth(i);
                if (input.isVisible()) {
                    System.out.println("Input [" + i + "]: placeholder=" + input.getAttribute("placeholder") + ", name=" + input.getAttribute("name") + ", id=" + input.getAttribute("id") + ", class=" + input.getAttribute("class"));
                }
            }

            System.out.println("=== Searching for text 'Phone cases by model' or similar ===");
            Locator headings = page.locator("h1, h2, h3, h4, p, div").filter(new Locator.FilterOptions().setHasText("cases by model"));
            for (int i = 0; i < headings.count(); i++) {
                System.out.println("Heading [" + i + "]: " + headings.nth(i).innerText());
            }

            System.out.println("=== Testing search box typing 'Apple' ===");
            Locator modelSearchInput = page.locator("input[placeholder*='search'], input[placeholder*='Search'], input[type='search'], input[name*='search'], #search-input, .search-input").first();
            System.out.println("Model search input visible: " + modelSearchInput.isVisible());
            modelSearchInput.fill("Apple");
            page.waitForTimeout(2000);

            Locator suggestions = page.locator(".autocomplete-suggestion, .search-result, .search-suggestions, [class*='suggestion'], [class*='search-item'], ul.results li, div.results div");
            System.out.println("Suggestions count: " + suggestions.count());
            for (int i = 0; i < Math.min(suggestions.count(), 10); i++) {
                System.out.println("Suggestion [" + i + "]: " + suggestions.nth(i).innerText());
            }

            browser.close();
        }
    }
}
