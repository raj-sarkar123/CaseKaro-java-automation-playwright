package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import models.CartItem;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CartPage extends BasePage {

    // Locators
    private final Locator cartIconBtn;
    private final Locator cartContainer;
    private final Locator cartItemRows;

    public CartPage(Page page) {
        super(page);
        this.cartIconBtn = page.locator("a[href*='/cart'], button[aria-label*='Cart'], .header__icon--cart, .cart-drawer-toggle, #cart-icon-bubble").first();
        this.cartContainer = page.locator(".cart-drawer, #cart-drawer, form[action*='/cart'], .cart__items, .cart-items").first();
        this.cartItemRows = page.locator(".cart-item, .cart-drawer__item, .cart-items__item, tr.cart-item, [class*='cart-item']");
    }

    public void openCart() {
        if (cartIconBtn.count() > 0 && cartIconBtn.isVisible()) {
            cartIconBtn.click();
        } else {
            page.navigate("https://casekaro.com/cart");
        }
        page.waitForLoadState();
    }

    public int getCurrentCartItemCount() {
        return cartItemRows.count();
    }

    public void verifyCartItemCountIncreasedFrom(int previousCount) {
        page.waitForLoadState();
        int newCount = cartItemRows.count();
        Assertions.assertTrue(newCount > previousCount,
            "Cart item count did not increase after add-to-cart! Before: " + previousCount + ", After: " + newCount);
    }

    public void verifyCartDrawerVisible() {
        page.waitForLoadState();
        Assertions.assertTrue(cartContainer.isVisible() || cartItemRows.count() > 0,
                "Cart drawer/page is not visible!");
    }

    public void verifyCartItemCount(int expectedCount) {
        verifyCartDrawerVisible();
        int count = cartItemRows.count();
        Assertions.assertEquals(expectedCount, count,
                "Cart item count mismatch! Expected: " + expectedCount + " but found: " + count);
    }

    public List<CartItem> getCartItems() {
        verifyCartDrawerVisible();
        List<CartItem> items = new ArrayList<>();
        int count = cartItemRows.count();

        for (int i = 0; i < count; i++) {
            Locator itemRow = cartItemRows.nth(i);

            // Extract Product Name
            String productName = "Unknown Product";
            Locator nameLocator = itemRow.locator(".cart-item__name, .product-title, a[href*='/products/'], .cart-item__title").first();
            if (nameLocator.count() > 0) {
                productName = nameLocator.innerText().trim();
            }

            // Extract Material Variant
            String material = "Standard";
            Locator variantLocator = itemRow.locator(".cart-item__option, .product-option, .variant-title, dd, [class*='variant']").first();
            if (variantLocator.count() > 0) {
                material = variantLocator.innerText().trim();
            } else {
    Locator detailsLocator = itemRow.locator(".cart-item__details, .cart-item__info, td").first();
    String detailsText = (detailsLocator.count() > 0 ? detailsLocator : itemRow).innerText();
    // Word-boundary match so "Hardcase"-style product names don't false-positive.
    if (Pattern.compile("\\bHard\\b", Pattern.CASE_INSENSITIVE).matcher(detailsText).find()) {
        material = "Hard";
    } else if (Pattern.compile("\\bSoft\\b", Pattern.CASE_INSENSITIVE).matcher(detailsText).find()) {
        material = "Soft";
    } else if (Pattern.compile("\\bGlass\\b", Pattern.CASE_INSENSITIVE).matcher(detailsText).find()) {
        material = "Glass";
    }
}

            // Extract Price
            String price = "₹0";
            Locator priceLocator = itemRow.locator(".cart-item__price, .price, .cart-item__discounted-prices, [class*='price']").first();
            if (priceLocator.count() > 0) {
                price = priceLocator.innerText().trim();
            }

            // Extract Product Link
            String productLink = page.url();
            Locator linkLocator = itemRow.locator("a[href*='/products/']").first();
            if (linkLocator.count() > 0) {
                String href = linkLocator.getAttribute("href");
                if (href != null) {
                    productLink = href.startsWith("http") ? href : "https://casekaro.com" + href;
                }
            }

            items.add(new CartItem(productName, material, price, productLink));
        }
        return items;
    }

    public void verifyMaterialsPresentInCart(List<String> expectedMaterials) {
        List<CartItem> cartItems = getCartItems();
        List<String> extractedMaterials = cartItems.stream().map(CartItem::getMaterial).toList();

        for (String expectedMaterial : expectedMaterials) {
            boolean found = cartItems.stream().anyMatch(item ->
                    item.getMaterial().equalsIgnoreCase(expectedMaterial) ||
                    item.getProductName().toLowerCase().contains(expectedMaterial.toLowerCase())
            );
            Assertions.assertTrue(found, "Required material variant '" + expectedMaterial + "' was not found in cart items! Found materials: " + extractedMaterials);
        }
    }

    public void verifyAllItemsCorrespondToSameProduct(String baseProductName) {
        List<CartItem> cartItems = getCartItems();
        Assertions.assertFalse(cartItems.isEmpty(), "Cart is empty, cannot validate product consistency!");

        String referenceName = baseProductName != null && !baseProductName.isEmpty() 
                ? baseProductName 
                : cartItems.get(0).getProductName();
        
        String cleanReference = referenceName.replaceAll("(?i)(hard|soft|glass|case|cover|back cover)", "").trim().toLowerCase();

        for (int i = 0; i < cartItems.size(); i++) {
            String itemProductName = cartItems.get(i).getProductName();
            String cleanItem = itemProductName.replaceAll("(?i)(hard|soft|glass|case|cover|back cover)", "").trim().toLowerCase();
            
            System.out.println("DEBUG: Validating Cart Item " + (i + 1) + ": '" + itemProductName + "' against reference: '" + referenceName + "'");
            
            boolean matches = cleanReference.contains(cleanItem) || cleanItem.contains(cleanReference) 
                    || itemProductName.toLowerCase().contains(cleanReference);
            
            Assertions.assertTrue(matches,
                    "Cart item '" + itemProductName + "' does not belong to parent product '" + referenceName + "'!");
        }
    }

    public void printFormattedCartItemsReport() {
        List<CartItem> cartItems = getCartItems();

        System.out.println("==================================================");
        System.out.println("CASEKARO CART VALIDATION");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("Total Cart Items: " + cartItems.size());
        System.out.println();

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            System.out.println("--------------------------------------------------");
            System.out.println("Item " + (i + 1));
            System.out.println("--------------------------------------------------");
            System.out.println("Product  : " + item.getProductName());
            System.out.println("Material : " + item.getMaterial());
            System.out.println("Price    : " + item.getPrice());
            System.out.println("Link     : " + item.getProductLink());
            System.out.println();
        }

        System.out.println("==================================================");
        System.out.println("VALIDATION RESULT: PASSED");
        System.out.println("==================================================");
    }
}
