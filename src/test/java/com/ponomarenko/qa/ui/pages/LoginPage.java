package com.ponomarenko.qa.ui.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.net.URI;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;

public class LoginPage {
    private final SelenideElement username = $("[data-test='username']");
    private final SelenideElement password = $("[data-test='password']");
    private final SelenideElement loginButton = $("[data-test='login-button']");
    private final SelenideElement error = $("[data-test='error']");

    public LoginPage open() {
        Selenide.open("/");
        return shouldBeOpen();
    }

    public LoginPage shouldBeOpen() {
        webdriver().shouldHave(url(URI.create(Configuration.baseUrl).resolve("/").toString()));
        username.shouldBe(visible);
        password.shouldBe(visible);
        loginButton.shouldBe(visible);
        return this;
    }

    public LoginPage submitCredentials(String username, String password) {
        this.username.setValue(username);
        this.password.setValue(password);
        loginButton.click();
        return this;
    }

    public ProductsPage loginAs(String username, String password) {
        submitCredentials(username, password);
        return new ProductsPage();
    }

    public LoginPage shouldShowError(String expectedMessage) {
        error.shouldBe(visible).shouldHave(exactText(expectedMessage));
        return this;
    }
}
