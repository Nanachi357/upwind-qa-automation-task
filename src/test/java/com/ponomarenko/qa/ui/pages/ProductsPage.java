package com.ponomarenko.qa.ui.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.net.URI;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;

public class ProductsPage {
    private final SelenideElement title = $("[data-test='title']");
    private final SelenideElement inventory = $("[data-test='inventory-container']");
    private final ElementsCollection products = inventory.$$("[data-test='inventory-item']");

    public ProductsPage shouldBeOpen() {
        String expectedUrl = URI.create(Configuration.baseUrl).resolve("/inventory.html").toString();
        webdriver().shouldHave(url(expectedUrl));
        title.shouldBe(visible).shouldHave(exactText("Products"));
        inventory.shouldBe(visible);
        return this;
    }

    public ProductsPage shouldContainProducts() {
        products.shouldHave(sizeGreaterThan(0));
        products.first().$("[data-test='inventory-item-name']")
                .shouldBe(visible)
                .shouldNotBe(empty);
        return this;
    }
}
