package com.ponomarenko.qa.ui.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.ponomarenko.qa.ui.model.Product;

import java.net.URI;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;

public class CartPage {
    private final SelenideElement title = $("[data-test='title']");
    private final SelenideElement cart = $("[data-test='cart-contents-container']");
    private final ElementsCollection products = cart.$$("[data-test='inventory-item']");

    public CartPage shouldBeOpen() {
        String expectedUrl = URI.create(Configuration.baseUrl).resolve("/cart.html").toString();
        webdriver().shouldHave(url(expectedUrl));
        title.shouldBe(visible).shouldHave(exactText("Your Cart"));
        cart.shouldBe(visible);
        return this;
    }

    public CartPage shouldContainOnlyProduct(Product expectedProduct) {
        SelenideElement product = products.shouldHave(size(1)).first().shouldBe(visible);
        product.$("[data-test='inventory-item-name']")
                .shouldBe(visible).shouldHave(exactText(expectedProduct.name()));
        product.$("[data-test='inventory-item-price']")
                .shouldBe(visible).shouldHave(exactText("$" + expectedProduct.price().toPlainString()));
        return this;
    }
}
