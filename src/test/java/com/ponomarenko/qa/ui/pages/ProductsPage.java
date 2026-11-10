package com.ponomarenko.qa.ui.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.ponomarenko.qa.ui.model.Product;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.exactTexts;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;

public class ProductsPage {
    private final SelenideElement title = $("[data-test='title']");
    private final SelenideElement inventory = $("[data-test='inventory-container']");
    private final ElementsCollection products = inventory.$$("[data-test='inventory-item']");
    private final ElementsCollection prices = inventory.$$("[data-test='inventory-item-price']");
    private final SelenideElement sortOrder = $("[data-test='product-sort-container']");
    private final SelenideElement cartLink = $("[data-test='shopping-cart-link']");
    private final SelenideElement cartBadge = $("[data-test='shopping-cart-badge']");

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

    public List<BigDecimal> prices() {
        return prices.shouldHave(sizeGreaterThan(0)).texts().stream()
                .map(ProductsPage::parsePrice)
                .toList();
    }

    public ProductsPage sortByPriceLowToHigh() {
        sortOrder.selectOptionByValue("lohi");
        sortOrder.shouldHave(value("lohi"));
        return this;
    }

    public ProductsPage shouldHavePrices(List<BigDecimal> expectedPrices) {
        List<String> expectedTexts = expectedPrices.stream()
                .map(price -> "$" + price.toPlainString())
                .toList();
        prices.shouldHave(exactTexts(expectedTexts));
        return this;
    }

    public Product addFirstProductToCart() {
        SelenideElement product = products.first().shouldBe(visible);
        String name = product.$("[data-test='inventory-item-name']")
                .shouldBe(visible).shouldNotBe(empty).text();
        BigDecimal price = parsePrice(product.$("[data-test='inventory-item-price']").text());
        Product selectedProduct = new Product(name, price);

        product.$("[data-test^='add-to-cart-']").click();
        product.$("[data-test^='remove-']").shouldBe(visible);
        cartBadge.shouldHave(exactText("1"));
        return selectedProduct;
    }

    public CartPage openCart() {
        cartLink.click();
        return new CartPage();
    }

    private static BigDecimal parsePrice(String priceText) {
        return new BigDecimal(priceText.replace("$", "").trim());
    }
}
