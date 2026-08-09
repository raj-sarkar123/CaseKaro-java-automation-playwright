package utils;

import java.util.List;

public class TestData {

    public static final String HOME_URL = ConfigReader.getProperty("baseUrl", "https://casekaro.com/");
    public static final String PHONE_BRAND_SEARCH = "Apple";
    public static final String PHONE_MODEL_SEARCH = "iPhone 16 Pro";
    public static final String UNEXPECTED_MODEL = "iPhone 16 Pro Max";

    public static final String MATERIAL_HARD = "Hard";
    public static final String MATERIAL_SOFT = "Soft";
    public static final String MATERIAL_GLASS = "Glass";

    public static final List<String> UNRELATED_BRANDS = List.of(
            "Samsung", "Vivo", "OnePlus", "Realme", "Xiaomi",
            "Oppo", "Motorola", "Nothing", "Google Pixel", "iQOO"
    );
}
