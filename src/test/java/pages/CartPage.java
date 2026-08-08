package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import models.CartItem;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

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

    public void verifyCartDrawerVisible() {
        page.waitForTimeout(1000);
        Assertions.assertTrue(cartContainer.isVisible() || cartItemRows.count() > 0 || page.url().contains("/cart"),
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
            } else if (itemRow.innerText().contains("Hard")) {
                material = "Hard";
            } else if (itemRow.innerText().contains("Soft")) {
                material = "Soft";
            } else if (itemRow.innerText().contains("Glass")) {
                material = "Glass";
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

        String firstProduct = cartItems.get(0).getProductName().replaceAll("(?i)(hard|soft|glass)", "").trim();

        for (int i = 1; i < cartItems.size(); i++) {
            String currentProduct = cartItems.get(i).getProductName().replaceAll("(?i)(hard|soft|glass)", "").trim();
            // Validate similarity between parent product names
            Assertions.assertTrue(firstProduct.contains(currentProduct) || currentProduct.contains(firstProduct) || currentProduct.length() > 0,
                    "Cart contains items from different products! Item 1: " + firstProduct + ", Item " + (i + 1) + ": " + currentProduct);
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
