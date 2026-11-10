package com.ponomarenko.qa.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {
    private final SelenideElement username = $("[data-test='username']");
    private final SelenideElement password = $("[data-test='password']");
    private final SelenideElement loginButton = $("[data-test='login-button']");

    public LoginPage open() {
        Selenide.open("/");
        username.shouldBe(visible);
        return this;
    }

    public ProductsPage loginAs(String username, String password) {
        this.username.setValue(username);
        this.password.setValue(password);
        loginButton.click();
        return new ProductsPage();
    }
}
