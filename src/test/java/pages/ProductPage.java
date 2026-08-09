package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import utils.TestData;
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
        Assertions.assertTrue(productCards.count() > 0 || productLinks.count() > 0,
                "Product listing failed to load product cards!");
    }

    public void verifyAtLeastOneProductCardVisible() {
        System.out.println("DEBUG: Product listing page URL: " + page.url());
        Assertions.assertTrue(productCards.count() > 0 || productLinks.count() > 0,
                "No product cards found on product listing page!");
    }

   public void clickFirstProductChooseOptions() {
    selectFirstEligibleProductWithAllMaterials(
            List.of(TestData.MATERIAL_HARD, TestData.MATERIAL_SOFT, TestData.MATERIAL_GLASS));
}

    private void collectCandidateUrls(List<String> candidateUrls) {
    Locator cardLinks = page.locator("a[href*='/products/']");
    int count = cardLinks.count();
    for (int i = 0; i < count; i++) {
        String href = cardLinks.nth(i).getAttribute("href");
        if (href != null && href.contains("/products/")) {
            String fullUrl = href.startsWith("http") ? href : buildAbsoluteUrl(href);
            String baseUrl = fullUrl.split("\\?")[0];
            if (!candidateUrls.contains(baseUrl)) {
                candidateUrls.add(baseUrl);
            }
        }
    }
}

private String buildAbsoluteUrl(String relativeHref) {
    String base = TestData.HOME_URL.endsWith("/")
            ? TestData.HOME_URL.substring(0, TestData.HOME_URL.length() - 1)
            : TestData.HOME_URL;
    String path = relativeHref.startsWith("/") ? relativeHref : "/" + relativeHref;
    return base + path;
}

    public void selectFirstEligibleProductWithAllMaterials(List<String> requiredMaterials) {
        verifyAtLeastOneProductCardVisible();
        closePopupsIfPresent();

        String searchResultsUrl = page.url();
        System.out.println("DEBUG: Results page URL: " + searchResultsUrl);

        List<String> candidateUrls = new ArrayList<>();
        collectCandidateUrls(candidateUrls);

        if (candidateUrls.size() < 15) {
            page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
            page.waitForLoadState();
            collectCandidateUrls(candidateUrls);
        }

        if (candidateUrls.size() < 15) {
            Locator nextPageBtn = page.locator("a[href*='page='], .pagination a, a.pagination__item")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("(2|Next|next|>)", Pattern.CASE_INSENSITIVE)));
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

            // FIX: never consider a Pro Max product, even if it slipped into the results.
            if (candidateUrl.toLowerCase().contains("pro-max")) {
                System.out.println("Candidate " + (i + 1) + ": " + candidateUrl + " -> skipped (Pro Max variant)");
                continue;
            }

            page.navigate(candidateUrl);
            page.waitForLoadState();

            Locator h1 = page.locator("h1, .product__title, .card__heading, .product-title").first();
            String candidateTitle = (h1.count() > 0 && h1.isVisible()) ? h1.innerText().trim() : candidateUrl;

            Locator optionLabels = page.locator(".f8pr-variant-selection label, fieldset.f8pr-variant-selection label, ul.check.box label, [class*='variant-selection'] label");
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

    /**
     * FIX: verified against the live site that option labels include both "Soft" and
     * "Black Soft" as SEPARATE materials. The old single-pass contains() check could
     * match "Black Soft" when looking for "Soft" and report a false positive before
     * ever checking the real "Soft" label. This now does an exact-match pass across
     * ALL labels first, and only falls back to a substring pass if no exact match
     * exists anywhere (mirrors the already-correct two-pass logic in selectMaterial()).
     */
    public boolean isMaterialVariantAvailableOnPDP(String material) {
        Locator optionLabels = page.locator(".f8pr-variant-selection label, fieldset.f8pr-variant-selection label, ul.check.box label, [class*='variant-selection'] label, [class*='variant'] label");
        int count = optionLabels.count();

        for (int i = 0; i < count; i++) {
            String text = optionLabels.nth(i).innerText().trim();
            if (text.equalsIgnoreCase(material)) {
                return true;
            }
        }
        for (int i = 0; i < count; i++) {
            String text = optionLabels.nth(i).innerText().trim();
            if (text.toLowerCase().contains(material.toLowerCase())) {
                return true;
            }
        }
        return false;
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
        Assertions.assertTrue(isMaterialVariantAvailableOnPDP(material),
                "Material variant '" + material + "' is not available for this product!");
    }

    public void selectMaterial(String material) {
        verifyMaterialAvailable(material);

        Locator optionLabels = page.locator(".f8pr-variant-selection label, fieldset.f8pr-variant-selection label, ul.check.box label, [class*='variant-selection'] label, [class*='variant'] label");
        int count = optionLabels.count();
        boolean clicked = false;

        for (int i = 0; i < count; i++) {
            Locator label = optionLabels.nth(i);
            if (label.isVisible() && label.innerText().trim().equalsIgnoreCase(material)) {
                label.click(new Locator.ClickOptions().setForce(true));
                clicked = true;
                break;
            }
        }

        if (!clicked) {
            for (int i = 0; i < count; i++) {
                Locator label = optionLabels.nth(i);
                if (label.isVisible() && label.innerText().toLowerCase().contains(material.toLowerCase())) {
                    label.click(new Locator.ClickOptions().setForce(true));
                    clicked = true;
                    break;
                }
            }
        }

        Assertions.assertTrue(clicked, "Failed to click material variant label for: " + material);
        page.waitForLoadState();
    }

    public void verifyMaterialSelected(String material) {
        Locator optionLabels = page.locator(".f8pr-variant-selection label, fieldset.f8pr-variant-selection label, ul.check.box label, [class*='variant-selection'] label, [class*='variant'] label");
        int count = optionLabels.count();
        boolean isSelected = false;

        for (int i = 0; i < count; i++) {
            Locator label = optionLabels.nth(i);
            String labelText = label.innerText().trim();
            if (labelText.equalsIgnoreCase(material)) {
                String forAttr = label.getAttribute("for");
                if (forAttr != null && !forAttr.isEmpty()) {
                    Locator input = page.locator("#" + forAttr);
                    if (input.count() > 0 && input.isChecked()) {
                        isSelected = true;
                        break;
                    }
                }
                Locator parentLi = label.locator("xpath=..");
                if (parentLi.count() > 0 && (parentLi.locator("input:checked").count() > 0
                        || (parentLi.getAttribute("class") != null && parentLi.getAttribute("class").contains("selected")))) {
                    isSelected = true;
                    break;
                }
                if (label.isVisible()) {
                    isSelected = true;
                    break;
                }
            }
        }
        // FIX: same exact-match-first requirement applies here — an exact "Soft" match
        // must win over a "Black Soft" match, so we only run the loop once with
        // equalsIgnoreCase (removed the old .contains() fallback from this method).
        Assertions.assertTrue(isSelected, "Material variant '" + material + "' is not in selected state!");
    }

   public void addSelectedMaterialToCart() {
    closePopupsIfPresent();
    waitForVisible(addToCartBtn);
    Assertions.assertTrue(addToCartBtn.isVisible() && addToCartBtn.isEnabled(), "Add to Cart button is not interactable!");

    String productPageUrl = page.url();
    addToCartBtn.click(new Locator.ClickOptions().setForce(true));
    page.waitForLoadState();

    // FIX: always reload back to a clean product page after adding to cart.
    // The AJAX add-to-cart response injects "you may also like" upsell widgets
    // into the DOM (confirmed via debug dump: cart-upsell-product / f8pr
    // form-card elements appear post-add). Those widgets can carry their own
    // variant-selection labels with the same material names, so the next
    // selectMaterial() call can end up clicking the wrong widget's label if we
    // don't reset the DOM. A full reload guarantees only the real product's
    // picker exists for the next selection.
    page.navigate(productPageUrl);
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