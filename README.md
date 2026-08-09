# CaseKaro Automation Testing Project

A professional, production-ready E2E Web Test Automation project built for [CaseKaro.com](https://casekaro.com/) using **Java**, **Playwright for Java**, **Cucumber BDD**, and **Maven** following the **Page Object Model (POM)** pattern.

---

## 📌 Project Overview & Objective

This project automates the end-to-end user workflow for searching and adding mobile cover material variants (`Hard`, `Soft`, `Glass`) of a specific phone model (`iPhone 16 Pro`) on CaseKaro.com, validating cart constraints, and extracting dynamic pricing and product information.

### Key Capabilities & Rules Followed:
- **No `try-catch` exception swallowing**: Automation relies on natural assertion failures and Playwright state waiting.
- **No `Thread.sleep()`**: Implements auto-waiting, locator state assertions, and short bounded polling loops (`page.waitForTimeout` inside a retry loop with a max attempt count) only where the site's own AJAX/debounce behavior requires it — never a blind fixed-length sleep.
- **Dynamic locators & extraction**: Prices, URLs, and material options are parsed dynamically from the live DOM at runtime. Cart contents are read from Shopify's own `/cart.js` JSON endpoint rather than scraped from HTML (see *Design Notes* below).
- **Strict negative validation**: Ensures search autocomplete results for "Apple" do not display unrelated brands (e.g., Samsung, Vivo, OnePlus).
- **Exact matching**: Differentiates strictly between `iPhone 16 Pro` and `iPhone 16 Pro Max`.
- **Automatic failure reporting**: Generates Cucumber HTML reports and captures full-page failure screenshots under `target/screenshots/`.
- **Configurable, override-friendly execution**: `config.properties` sets defaults; any value can be overridden per-run via `-D` JVM system properties (e.g. `-Dheadless=true`), without editing the file.

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

### One-Time Setup: Install Playwright Browsers
Playwright drives real browser binaries (Chromium/Firefox/WebKit) that are **not** downloaded by `mvn` automatically. On a fresh clone, compile once and install the browsers once before running any tests:

```bash
mvn compile
mvn exec:java -e -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install --with-deps"
```

**Windows PowerShell users:** the `-D` flag needs different quoting than bash. Use either:
```powershell
mvn exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install"
```
or the stop-parsing token, which is usually the most reliable on Windows:
```powershell
mvn exec:java --% -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=install
```
(Omit `--with-deps` on Windows/macOS — it installs Linux OS-level packages and is only relevant on Linux CI/containers.)

Skipping this step causes every test to fail immediately with `Executable doesn't exist ...ms-playwright...`.

### Application Configuration
Modify `src/test/resources/config/config.properties` to configure execution options:

```properties
browser=chromium       # Options: chromium, firefox, webkit
headless=false         # Set to true for headless CI/CD execution
baseUrl=https://casekaro.com/
slowMo=0               # Slowdown in ms for manual debugging
timeout=30000          # Default locator timeout in ms
```

Any of these can also be overridden per-run from the command line without editing the file (see *Execution Commands* below).

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
PowerShell:
```powershell
mvn clean test "-Dheadless=true"
```

### 3. Cross-Browser Execution
To run tests on Firefox or WebKit:
```bash
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=webkit
```
PowerShell:
```powershell
mvn clean test "-Dbrowser=firefox"
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
6. **First Eligible Product Selection**: Walks the product listing to find the first product that actually exposes all three required material variants, and clicks "Choose Options" (see *Design Notes* below for why this isn't always literally card #1).
7. **Variant Addition**: Adds `Hard`, `Soft`, and `Glass` material variants of the *same* case into the cart, reloading the product page between each add so a subsequent selection can't be intercepted by post-add "you may also like" upsell widgets.
8. **Cart Verification & Data Extraction**: Opens cart, asserts total count == 3 via Shopify's `/cart.js` endpoint, verifies presence of `Hard`, `Soft`, and `Glass`, verifies product identity consistency, and prints formatted output.

---

## 🧠 Design Notes

A few implementation decisions worth understanding if you're reviewing or extending this suite:

- **"First product card" is really "first *eligible* product card."** On the live site, the literal first product returned for `iPhone 16 Pro` does not always expose all three material variants (e.g. a glass-only case). Since the assignment's next requirement is "all 3 materials for the same case," the suite walks forward through the listing and selects the first product that genuinely has Hard, Soft, and Glass available — logging every candidate it skips and why, so the choice is auditable rather than silent.
- **Cart contents are read via Shopify's `/cart.js` JSON API, not scraped from a drawer.** This store doesn't render a traditional mini-cart with per-item DOM rows after an AJAX add-to-cart — confirmed via a live DOM dump during debugging. Reading `/cart.js` directly (`product_title`, `variant_title`, `price`, `url` per line item) is both more reliable and more accurate than guessing at drawer selectors.
- **The product page is reloaded after every add-to-cart.** The AJAX add-to-cart response injects "customers also bought" upsell widgets into the page, which carry their own variant-selection labels. Reloading back to a clean product page before selecting the next material prevents the suite from accidentally clicking a label inside an upsell widget instead of the real product's picker.
- **`-D` command-line overrides (`-Dheadless=true`, `-Dbrowser=firefox`) are read via `System.getProperty()`** in `ConfigReader`, falling back to `config.properties` when not supplied — so CI pipelines can override execution mode without touching the properties file.

---

## 🩹 Troubleshooting

| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| `Executable doesn't exist ...ms-playwright...` | Playwright browser binaries were never downloaded | Run the one-time browser install command above |
| `mvn: command not found` | Maven isn't installed / on PATH | Install Maven 3.8+ |
| `Unknown lifecycle phase` error when running `exec:java` | PowerShell splits `-D` flags differently than bash | Quote the whole `-Dkey=value` pair, or use the `--%` stop-parsing token (see above) |
| Rupee symbol prints as `?` instead of `₹` in the console | Windows PowerShell console isn't using UTF-8 by default | Run `chcp 65001` before `mvn clean test`, or `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8` |
| "Exact iPhone 16 Pro suggestion not found" fails intermittently | The site's predictive search is a genuinely slow AJAX call; a single check can run before results finish rendering | Suite already polls with a bounded retry loop; a rare failure under real network slowness is expected flakiness, not a code defect — rerun |
| Only 1 of 3 materials shows up as a distinct cart item | Add-to-cart didn't reload the product page, so a later material selection got intercepted by an upsell widget | Already fixed — `addSelectedMaterialToCart()` always reloads the product page after each add |

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
Product  : Aquarius Zodiac iPhone 16 Pro Back Cover
Material : Glass
Price    : ₹249
Link     : https://casekaro.com/products/aquarius-zodiac-iphone-16-pro-back-cover?variant=42921002827894

--------------------------------------------------
Item 2
--------------------------------------------------
Product  : Aquarius Zodiac iPhone 16 Pro Back Cover
Material : Soft
Price    : ₹149
Link     : https://casekaro.com/products/aquarius-zodiac-iphone-16-pro-back-cover?variant=42921002860662

--------------------------------------------------
Item 3
--------------------------------------------------
Product  : Aquarius Zodiac iPhone 16 Pro Back Cover
Material : Hard
Price    : ₹99
Link     : https://casekaro.com/products/aquarius-zodiac-iphone-16-pro-back-cover?variant=42921002762358

==================================================
VALIDATION RESULT: PASSED
==================================================
```