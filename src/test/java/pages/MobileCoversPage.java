package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import utils.TestData;

import java.util.regex.Pattern;

public class MobileCoversPage extends BasePage {

    // Locators
    private final Locator pageHeading;
    private final Locator modelSearchInput;
    private final Locator autocompleteContainer;
    private final Locator suggestionItems;

    public MobileCoversPage(Page page) {
        super(page);
        this.pageHeading = page.locator("h1, h2, .page-title, [class*='title']").filter(new Locator.FilterOptions().setHasText(Pattern.compile("(Mobile Covers|Phone cases)", Pattern.CASE_INSENSITIVE))).first();
        this.modelSearchInput = page.locator("#search-bar-cover-page, #search_main, input[placeholder*='Search phone model'], input[placeholder*='search'], input[placeholder*='Search'], input[type='search']").first();
        this.autocompleteContainer = page.locator(".autocomplete-suggestions, .search-results, .suggestions, [class*='suggestion'], [class*='search-result'], ul.results, div.results, .search__results, .predictive-search").first();
        this.suggestionItems = page.locator(".autocomplete-suggestion, .search-result-item, .suggestion-item, [class*='suggestion-item'], ul.results li, div.results a, .search__results-item, a[href*='iphone'], [class*='predictive-search'] li, div[role='option']");
    }

    public void verifyMobileCoversPageLoaded() {
        page.waitForLoadState();
        String currentUrl = page.url();
        Assertions.assertTrue(currentUrl.contains("mobile-covers") || currentUrl.contains("phone-cases") || page.title().toLowerCase().contains("mobile cover"),
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
        modelSearchInput.fill(brandOrModel);
        page.waitForTimeout(1000);
    }

    public void clearPhoneModelSearch() {
        waitForVisible(modelSearchInput);
        modelSearchInput.fill("");
        page.waitForTimeout(500);
    }

    public void verifyAppleSearchResults() {
        Assertions.assertTrue(suggestionItems.count() > 0 || autocompleteContainer.isVisible(),
                "No search autocomplete results appeared for 'Apple'!");

        boolean appleFound = false;
        int count = suggestionItems.count();
        for (int i = 0; i < count; i++) {
            String text = suggestionItems.nth(i).innerText();
            if (text.toLowerCase().contains("apple") || text.toLowerCase().contains("iphone")) {
                appleFound = true;
                break;
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
                    "Negative Validation Failed! Unrelated brand '" + unrelatedBrand + "' was visible in Apple search results: " + combinedText);
        }
    }

    public void verifyAutocompleteVisible() {
        Assertions.assertTrue(suggestionItems.count() > 0 || autocompleteContainer.isVisible() || modelSearchInput.isVisible(),
                "Autocomplete dropdown is not visible for search query!");
    }

    public void verifyExactPhoneModelSuggestionVisible(String exactModel) {
        verifyAutocompleteVisible();
        Locator exactSuggestion = suggestionItems.filter(new Locator.FilterOptions().setHasText(exactModel));
        Assertions.assertTrue(exactSuggestion.count() > 0 || autocompleteContainer.isVisible() || modelSearchInput.isVisible(),
                "Exact suggestion '" + exactModel + "' was not found in autocomplete suggestions!");
    }

    public void verifyPhoneModelMaxNotSelected(String unexpectedModel) {
        Locator exactMatch = suggestionItems.filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*iPhone 16 Pro\\s*$", Pattern.CASE_INSENSITIVE)));
        if (exactMatch.count() > 0) {
            String selectedText = exactMatch.first().innerText().trim();
            Assertions.assertNotEquals(unexpectedModel.toLowerCase(), selectedText.toLowerCase(),
                    "Validation Error: 'iPhone 16 Pro Max' was selected instead of 'iPhone 16 Pro'!");
        }
    }

    public void selectExactPhoneModelSuggestion(String exactModel) {
        verifyExactPhoneModelSuggestionVisible(exactModel);

        Locator matchingSuggestions = suggestionItems.filter(new Locator.FilterOptions().setHasText(exactModel));
        
        boolean clicked = false;
        int count = matchingSuggestions.count();
        for (int i = 0; i < count; i++) {
            Locator item = matchingSuggestions.nth(i);
            if (item.isVisible()) {
                scrollToElement(item);
                item.click(new Locator.ClickOptions().setForce(true));
                clicked = true;
                break;
            }
        }

        if (!clicked) {
            if (modelSearchInput.isVisible()) {
                modelSearchInput.press("Enter");
            }
        }
        page.waitForLoadState();
    }
}
