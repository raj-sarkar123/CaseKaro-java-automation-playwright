package stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.CartPage;
import pages.HomePage;
import pages.MobileCoversPage;
import pages.ProductPage;
import utils.BrowserManager;

import java.util.List;

public class CaseKaroSteps {

    private HomePage homePage;
    private MobileCoversPage mobileCoversPage;
    private ProductPage productPage;
    private CartPage cartPage;

    private String selectedProductName = "";

    private void initPages() {
        if (homePage == null) {
            homePage = new HomePage(BrowserManager.getPage());
            mobileCoversPage = new MobileCoversPage(BrowserManager.getPage());
            productPage = new ProductPage(BrowserManager.getPage());
            cartPage = new CartPage(BrowserManager.getPage());
        }
    }

    @Given("I navigate to the CaseKaro website")
    public void iNavigateToCaseKaroWebsite() {
        initPages();
        homePage.navigateToHomePage();
    }

    @Given("the website home page should be loaded successfully")
    public void verifyHomePageLoaded() {
        initPages();
        homePage.verifyHomePageLoaded();
    }

    @When("I click on Mobile Covers from the top navigation menu")
    public void iClickOnMobileCovers() {
        initPages();
        homePage.clickMobileCovers();
    }

    @Then("the Mobile Covers section should load successfully")
    public void verifyMobileCoversSectionLoaded() {
        initPages();
        mobileCoversPage.verifyMobileCoversPageLoaded();
    }

    @When("I scroll to the Phone cases by model search box")
    public void scrollToSearchBox() {
        initPages();
        mobileCoversPage.scrollToPhoneCasesByModelSection();
    }

    @When("I search for {string} in the phone model search box")
    public void searchForPhoneModel(String searchKey) {
        initPages();
        mobileCoversPage.searchPhoneModel(searchKey);
    }

    @Then("Apple-related phone model suggestions should be visible in search results")
    public void verifyAppleSearchResults() {
        initPages();
        mobileCoversPage.verifyAppleSearchResults();
    }

    @Then("unrelated phone brands should not be displayed in the search result area")
    public void verifyNoOtherBrandsInSearchResults() {
        initPages();
        mobileCoversPage.verifyNoOtherBrandsVisibleInSearchResults();
    }

    @When("I clear the phone model search box")
    public void clearPhoneModelSearch() {
        initPages();
        mobileCoversPage.clearPhoneModelSearch();
    }

    @Then("the autocomplete dropdown should be visible")
    public void verifyAutocompleteVisible() {
        initPages();
        mobileCoversPage.verifyAutocompleteVisible();
    }

    @Then("exact {string} suggestion should exist in suggestions")
    public void verifyExactSuggestionExists(String exactModel) {
        initPages();
        mobileCoversPage.verifyExactPhoneModelSuggestionVisible(exactModel);
    }

    @Then("{string} should not be selected")
    public void verifyModelNotSelected(String unexpectedModel) {
        initPages();
        mobileCoversPage.verifyPhoneModelMaxNotSelected(unexpectedModel);
    }

    @When("I select the exact {string} suggestion from autocomplete")
    public void selectExactSuggestion(String exactModel) {
        initPages();
        mobileCoversPage.selectExactPhoneModelSuggestion(exactModel);
    }

    @Then("the product listing for {string} should load successfully")
    public void verifyProductListingLoaded(String model) {
        initPages();
        productPage.verifyProductListingLoaded();
    }

    @Then("at least one product card should be visible")
    public void verifyAtLeastOneProductCardVisible() {
        initPages();
        productPage.verifyAtLeastOneProductCardVisible();
    }

    @When("I identify the first product card dynamically")
    public void identifyFirstProductCard() {
        initPages();
        productPage.verifyAtLeastOneProductCardVisible();
    }

    @When("I click Choose Options on the first product card")
    public void clickChooseOptionsOnFirstProduct() {
        initPages();
        productPage.clickFirstProductChooseOptions();
        selectedProductName = productPage.getCapturedProductName();
    }

    @Then("material options Hard, Soft, and Glass should be available for the selected case")
    public void verifyAllMaterialsAvailable() {
        initPages();
        productPage.verifyMaterialAvailable("Hard");
        productPage.verifyMaterialAvailable("Soft");
        productPage.verifyMaterialAvailable("Glass");
    }

    @When("I select the {string} material variant")
    public void selectMaterialVariant(String material) {
        initPages();
        productPage.selectMaterial(material);
    }

    @Then("{string} material variant should be selected")
    public void verifyMaterialVariantSelected(String material) {
        initPages();
        productPage.verifyMaterialSelected(material);
    }

    private int cartCountBeforeAdd = 0;

    @When("I add the selected variant to the cart")
    public void addSelectedVariantToCart() {
        initPages();
        cartCountBeforeAdd = cartPage.getCurrentCartItemCount();
        productPage.addSelectedMaterialToCart();
    }

    @Then("the {string} variant should be added to the cart successfully")
    public void verifyVariantAddedToCart(String material) {
        initPages();
        cartPage.verifyCartItemCountIncreasedFrom(cartCountBeforeAdd);
        productPage.closeCartDrawerIfOpen();
    }

    @When("I open the cart")
    public void openCart() {
        initPages();
        cartPage.openCart();
    }

    @Then("the cart drawer should be visible")
    public void verifyCartDrawerVisible() {
        initPages();
        cartPage.verifyCartDrawerVisible();
    }

    @Then("the cart should contain exactly {int} items")
    public void verifyCartItemCount(int expectedCount) {
        initPages();
        cartPage.verifyCartItemCount(expectedCount);
    }

    @Then("the cart should contain {string}, {string}, and {string} material variants")
    public void verifyMaterialsInCart(String m1, String m2, String m3) {
        initPages();
        cartPage.verifyMaterialsPresentInCart(List.of(m1, m2, m3));
    }

    @Then("all three cart items should belong to the same parent product")
    public void verifyAllItemsBelongToSameProduct() {
        initPages();
        cartPage.verifyAllItemsCorrespondToSameProduct(selectedProductName);
    }

    @Then("I dynamically extract and print material, price, and product link for every cart item")
    public void extractAndPrintCartItems() {
        initPages();
        cartPage.printFormattedCartItemsReport();
    }
}
