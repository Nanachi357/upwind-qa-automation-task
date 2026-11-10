# Upwind QA Automation Task

Practical assignment for the QA Automation Engineer position at Upwind.

## Current coverage

Three scenarios are implemented:

1. Log in with the provided credentials, check the Products page URL and heading,
   and verify that the page contains a visible product with a non-empty name.
2. Log in independently, sort all products by price from low to high, add a
   product to the cart, and verify its name and price in the cart.
3. Create a Restful Booker booking, retrieve it by the returned ID, and verify
   the stored booking data through REST Assured. This scenario runs for both
   `depositpaid=true` and `depositpaid=false`, giving four test cases in total.

## Stack

- Java 21
- Gradle Wrapper
- JUnit 5
- Selenide
- REST Assured
- Jackson for JSON request serialization

## Prerequisites

- JDK 21, available through `JAVA_HOME` or `PATH`.
- Internet access to Maven Central, Gradle distributions, Chrome for Testing
  downloads, SauceDemo and Restful Booker.

The Gradle Wrapper supplies Gradle. Selenium Manager downloads and caches
Chrome for Testing **155.0.8059.84** and its matching ChromeDriver automatically.
This browser version matches the CDP support bundled with the current Selenium
dependency. A separately installed Chrome is not required for the default setup.
The first run takes longer because of these downloads.

On the first Windows browser download, Selenium Manager 4.51.0 can log
`Unable to configure the Chrome sandbox: setup.exe exited with exit code: 78`.
This is a false warning: the [Chrome installer defines code 78 as successful
sandbox configuration](https://github.com/chromium/chromium/blob/155.0.8059.84/chrome/installer/util/util_constants.h),
while [Selenium Manager checks for a zero exit code](https://github.com/SeleniumHQ/selenium/blob/selenium-4.51.0/rust/src/chrome.rs).
Both UI tests passed in a clean setup that emitted this warning.

Verified locally on Windows x64. On Linux, Chrome also requires its usual system
libraries to be installed.

## Run the tests

Run these PowerShell commands from the project root:

```powershell
# Run the UI suite
.\gradlew.bat uiTest

# Run only the login test
.\gradlew.bat uiTest --tests com.ponomarenko.qa.ui.LoginTest

# Run only the sorting and cart test
.\gradlew.bat uiTest --tests com.ponomarenko.qa.ui.ProductAndCartTest

# Run the API suite
.\gradlew.bat apiTest

# Run all implemented tests
.\gradlew.bat test

# Clean build and run all implemented tests
.\gradlew.bat clean build
```

On macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`.
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

# Override API connection and read timeouts
.\gradlew.bat apiTest "-Dapi.timeoutMs=20000"
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

| API setting | Default |
| --- | --- |
| `api.baseUrl` | `https://restful-booker.herokuapp.com` |
| `api.timeoutMs` | `15000` milliseconds |

The API timeout applies separately to connecting, acquiring a connection and
reading a response.

## Test approach

- Page objects keep the dedicated `data-test` locators and page operations
  separate from the test scenario.
- Selenide conditions wait for the expected URL, visible elements, heading text
  and a non-empty product collection. There are no fixed sleeps.
- The sorting test reads the initial prices as `BigDecimal`, sorts that snapshot
  numerically and checks the full displayed price sequence, including duplicate
  prices and the number of products. It requires at least two different prices
  so that the ordering check is meaningful. Prices use the site's USD format.
- Product details are captured before adding the item. The cart must contain
  exactly that one product with the same case-sensitive name and price. The
  price element must be visible and non-empty before its value is read.
- `BaseUiTest` applies browser configuration before each test and closes the
  browser afterward, including failed tests. Each test gets a fresh session.
- When a browser is available, Selenide's JUnit extension captures a screenshot
  and page source before cleanup if a UI test fails, including ordinary JUnit
  assertion failures.
- The API test creates a fresh synthetic booking on each run, with a unique
  name and future dates calculated once in UTC. It uses only its own returned ID.
  Both deposit flag values are exercised to detect an API that always returns
  a fixed boolean value.
- POST and GET must both return HTTP 200 and JSON. The ID is checked in the POST
  response and used in the GET URL, since the GET body does not repeat the ID.
  The retrieved name, price, deposit flag, both dates and additional needs are
  compared with the submitted values.
- API requests use typed records serialized by Jackson. `BookingClient` creates
  a separate request specification for each call, with explicit JSON headers and
  timeouts. REST Assured logs the request and response when a validation fails.
  `Accept` is sent as exactly `application/json`, since Booker rejects the
  multi-value header produced by REST Assured's `ContentType.JSON` overload.
- API assertion messages identify POST or GET and include the generated name or
  booking ID. GET field validations are grouped so one failure reports all
  mismatched fields together.
- Tests currently run sequentially.
- SLF4J Simple provides Selenide logs, captured in the Gradle test reports.
  WebDriver BiDi is enabled for Selenide's browser-log collection.

The tests exercise public SauceDemo and Restful Booker services and depend on
their availability. The API test creates data in the shared demo service.

## Reports

- UI HTML report: `build/reports/tests/uiTest/index.html`.
- API HTML report: `build/reports/tests/apiTest/index.html`.
- All-tests HTML report: `build/reports/tests/test/index.html`.
- JUnit XML results: `build/test-results/<task-name>/`.
- Screenshots and page sources from failed UI tests:
  `build/reports/selenide/`.
