# CaseKaro Automation Testing Project

A professional, production-ready E2E Web Test Automation project built for [CaseKaro.com](https://casekaro.com/) using **Java**, **Playwright for Java**, **Cucumber BDD**, and **Maven** following the **Page Object Model (POM)** pattern.

---

## 📌 Project Overview & Objective

This project automates the end-to-end user workflow for searching and adding mobile cover material variants (`Hard`, `Soft`, `Glass`) of a specific phone model (`iPhone 16 Pro`) on CaseKaro.com, validating cart constraints, and extracting dynamic pricing and product information.

### Key Capabilities & Rules Followed:
- **No `try-catch` exception swallowing**: Automation relies on natural assertion failures and Playwright state waiting.
- **No `Thread.sleep()`**: Implements auto-waiting and locator state assertions.
- **Dynamic locators & extraction**: Prices, URLs, and material options are parsed dynamically from the live DOM at runtime.
- **Strict negative validation**: Ensures search autocomplete results for "Apple" do not display unrelated brands (e.g., Samsung, Vivo, OnePlus).
- **Exact matching**: Differentiates strictly between `iPhone 16 Pro` and `iPhone 16 Pro Max`.
- **Automatic failure reporting**: Generates Cucumber HTML reports and captures full-page failure screenshots under `target/screenshots/`.

---

## 🛠️ Technology Stack

| Technology | Purpose |
| :--- | :--- |
| **Java 17** | Core programming language |
| **Playwright for Java (v1.44.0)** | Fast, reliable web automation engine |
| **Cucumber BDD (v7.18.0)** | Business-readable Given/When/Then feature specifications |
| **JUnit 5 / Platform Suite** | Test execution framework |
| **Maven** | Build and dependency management tool |
| **Page Object Model (POM)** | Clean separation of UI locators and business actions |

---

## 📂 Project Architecture

```
casekaro-playwright-automation/
│
├── pom.xml
├── README.md
├── .gitignore
│
├── src/
│   └── test/
│       ├── java/
│       │   ├── pages/
│       │   │   ├── BasePage.java
│       │   │   ├── HomePage.java
│       │   │   ├── MobileCoversPage.java
│       │   │   ├── ProductPage.java
│       │   │   └── CartPage.java
│       │   │
│       │   ├── stepdefinitions/
│       │   │   └── CaseKaroSteps.java
│       │   │
│       │   ├── runners/
│       │   │   └── TestRunner.java
│       │   │
│       │   ├── hooks/
│       │   │   └── Hooks.java
│       │   │
│       │   ├── models/
│       │   │   └── CartItem.java
│       │   │
│       │   └── utils/
│       │       ├── BrowserManager.java
│       │       ├── ConfigReader.java
│       │       └── TestData.java
│       │
│       └── resources/
│           ├── features/
│           │   └── casekaro.feature
│           │
│           └── config/
│               └── config.properties
│
└── target/
    ├── cucumber-reports/
    │   └── cucumber-html-report.html
    └── screenshots/
```

---

## ⚙️ Configuration & Setup

### Prerequisites
1. **Java JDK 17+** installed (`java -version`).
2. **Apache Maven 3.8+** installed (`mvn -version` or portable Maven).

### Application Configuration
Modify `src/test/resources/config/config.properties` to configure execution options:

```properties
browser=chromium       # Options: chromium, firefox, webkit
headless=false         # Set to true for headless CI/CD execution
baseUrl=https://casekaro.com/
slowMo=0               # Slowdown in ms for manual debugging
timeout=30000          # Default locator timeout in ms
```

---

## 🚀 Execution Commands

To compile and execute the complete test suite:

### 1. Headed Mode Execution (Default)
```bash
mvn clean test
```

### 2. Headless Mode Execution (CI/CD)
You can override properties directly from the command line:
```bash
mvn clean test -Dheadless=true
```

### 3. Cross-Browser Execution
To run tests on Firefox or WebKit:
```bash
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=webkit
```

---

## 📊 Test Reports & Screenshots

- **Cucumber HTML Report**: Located at `target/cucumber-reports/cucumber-html-report.html`
- **Cucumber JSON Report**: Located at `target/cucumber-reports/cucumber.json`
- **Failure Screenshots**: If any scenario fails, a full-page PNG screenshot is automatically saved in `target/screenshots/` and embedded into the HTML report.

---

## 📝 Test Flow Walkthrough

1. **Navigate to CaseKaro**: Opens `https://casekaro.com/` and validates successful page load.
2. **Mobile Covers**: Clicks "Mobile Covers" from top header menu; asserts section loaded.
3. **Phone Model Search**: Scrolls to "Phone cases by model" search field.
4. **Apple Brand Search & Scoped Negative Validation**: Types `Apple`, waits for autocomplete suggestions, and verifies unrelated brands (Samsung, Vivo, OnePlus, etc.) are NOT present in suggestions.
5. **Exact Autocomplete Selection**: Types `iPhone 16 Pro` and selects exact `iPhone 16 Pro` item, validating that `iPhone 16 Pro Max` is NOT selected.
6. **First Product Selection**: Identifies the first product card dynamically and clicks "Choose Options".
7. **Variant Addition**: Adds `Hard`, `Soft`, and `Glass` material variants of the *same* case into the cart.
8. **Cart Verification & Data Extraction**: Opens cart, asserts total count == 3, verifies presence of `Hard`, `Soft`, and `Glass`, verifies product identity consistency, and prints formatted output.

---

## 📌 Formatted Console Output Example

```text
==================================================
CASEKARO CART VALIDATION
==================================================

Total Cart Items: 3

--------------------------------------------------
Item 1
--------------------------------------------------
Product  : iPhone 16 Pro Back Cover
Material : Hard
Price    : ₹69
Link     : https://casekaro.com/products/iphone-16-pro-back-cover-hard

--------------------------------------------------
Item 2
--------------------------------------------------
Product  : iPhone 16 Pro Back Cover
Material : Soft
Price    : ₹99
Link     : https://casekaro.com/products/iphone-16-pro-back-cover-soft

--------------------------------------------------
Item 3
--------------------------------------------------
Product  : iPhone 16 Pro Back Cover
Material : Glass
Price    : ₹149
Link     : https://casekaro.com/products/iphone-16-pro-back-cover-glass

==================================================
VALIDATION RESULT: PASSED
==================================================
```
