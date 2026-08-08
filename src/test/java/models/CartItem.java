package models;

public class CartItem {

    private final String productName;
    private final String material;
    private final String price;
    private final String productLink;

    public CartItem(String productName, String material, String price, String productLink) {
        this.productName = productName;
        this.material = material;
        this.price = price;
        this.productLink = productLink;
    }

    public String getProductName() {
        return productName;
    }

    public String getMaterial() {
        return material;
    }

    public String getPrice() {
        return price;
    }

    public String getProductLink() {
        return productLink;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productName='" + productName + '\'' +
                ", material='" + material + '\'' +
                ", price='" + price + '\'' +
                ", productLink='" + productLink + '\'' +
                '}';
    }
}
