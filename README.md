# Upwind QA Automation Task

Practical assignment for the QA Automation Engineer position at Upwind.

## Current coverage

One SauceDemo UI test logs in with the provided credentials, checks the Products
page URL and heading, and verifies that the page contains a visible product
with a non-empty name.

## Stack

- Java 21
- Gradle Wrapper
- JUnit 5
- Selenide
- REST Assured

REST Assured is included as a dependency. There are currently no API tests.

## Prerequisites

- JDK 21, available through `JAVA_HOME` or `PATH`.
- Internet access to Maven Central, Gradle distributions, Chrome for Testing
  downloads and SauceDemo.

The Gradle Wrapper supplies Gradle. Selenium Manager downloads and caches
Chrome for Testing **155.0.8059.84** and its matching ChromeDriver automatically.
This browser version matches the CDP support bundled with the current Selenium
dependency. A separately installed Chrome is not required for the default setup.
The first run takes longer because of these downloads.

Verified locally on Windows x64. On Linux, Chrome also requires its usual system
libraries to be installed.

## Run the tests

Run these PowerShell commands from the project root:

```powershell
# Run the UI suite
.\gradlew.bat uiTest

# Run only the login test
.\gradlew.bat uiTest --tests com.ponomarenko.qa.ui.LoginTest

# Run all implemented tests
.\gradlew.bat test

# Clean build and run all implemented tests
.\gradlew.bat clean build
```

On macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`.
The `apiTest` task currently reports `NO-SOURCE` because its suite is empty.
Test tasks execute on every invocation, since the external application can change
without any changes to the project files.

## Configuration

Defaults are in `src/test/resources/test.properties`. JVM system properties
override them. Gradle forwards properties starting with `ui.`, `api.` or `sauce.`
to the test JVM.

```powershell
# Show the browser window
.\gradlew.bat uiTest "-Dui.headless=false"

# Change the maximum condition wait
.\gradlew.bat uiTest "-Dui.timeoutMs=15000"
```

Keep `-D` arguments quoted in PowerShell.

| UI setting | Default |
| --- | --- |
| `ui.baseUrl` | `https://www.saucedemo.com` |
| `ui.browser` | `chrome` |
| `ui.browserVersion` | `155.0.8059.84` |
| `ui.headless` | `true` |
| `ui.browserSize` | `1440x900` |
| `ui.timeoutMs` | `10000` milliseconds |
| `sauce.username` | `standard_user` |
| `sauce.password` | `secret_sauce` |

The credentials are public test credentials supplied in the assignment.
Changing the browser version requires checking its compatibility with Selenium.

## Test approach

- Page objects keep the dedicated `data-test` locators and page operations
  separate from the test scenario.
- Selenide conditions wait for the expected URL, visible elements, heading text
  and a non-empty product collection. There are no fixed sleeps.
- `BaseUiTest` applies browser configuration before each test and closes the
  browser afterward, including failed tests. Each test gets a fresh session.
- Tests currently run sequentially.
- SLF4J Simple provides Selenide logs, captured in the Gradle test reports.
  WebDriver BiDi is enabled for Selenide's browser-log collection.

The tests exercise the public SauceDemo site and depend on its availability.

## Reports

- UI HTML report: `build/reports/tests/uiTest/index.html`.
- All-tests HTML report: `build/reports/tests/test/index.html`.
- JUnit XML results: `build/test-results/<task-name>/`.
- Screenshots and page sources from failed Selenide checks:
  `build/reports/tests/`.
