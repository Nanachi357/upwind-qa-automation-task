# Upwind QA Automation Task

Practical assignment for the QA Automation Engineer position at Upwind.

## Coverage

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
- Gradle 9.1.0 through the Gradle Wrapper
- JUnit 5
- Selenide
- REST Assured
- Jackson for JSON request serialization

## Code structure

All Java code is under `src/test/java/com/ponomarenko/qa`:

```text
config/       Shared configuration loader
ui/           Login and product/cart scenarios, browser setup and cleanup
  pages/      Page objects with locators, actions and UI assertions
  model/      Selected product name and price
api/          Booking creation and retrieval scenario
  client/     Request configuration and booking endpoints
  model/      Booking and booking-date records
```

Defaults are in `src/test/resources/test.properties`. Gradle tasks and
dependencies are defined in `build.gradle`. `Dockerfile` and
`docker/google-chrome` configure container execution.

## Prerequisites for local execution

- JDK 21, available through `JAVA_HOME` or `PATH`.
- Internet access to Maven Central, Gradle distributions, Chrome for Testing
  downloads, SauceDemo and Restful Booker.

The Gradle Wrapper supplies Gradle. Selenium Manager downloads and caches
Chrome for Testing **155.0.8059.84** and its matching ChromeDriver automatically.
This browser version matches the CDP support bundled with the current Selenium
dependency. A separately installed Chrome is not required for the default setup.
The first run takes longer because of these downloads.

Verified locally on Windows x64 and in the supplied Docker image on Linux amd64.
For a local Linux setup, Chrome also requires its usual system libraries.

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

## Run with Docker

Docker must be running Linux containers. The image targets `linux/amd64` and
includes Java 21, the Gradle Wrapper, Chrome for Testing **155.0.8059.84** and its
matching ChromeDriver. No local Java, Gradle or browser installation is needed.

From the project root:

```powershell
docker build -t qa-automation-tests .
docker run --rm qa-automation-tests
```

The default command runs all four test cases with headless Chrome. Building the
image compiles the tests without executing them. Internet access to dependency
repositories and the public test services is required for setup and execution.
Keep `ui.headless=true` in this image, which has no display server.

The same image can run individual suites and accept configuration overrides:

```powershell
docker run --rm qa-automation-tests uiTest
docker run --rm qa-automation-tests apiTest
docker run --rm qa-automation-tests uiTest "-Dui.timeoutMs=15000"
```

To keep reports after the container is removed, mount the local `build` directory.
For PowerShell on Windows, run from the project root:

```powershell
New-Item -ItemType Directory -Force build | Out-Null
docker run --rm --mount "type=bind,source=$($PWD.Path)\build,target=/app/build" qa-automation-tests
```

The reports then appear at the paths listed below. Without a mount, they remain
inside the container and are removed along with it. A failed test causes a
non-zero container exit code.

Tests run as the non-root `qa` user. The container's Chrome launcher adds
`--no-sandbox` and `--disable-dev-shm-usage` to work with Docker's process and
shared-memory constraints. The latter uses `/tmp` instead of `/dev/shm`, so the
standard run command does not require a larger shared-memory allocation.
The Chrome version in the Dockerfile must stay aligned with `ui.browserVersion`.

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

- Page objects keep locators, page operations and UI assertions separate from
  the scenarios. Dedicated `data-test` attributes identify controls without
  coupling the tests to visual styling or element positions.
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

## Assumptions and limitations

- The tests exercise public SauceDemo and Restful Booker services and depend on
  their availability and current behavior.
- Sorting assumes SauceDemo's USD price format and at least two different
  product prices. Each UI scenario starts with a fresh browser session.
- The API uses synthetic data and leaves the created bookings in the shared
  demo service. Each case retrieves only the booking it created.
- The supplied Docker image targets Linux amd64 and runs Chrome headlessly.

## Reports

- UI HTML report: `build/reports/tests/uiTest/index.html`.
- API HTML report: `build/reports/tests/apiTest/index.html`.
- All-tests HTML report: `build/reports/tests/test/index.html`.
- JUnit XML results: `build/test-results/<task-name>/`.
- Screenshots and page sources from failed UI tests:
  `build/reports/selenide/`.

## Troubleshooting

On the first Windows browser download, Selenium Manager 4.51.0 can log
`Unable to configure the Chrome sandbox: setup.exe exited with exit code: 78`.
This is a false warning: the [Chrome installer defines code 78 as successful
sandbox configuration](https://github.com/chromium/chromium/blob/155.0.8059.84/chrome/installer/util/util_constants.h),
while [Selenium Manager checks for a zero exit code](https://github.com/SeleniumHQ/selenium/blob/selenium-4.51.0/rust/src/chrome.rs).
Both UI tests passed in a clean setup that emitted this warning.

Gradle reports `compileJava NO-SOURCE` and `processResources NO-SOURCE` because
this project contains only test code and test resources. The selected test task
still executes and reports the individual test results.
