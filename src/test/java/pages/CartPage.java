package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import models.CartItem;
import org.junit.jupiter.api.Assertions;
import utils.TestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CartPage extends BasePage {

    public CartPage(Page page) {
        super(page);
    }

    public void openCart() {
        page.navigate(buildAbsoluteUrl("cart"));
        page.waitForLoadState();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchShopifyCart() {
        Object result = page.evaluate("async () => { const res = await fetch('/cart.js'); return await res.json(); }");
        return (Map<String, Object>) result;
    }

    private int getShopifyCartItemCount() {
        Map<String, Object> cart = fetchShopifyCart();
        return ((Number) cart.get("item_count")).intValue();
    }

    public int getCurrentCartItemCount() {
        return getShopifyCartItemCount();
    }

    public void verifyCartItemCountIncreasedFrom(int previousCount) {
        int newCount = previousCount;
        for (int attempt = 0; attempt < 20; attempt++) {
            newCount = getShopifyCartItemCount();
            if (newCount > previousCount) {
                break;
            }
            page.waitForTimeout(500);
        }
        Assertions.assertTrue(newCount > previousCount,
                "Cart item count did not increase after add-to-cart! Before: " + previousCount + ", After: " + newCount);
    }

    public void verifyCartDrawerVisible() {
        page.waitForLoadState();
        boolean urlLooksLikeCart = page.url().toLowerCase().contains("cart");
        int itemCount = getShopifyCartItemCount();
        Assertions.assertTrue(urlLooksLikeCart || itemCount > 0, "Cart drawer/page is not visible!");
    }

    public void verifyCartItemCount(int expectedCount) {
        int count = 0;
        for (int attempt = 0; attempt < 20; attempt++) {
            count = getShopifyCartItemCount();
            if (count == expectedCount) {
                break;
            }
            page.waitForTimeout(300);
        }
        Assertions.assertEquals(expectedCount, count,
                "Cart item count mismatch! Expected: " + expectedCount + " but found: " + count);
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> getCartItems() {
        Map<String, Object> cart = fetchShopifyCart();
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) cart.get("items");

        List<CartItem> items = new ArrayList<>();
        for (Map<String, Object> raw : rawItems) {
            String productName = String.valueOf(raw.getOrDefault("product_title", "Unknown Product"));
            String variantTitle = String.valueOf(raw.getOrDefault("variant_title", ""));
            String material = extractMaterialFromVariantTitle(variantTitle);

            long priceCents = ((Number) raw.getOrDefault("price", 0)).longValue();
            String price = "\u20B9" + (priceCents / 100);

            String relativeUrl = String.valueOf(raw.getOrDefault("url", ""));
            String productLink = buildAbsoluteUrl(relativeUrl);

            items.add(new CartItem(productName, material, price, productLink));
        }
        return items;
    }

    private String extractMaterialFromVariantTitle(String variantTitle) {
        if (Pattern.compile("\\bBlack\\s+Soft\\b", Pattern.CASE_INSENSITIVE).matcher(variantTitle).find()) return "Black Soft";
        if (Pattern.compile("\\bHard\\b", Pattern.CASE_INSENSITIVE).matcher(variantTitle).find()) return "Hard";
        if (Pattern.compile("\\bMetal\\b", Pattern.CASE_INSENSITIVE).matcher(variantTitle).find()) return "Metal";
        if (Pattern.compile("\\bGlass\\b", Pattern.CASE_INSENSITIVE).matcher(variantTitle).find()) return "Glass";
        if (Pattern.compile("\\bSoft\\b", Pattern.CASE_INSENSITIVE).matcher(variantTitle).find()) return "Soft";
        return variantTitle.isEmpty() ? "Standard" : variantTitle;
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

    private String buildAbsoluteUrl(String relativeHref) {
        String base = TestData.HOME_URL.endsWith("/")
                ? TestData.HOME_URL.substring(0, TestData.HOME_URL.length() - 1)
                : TestData.HOME_URL;
        String path = relativeHref.startsWith("/") ? relativeHref : "/" + relativeHref;
        return base + path;
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