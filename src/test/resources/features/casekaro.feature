Feature: CaseKaro End-to-End Mobile Cover Order Flow Automation

  Scenario: Search iPhone 16 Pro and add all material variants of the first case to cart

    Given I navigate to the CaseKaro website
    And the website home page should be loaded successfully

    When I click on Mobile Covers from the top navigation menu
    Then the Mobile Covers section should load successfully

    When I scroll to the Phone cases by model search box
    And I search for "Apple" in the phone model search box
    Then Apple-related phone model suggestions should be visible in search results
    And unrelated phone brands should not be displayed in the search result area

    When I clear the phone model search box
    And I search for "iPhone 16 Pro" in the phone model search box
    Then the autocomplete dropdown should be visible
    And exact "iPhone 16 Pro" suggestion should exist in suggestions
    And "iPhone 16 Pro Max" should not be selected

    When I select the exact "iPhone 16 Pro" suggestion from autocomplete
    Then the product listing for "iPhone 16 Pro" should load successfully
    And at least one product card should be visible

    When I identify the first product card dynamically
    And I click Choose Options on the first product card
    Then material options Hard, Soft, and Glass should be available for the selected case

    When I select the "Hard" material variant
    Then "Hard" material variant should be selected
    When I add the selected variant to the cart
    Then the "Hard" variant should be added to the cart successfully

    When I select the "Soft" material variant
    Then "Soft" material variant should be selected
    When I add the selected variant to the cart
    Then the "Soft" variant should be added to the cart successfully

    When I select the "Glass" material variant
    Then "Glass" material variant should be selected
    When I add the selected variant to the cart
    Then the "Glass" variant should be added to the cart successfully

    When I open the cart
    Then the cart drawer should be visible
    And the cart should contain exactly 3 items
    And the cart should contain "Hard", "Soft", and "Glass" material variants
    And all three cart items should belong to the same parent product
    And I dynamically extract and print material, price, and product link for every cart item
