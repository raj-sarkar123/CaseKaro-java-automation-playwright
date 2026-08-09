package utils;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class ConfigReader {

    private static final Properties properties = loadProperties();

    private static Properties loadProperties() {
        Properties temp = new Properties();
        InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream("config/config.properties");
        if (inputStream == null) {
            throw new RuntimeException("config.properties file not found in resources/config/");
        }
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                String[] parts = line.split("=", 2);
                temp.setProperty(parts[0].trim(), parts[1].trim());
            }
        }
        scanner.close();
        return temp;
    }

   public static String getProperty(String key) {
    String systemValue = System.getProperty(key);
    return systemValue != null ? systemValue : properties.getProperty(key);
}

public static String getProperty(String key, String defaultValue) {
    String systemValue = System.getProperty(key);
    if (systemValue != null) {
        return systemValue;
    }
    return properties.getProperty(key, defaultValue);
}

public static boolean getBooleanProperty(String key, boolean defaultValue) {
    String systemValue = System.getProperty(key);
    if (systemValue != null) {
        return Boolean.parseBoolean(systemValue);
    }
    String value = properties.getProperty(key);
    return value != null ? Boolean.parseBoolean(value) : defaultValue;
}
}
