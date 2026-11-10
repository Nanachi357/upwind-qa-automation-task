package com.ponomarenko.qa.ui;

import com.ponomarenko.qa.config.TestConfig;
import com.ponomarenko.qa.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginTest extends BaseUiTest {
    @Test
    @DisplayName("Standard user can log in and see products")
    void standardUserCanLogInAndSeeProducts() {
        var productsPage = new LoginPage()
                .open()
                .loginAs(TestConfig.get("sauce.username"), TestConfig.get("sauce.password"));

        productsPage.shouldBeOpen()
                .shouldContainProducts();
    }
}
