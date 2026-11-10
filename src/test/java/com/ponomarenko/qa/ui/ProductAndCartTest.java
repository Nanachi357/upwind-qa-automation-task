package com.ponomarenko.qa.ui;

import com.ponomarenko.qa.config.TestConfig;
import com.ponomarenko.qa.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductAndCartTest extends BaseUiTest {
    @Test
    @DisplayName("User can sort products by price and add a product to the cart")
    void userCanSortProductsByPriceAndAddAProductToCart() {
        var productsPage = new LoginPage()
                .open()
                .loginAs(TestConfig.get("sauce.username"), TestConfig.get("sauce.password"))
                .shouldBeOpen()
                .shouldContainProducts();

        var initialPrices = productsPage.prices();
        assertTrue(initialPrices.stream().distinct().count() > 1,
                "The catalog must contain at least two different prices to verify sorting");
        var expectedPrices = initialPrices.stream().sorted().toList();

        productsPage.sortByPriceLowToHigh()
                .shouldHavePrices(expectedPrices);
        var selectedProduct = productsPage.addFirstProductToCart();

        productsPage.openCart()
                .shouldBeOpen()
                .shouldContainOnlyProduct(selectedProduct);
    }
}
