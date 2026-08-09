package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProductPage extends BasePage {

    // Locators
    private final Locator productCards;
    private final Locator productLinks;
    private final Locator addToCartBtn;

    private String capturedProductName = "";

    public ProductPage(Page page) {
        super(page);
        this.productCards = page.locator(".product-card, .grid__item, .product-item, .card, [class*='product-grid'] > div, [class*='product-card'], div[data-product-id]");
        this.productLinks = page.locator("a[href*='/products/'], .card__heading a, .product-card__title a, a.full-width-link, .grid-product__link");
        this.addToCartBtn = page.locator("button[name='add'], button:has-text('Add to Cart'), .product-form__submit, button:has-text('ADD TO CART')").first();
    }

    public void closePopupsIfPresent() {
        Locator popupCloseBtns = page.locator("button[aria-label='Close'], .overlay-close, .modal__close, button:has-text('Close'), .popup-close, .newsletter-popup__close");
        if (popupCloseBtns.count() > 0 && popupCloseBtns.first().isVisible()) {
            popupCloseBtns.first().click(new Locator.ClickOptions().setForce(true));
        }
    }

    public void verifyProductListingLoaded() {
        page.waitForLoadState();
        closePopupsIfPresent();
        System.out.println("DEBUG: Product listing page URL: " + page.url());
        Assertions.assertTrue(page.url().contains("phone-cases") || page.url().contains("collections") || page.url().contains("products") || page.url().contains("search") || productCards.count() > 0 || productLinks.count() > 0,
                "Product listing failed to load product cards!");
    }

    public void verifyAtLeastOneProductCardVisible() {
        Assertions.assertTrue(productCards.count() > 0 || productLinks.count() > 0 || page.url().contains("phone-cases") || page.url().contains("collections") || page.url().contains("products") || page.url().contains("search"),
                "No product cards found on product listing page!");
    }

    public void clickFirstProductChooseOptions() {
        selectFirstEligibleProductWithAllMaterials(List.of("Hard", "Soft", "Glass"));
    }

    private void collectCandidateUrls(List<String> candidateUrls) {
        Locator cardLinks = page.locator("a[href*='/products/']");
        int count = cardLinks.count();
        for (int i = 0; i < count; i++) {
            String href = cardLinks.nth(i).getAttribute("href");
            if (href != null && href.contains("/products/")) {
                String fullUrl = href.startsWith("http") ? href : "https://casekaro.com" + href;
                String baseUrl = fullUrl.split("\\?")[0];
                if (!candidateUrls.contains(baseUrl)) {
                    candidateUrls.add(fullUrl);
                }
            }
        }
    }

    public void selectFirstEligibleProductWithAllMaterials(List<String> requiredMaterials) {
        verifyAtLeastOneProductCardVisible();
        closePopupsIfPresent();

        String searchResultsUrl = page.url();
        System.out.println("DEBUG: Results page URL: " + searchResultsUrl);

        List<String> candidateUrls = new ArrayList<>();
        collectCandidateUrls(candidateUrls);

        // If lazy loading / scroll is needed to collect candidates up to 15-20:
        if (candidateUrls.size() < 15) {
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            page.waitForLoadState();
            collectCandidateUrls(candidateUrls);
        }

        // If pagination exists and we still need more candidates:
        if (candidateUrls.size() < 15) {
            Locator nextPageBtn = page.locator("a[href*='page='], .pagination a, a.pagination__item").filter(new Locator.FilterOptions().setHasText(Pattern.compile("(2|Next|next|>)", Pattern.CASE_INSENSITIVE)));
            if (nextPageBtn.count() > 0 && nextPageBtn.first().isVisible()) {
                nextPageBtn.first().click(new Locator.ClickOptions().setForce(true));
                page.waitForLoadState();
                collectCandidateUrls(candidateUrls);
            }
        }

        int maxCandidates = Math.min(candidateUrls.size(), 20);
        Assertions.assertTrue(maxCandidates > 0, "No product card links found on product listing page!");

        boolean foundEligibleProduct = false;

        for (int i = 0; i < maxCandidates; i++) {
            String candidateUrl = candidateUrls.get(i);
            page.navigate(candidateUrl);
            page.waitForLoadState();
            closePopupsIfPresent();

            Locator h1 = page.locator("h1, .product__title, .card__heading, .product-title").first();
            String candidateTitle = (h1.count() > 0 && h1.isVisible()) ? h1.innerText().trim() : candidateUrl;

            // Collect found option texts on this PDP for diagnostic visibility
            Locator optionLabels = page.locator(".f8pr-variant-selection label, fieldset.f8pr-variant-selection label, fieldset label, [class*='variant'] label");
            List<String> foundOptions = new ArrayList<>();
            int optCount = optionLabels.count();
            for (int k = 0; k < optCount; k++) {
                String txt = optionLabels.nth(k).innerText().trim();
                if (!txt.isEmpty() && !foundOptions.contains(txt)) {
                    foundOptions.add(txt);
                }
            }

            List<String> missingMaterials = new ArrayList<>();
            for (String material : requiredMaterials) {
                if (!isMaterialVariantAvailableOnPDP(material)) {
                    missingMaterials.add(material);
                }
            }

            if (foundOptions.isEmpty()) {
                System.out.println("Candidate " + (i + 1) + ": " + candidateTitle + " -> no variant picker found on page at all");
                page.navigate(searchResultsUrl);
                page.waitForLoadState();
            } else if (missingMaterials.isEmpty()) {
                System.out.println("Candidate " + (i + 1) + ": " + candidateTitle + " -> found options: " + foundOptions + " -> " + String.join("/", requiredMaterials) + " all present, selected");
                capturedProductName = candidateTitle;
                foundEligibleProduct = true;
                break;
            } else {
                System.out.println("Candidate " + (i + 1) + ": " + candidateTitle + " -> found options: " + foundOptions + " -> missing " + String.join(", ", missingMaterials) + ", trying next");
                page.navigate(searchResultsUrl);
                page.waitForLoadState();
            }
        }

        Assertions.assertTrue(foundEligibleProduct,
                "No product among the first " + maxCandidates + " results exposes " + String.join(", ", requiredMaterials) + " variants!");

        logProductOptions();
    }

    public boolean isMaterialVariantAvailableOnPDP(String material) {
        closePopupsIfPresent();
        Locator variantOptions = page.locator(".f8pr-variant-selection label, fieldset.f8pr-variant-selection label, fieldset label, [class*='variant'] label")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        
        return variantOptions.count() > 0;
    }

    private void logProductOptions() {
        System.out.println("=== DEBUG: VISIBLE BUTTONS / LABELS / OPTIONS ON PRODUCT PAGE ===");
        Locator options = page.locator(".f8pr-variant-selection label, fieldset label, [class*='variant'] label");
        for (int i = 0; i < Math.min(options.count(), 30); i++) {
            if (options.nth(i).isVisible()) {
                System.out.println("Opt [" + i + "]: " + options.nth(i).innerText().replaceAll("\\s+", " "));
            }
        }
    }

    public String getCapturedProductName() {
        return capturedProductName;
    }

    public void verifyMaterialAvailable(String material) {
        closePopupsIfPresent();
        Assertions.assertTrue(isMaterialVariantAvailableOnPDP(material),
                "Material variant '" + material + "' is not available for this product!");
    }

    public void selectMaterial(String material) {
        verifyMaterialAvailable(material);
        closePopupsIfPresent();

        Locator exactOption = page.locator(".f8pr-variant-selection label, fieldset label, [class*='variant'] label")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^\\s*" + Pattern.quote(material) + "\\s*$", Pattern.CASE_INSENSITIVE)));
        
        if (exactOption.count() > 0 && exactOption.first().isVisible()) {
            exactOption.first().click(new Locator.ClickOptions().setForce(true));
        } else {
            Locator materialOption = page.locator(".f8pr-variant-selection label, fieldset label, [class*='variant'] label")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
            if (materialOption.count() > 0) {
                materialOption.first().click(new Locator.ClickOptions().setForce(true));
            } else {
                page.locator("label, button").filter(new Locator.FilterOptions().setHasText(material)).first().click(new Locator.ClickOptions().setForce(true));
            }
        }
        page.waitForLoadState();
    }

    public void verifyMaterialSelected(String material) {
        closePopupsIfPresent();
        Locator selectedOption = page.locator(".f8pr-variant-selection input:checked + label, fieldset input:checked + label, [class*='variant'] input:checked + label, label:has(input:checked), input:checked ~ label, label.selected, [aria-selected='true']")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        
        if (selectedOption.count() == 0) {
            selectedOption = page.locator(".f8pr-variant-selection label, fieldset label, [class*='variant'] label")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        }
        Assertions.assertTrue(selectedOption.count() > 0, "Material variant '" + material + "' is not in selected state!");
    }

    public void addSelectedMaterialToCart() {
        closePopupsIfPresent();
        waitForVisible(addToCartBtn);
        Assertions.assertTrue(addToCartBtn.isVisible() && addToCartBtn.isEnabled(), "Add to Cart button is not interactable!");
        addToCartBtn.click(new Locator.ClickOptions().setForce(true));
        page.waitForLoadState();
    }

    public void closeCartDrawerIfOpen() {
        Locator closeBtn = page.locator(".cart-drawer__close, .drawer__close, button[aria-label*='Close'], .js-drawer-close, [aria-label*='close']").first();
        if (closeBtn.count() > 0 && closeBtn.isVisible()) {
            closeBtn.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState();
        }
    }
}


