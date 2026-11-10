package com.ponomarenko.qa.ui;

import com.codeborne.selenide.Configuration;
import com.ponomarenko.qa.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.MutableCapabilities;

import static com.codeborne.selenide.Selenide.closeWebDriver;

abstract class BaseUiTest {
    @BeforeEach
    void configureBrowser() {
        Configuration.baseUrl = TestConfig.get("ui.baseUrl");
        Configuration.browser = TestConfig.get("ui.browser");
        Configuration.browserVersion = TestConfig.get("ui.browserVersion");
        Configuration.headless = TestConfig.getBoolean("ui.headless");
        Configuration.browserSize = TestConfig.get("ui.browserSize");
        Configuration.timeout = TestConfig.getPositiveLong("ui.timeoutMs");
        // Selenide uses WebDriver BiDi when collecting browser logs.
        Configuration.browserCapabilities = new MutableCapabilities();
        Configuration.browserCapabilities.setCapability("webSocketUrl", true);
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }
}
