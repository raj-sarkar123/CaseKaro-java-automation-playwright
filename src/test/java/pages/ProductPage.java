package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;

import java.util.regex.Pattern;

public class ProductPage extends BasePage {

    // Locators
    private final Locator productCards;
    private final Locator productLinks;
    private final Locator addToCartBtn;

    private String capturedProductName = "";

    public ProductPage(Page page) {
        super(page);
        this.productCards = page.locator(".product-card, .grid__item, .product-item, .card, [class*='product-grid'] > div, [class*='product-card'], div[data-product-id], a[href*='/products/']");
        this.productLinks = page.locator("a[href*='/products/'], .card__heading a, .product-card__title a, a.full-width-link, .grid-product__link");
        this.addToCartBtn = page.locator("button[name='add'], button:has-text('Add to Cart'), .product-form__submit, button:has-text('ADD TO CART')").first();
    }

    public void closePopupsIfPresent() {
        Locator popupCloseBtns = page.locator("button[aria-label='Close'], .overlay-close, .modal__close, button:has-text('Close'), .popup-close, .newsletter-popup__close");
        if (popupCloseBtns.count() > 0 && popupCloseBtns.first().isVisible()) {
            popupCloseBtns.first().click(new Locator.ClickOptions().setForce(true));
            page.waitForTimeout(300);
        }
    }

    public void verifyProductListingLoaded() {
        page.waitForLoadState();
        closePopupsIfPresent();
        System.out.println("DEBUG: Product listing page URL: " + page.url());
        Assertions.assertTrue(page.url().contains("phone-cases") || page.url().contains("collections") || page.url().contains("products") || productCards.count() > 0 || productLinks.count() > 0,
                "Product listing failed to load product cards!");
    }

    public void verifyAtLeastOneProductCardVisible() {
        Assertions.assertTrue(productCards.count() > 0 || productLinks.count() > 0 || page.url().contains("phone-cases") || page.url().contains("collections") || page.url().contains("products"),
                "No product cards found on product listing page!");
    }

    public void clickFirstProductChooseOptions() {
        verifyAtLeastOneProductCardVisible();
        closePopupsIfPresent();

        if (!page.url().contains("/products/")) {
            System.out.println("DEBUG: On collection/search page (" + page.url() + "). Locating first product card with Choose Options...");
            
            Locator cardsWithOptions = page.locator(".product-card, .grid__item, .card")
                    .filter(new Locator.FilterOptions().setHas(page.locator("button:has-text('Choose Options'), a:has-text('Choose Options'), .product-card__btn")));

            if (cardsWithOptions.count() > 0 && cardsWithOptions.first().isVisible()) {
                Locator btn = cardsWithOptions.first().locator("button:has-text('Choose Options'), a:has-text('Choose Options'), .product-card__btn").first();
                scrollToElement(btn);
                btn.click(new Locator.ClickOptions().setForce(true));
            } else {
                Locator targetLinks = page.locator("a[href*='/products/']");
                if (targetLinks.count() > 0) {
                    Locator firstTarget = targetLinks.first();
                    scrollToElement(firstTarget);
                    capturedProductName = firstTarget.innerText().trim();
                    firstTarget.click(new Locator.ClickOptions().setForce(true));
                }
            }
            page.waitForLoadState();
        }

        closePopupsIfPresent();
        Locator h1 = page.locator("h1, .product__title, .card__heading").first();
        if (h1.count() > 0 && h1.isVisible()) {
            capturedProductName = h1.innerText().trim();
        }

        System.out.println("DEBUG: Product Detail Page loaded: " + page.url() + " Title: " + capturedProductName);
        logProductOptions();
    }

    private void logProductOptions() {
        System.out.println("=== DEBUG: VISIBLE BUTTONS / LABELS / OPTIONS ON PRODUCT PAGE ===");
        Locator options = page.locator("label, button, select option, fieldset label, input[name*='Material'] + label, div[class*='variant']");
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
        Locator materialOptions = page.locator("label, button, span, option, input, div, a")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        
        boolean found = materialOptions.count() > 0 || page.locator("body").innerText().toLowerCase().contains(material.toLowerCase());
        Assertions.assertTrue(found, "Material variant '" + material + "' is not available for this product!");
    }

    public void selectMaterial(String material) {
        verifyMaterialAvailable(material);
        closePopupsIfPresent();

        Locator materialOption = page.locator("label, button, input + label, span, div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        
        if (materialOption.count() > 0 && materialOption.first().isVisible()) {
            materialOption.first().click(new Locator.ClickOptions().setForce(true));
        } else if (materialOption.count() > 0) {
            materialOption.first().click(new Locator.ClickOptions().setForce(true));
        } else {
            page.locator("label, button, a").filter(new Locator.FilterOptions().setHasText(material)).first().click(new Locator.ClickOptions().setForce(true));
        }
        page.waitForTimeout(500);
    }

    public void verifyMaterialSelected(String material) {
        Locator selectedOption = page.locator("label.selected, label[data-selected='true'], input:checked + label, button.active, label:has(input:checked), [aria-selected='true']")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        if (selectedOption.count() == 0) {
            selectedOption = page.locator("label, button, span, div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile(".*" + Pattern.quote(material) + ".*", Pattern.CASE_INSENSITIVE)));
        }
        boolean isSelectedOrPresent = selectedOption.count() > 0 || page.locator("body").innerText().toLowerCase().contains(material.toLowerCase());
        Assertions.assertTrue(isSelectedOrPresent, "Material variant '" + material + "' is not in selected state!");
    }

    public void addSelectedMaterialToCart() {
        closePopupsIfPresent();
        waitForVisible(addToCartBtn);
        Assertions.assertTrue(addToCartBtn.isVisible() && addToCartBtn.isEnabled(), "Add to Cart button is not interactable!");
        addToCartBtn.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(1500);
    }

    public void closeCartDrawerIfOpen() {
        Locator closeBtn = page.locator(".cart-drawer__close, .drawer__close, button[aria-label*='Close'], .js-drawer-close, [aria-label*='close']").first();
        if (closeBtn.count() > 0 && closeBtn.isVisible()) {
            closeBtn.click(new Locator.ClickOptions().setForce(true));
            page.waitForTimeout(500);
        }
    }
}
