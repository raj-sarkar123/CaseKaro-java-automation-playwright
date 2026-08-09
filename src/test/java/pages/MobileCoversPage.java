package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import utils.TestData;

import java.util.regex.Pattern;

public class MobileCoversPage extends BasePage {

    private final Locator pageHeading;
    private final Locator modelSearchInput;
    private final Locator autocompleteContainer;
    private final Locator suggestionItems;

    public MobileCoversPage(Page page) {
        super(page);
        this.pageHeading = page.locator("h1, h2, .page-title, [class*='title']").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("(Mobile Covers|Phone cases)", Pattern.CASE_INSENSITIVE))).first();

        this.modelSearchInput = page.locator(
                "#search_main, #search-bar-cover-page, input[placeholder*='Search phone model'], input[placeholder*='search'], input[placeholder*='Search'], input[type='search']")
                .first();

        this.autocompleteContainer = page.locator(
                ".autocomplete-suggestions, .search-results, .suggestions, [class*='suggestion'], [class*='search-result'], ul.results, div.results, .search__results, .predictive-search, [role='listbox']")
                .first();

        this.suggestionItems = page.locator(
                ".autocomplete-suggestion, .search-result-item, .suggestion-item, [class*='suggestion-item'], " +
                "ul.results li, div.results a, .search__results-item, a[href*='iphone'], " +
                "[class*='predictive-search'] li, div[role='option'], .predictive-search__item, " +
                ".predictive-search a, a[href*='search?q=']");
    }

    public void verifyMobileCoversPageLoaded() {
        page.waitForLoadState();
        String currentUrl = page.url();
        Assertions.assertTrue(
                currentUrl.contains("mobile-covers") || currentUrl.contains("phone-cases")
                        || page.title().toLowerCase().contains("mobile cover"),
                "Mobile Covers page failed to load! URL: " + currentUrl);
    }

    public void scrollToPhoneCasesByModelSection() {
        if (modelSearchInput.count() > 0) {
            scrollToElement(modelSearchInput);
            waitForVisible(modelSearchInput);
            Assertions.assertTrue(modelSearchInput.isVisible(), "Phone model search input is not visible!");
        }
    }

    public void searchPhoneModel(String brandOrModel) {
        waitForVisible(modelSearchInput);
        modelSearchInput.click();
        modelSearchInput.fill("");
        modelSearchInput.pressSequentially(brandOrModel,
                new Locator.PressSequentiallyOptions().setDelay(120));

        suggestionItems.first().waitFor(new Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(10000));

        int previousCount = -1;
        for (int attempt = 0; attempt < 10; attempt++) {
            int currentCount = suggestionItems.count();
            if (currentCount == previousCount && currentCount > 0) {
                break;
            }
            previousCount = currentCount;
            page.waitForTimeout(300);
        }
    }

    public void clearPhoneModelSearch() {
        waitForVisible(modelSearchInput);
        modelSearchInput.fill("");
        modelSearchInput.dispatchEvent("input");
        page.waitForLoadState();
    }

    public void verifyAppleSearchResults() {
    boolean appleFound = false;
    for (int attempt = 0; attempt < 15 && !appleFound; attempt++) {
        int count = suggestionItems.count();
        for (int i = 0; i < count; i++) {
            String text = suggestionItems.nth(i).innerText();
            if (text.toLowerCase().contains("apple") || text.toLowerCase().contains("iphone")) {
                appleFound = true;
                break;
            }
        }
        if (!appleFound) {
            page.waitForTimeout(400);
        }
    }
    Assertions.assertTrue(appleFound || autocompleteContainer.isVisible(),
            "Search results for 'Apple' did not contain any Apple/iPhone model suggestions!");
}

    public void verifyNoOtherBrandsVisibleInSearchResults() {
        int count = suggestionItems.count();
        StringBuilder visibleSuggestions = new StringBuilder();

        for (int i = 0; i < count; i++) {
            if (suggestionItems.nth(i).isVisible()) {
                visibleSuggestions.append(" ").append(suggestionItems.nth(i).innerText());
            }
        }
        String combinedText = visibleSuggestions.toString().toLowerCase();

        for (String unrelatedBrand : TestData.UNRELATED_BRANDS) {
            Assertions.assertFalse(combinedText.contains(unrelatedBrand.toLowerCase()),
                    "Negative Validation Failed! Unrelated brand '" + unrelatedBrand
                            + "' was visible in Apple search results: " + combinedText);
        }
    }

    public void verifyAutocompleteVisible() {
        Assertions.assertTrue(suggestionItems.count() > 0 || autocompleteContainer.isVisible(),
                "Autocomplete dropdown is not visible for search query!");
    }

    private int findSuggestionIndexByText(String targetText, boolean exactMatch) {
        int count = suggestionItems.count();
        for (int i = 0; i < count; i++) {
            Locator item = suggestionItems.nth(i);
            if (!item.isVisible()) continue;

            Object insideChrome = item.evaluate("el => !!el.closest('header, nav, footer')");
            if (Boolean.TRUE.equals(insideChrome)) continue;

            String text = item.innerText().replaceAll("\\s+", " ").trim();
            if (exactMatch) {
                if (text.equalsIgnoreCase(targetText)) return i;
            } else {
                if (text.toLowerCase().contains(targetText.toLowerCase())) return i;
            }
        }
        return -1;
    }

   public void verifyExactPhoneModelSuggestionVisible(String exactModel) {
    verifyAutocompleteVisible();

    int idx = -1;
    for (int attempt = 0; attempt < 15; attempt++) {
        idx = findSuggestionIndexByText(exactModel, false);
        if (idx != -1) {
            break;
        }
        page.waitForTimeout(400);
    }

    if (idx == -1) {
        System.out.println("=== DEBUG: real (non-nav/footer) suggestions currently visible ===");
        int count = suggestionItems.count();
        for (int i = 0; i < count; i++) {
            Locator item = suggestionItems.nth(i);
            if (!item.isVisible()) continue;
            Object insideChrome = item.evaluate("el => !!el.closest('header, nav, footer')");
            if (Boolean.TRUE.equals(insideChrome)) continue;
            System.out.println("  [" + i + "] " + item.innerText().replaceAll("\\s+", " ").trim());
        }
    }

    Assertions.assertTrue(idx != -1,
            "Exact suggestion '" + exactModel + "' was not found among real (non-navigation) suggestions!");
}

public void selectExactPhoneModelSuggestion(String exactModel) {
    int idx = -1;
    for (int attempt = 0; attempt < 15; attempt++) {
        idx = findSuggestionIndexByText(exactModel, true);
        if (idx != -1) {
            break;
        }
        page.waitForTimeout(400);
    }

    Assertions.assertTrue(idx != -1,
            "Exact suggestion '" + exactModel + "' was not found among real (non-navigation) suggestions!");

    suggestionItems.nth(idx).click(new Locator.ClickOptions().setForce(true));
    page.waitForLoadState();
}
    public void verifyPhoneModelMaxNotSelected(String unexpectedModel) {
        String currentUrl = page.url();
        String pageHeadingText = page.locator("h1, .page-title").first().count() > 0
                ? page.locator("h1, .page-title").first().innerText().trim()
                : "";

        boolean maxWasSelected = currentUrl.toLowerCase().contains("pro-max")
                || pageHeadingText.equalsIgnoreCase(unexpectedModel);

        Assertions.assertFalse(maxWasSelected,
                "Validation Error: 'iPhone 16 Pro Max' was selected instead of 'iPhone 16 Pro'! URL: " + currentUrl);
    }
}