# Amazon Automation Framework
### Java | Selenium 4 | TestNG | Cucumber BDD | Extent Reports | Docker Grid

---

## Project Structure

```
amazon-automation-framework/
│
├── .github/
│   ├── CODEOWNERS                          ← module access control
│   └── workflows/automation.yml            ← GitHub Actions CI/CD
│
├── docker/
│   ├── docker-compose.yml                  ← Selenium Grid (local dev)
│   └── docker-compose.ci.yml              ← CI override (more nodes)
│
├── src/main/java/com/amazon/framework/
│   ├── base/
│   │   ├── BaseTest.java                   ← ThreadLocal WebDriver, lifecycle hooks
│   │   └── BasePage.java                   ← Enforced contract for all page classes
│   ├── config/
│   │   └── ConfigManager.java              ← Env-aware properties loader
│   ├── factory/
│   │   └── DriverFactory.java              ← local / grid / BrowserStack switch
│   ├── listeners/
│   │   ├── ExtentReportManager.java        ← ThreadLocal Extent Reports
│   │   ├── RetryAnalyzer.java              ← Auto-retry failed tests (max 2)
│   │   └── RetryTransformer.java           ← Applies retry globally via testng.xml
│   └── utils/
│       ├── ExcelUtils.java                 ← Apache POI, standard + streaming
│       ├── DatabaseUtils.java              ← HikariCP connection pool + JDBC
│       ├── WaitUtils.java                  ← FluentWait, polling, custom waits
│       └── TestDataUtils.java              ← Java Faker dynamic data generation
│
├── src/test/java/com/amazon/
│   ├── login/
│   │   ├── pages/LoginPage.java            ← @auth-team
│   │   └── stepdefs/
│   │       ├── LoginStepDefs.java
│   │       └── SharedContext.java          ← PicoContainer shared state
│   ├── product/
│   │   ├── pages/ProductPage.java          ← @product-team
│   │   └── stepdefs/ProductStepDefs.java
│   ├── cart/
│   │   ├── pages/CartPage.java             ← @cart-team
│   │   └── stepdefs/CartStepDefs.java
│   ├── checkout/
│   │   ├── pages/CheckoutPage.java         ← @checkout-team
│   │   ├── stepdefs/CheckoutStepDefs.java
│   │   └── tests/OrderFlowTest.java        ← Data-driven E2E test
│   ├── tracking/
│   │   ├── pages/TrackingPage.java         ← @orders-team
│   │   └── stepdefs/TrackingStepDefs.java
│   ├── offers/
│   │   ├── pages/OffersPage.java           ← @offers-team
│   │   └── stepdefs/OffersStepDefs.java
│   ├── hooks/CucumberHooks.java            ← Before/After scenario hooks
│   └── runners/CucumberRunner.java         ← parallel=true DataProvider
│
├── src/test/resources/
│   ├── config/
│   │   ├── local.properties               ← developer defaults
│   │   └── staging.properties             ← CI/staging environment
│   ├── features/
│   │   ├── login/Login.feature
│   │   ├── product/ProductSearch.feature
│   │   ├── cart/Cart.feature
│   │   ├── checkout/Checkout.feature
│   │   ├── tracking/TrackOrder.feature
│   │   └── offers/Offers.feature
│   ├── suites/
│   │   ├── smoke-suite.xml                ← Every PR, 5 threads, ~10 min
│   │   ├── regression-suite.xml           ← Nightly, 10 threads, all modules
│   │   ├── checkout-suite.xml             ← Checkout team PRs only
│   │   └── auth-suite.xml                 ← Auth team PRs only
│   ├── testdata/
│   │   ├── orders.xlsx                    ← E2E order data (create manually)
│   │   └── products.xlsx                  ← Search keywords + expected results
│   └── extent.properties                  ← Extent Reports adapter config
│
├── pom.xml
├── Jenkinsfile
└── README.md
```

---

## Quick Start

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- Docker Desktop (for Grid)
- Chrome/Firefox installed (for local runs)

### 2. Run locally (no Docker needed)
```bash
# Smoke tests — local Chrome
mvn test -Denv=local -Dbrowser=chrome -Dsuite=suites/smoke-suite.xml

# Specific module only
mvn test -Denv=local -Dcucumber.filter.tags="@checkout"

# Data-driven E2E test only
mvn test -Denv=local -Dtest=OrderFlowTest
```

### 3. Run on Docker Selenium Grid
```bash
# Start Grid
cd docker && docker-compose up -d

# Wait for: http://localhost:4444/ui shows nodes registered

# Run regression on Grid with 10 threads
mvn test \
  -Denv=local \
  -Dbrowser=chrome \
  -Dsuite=suites/regression-suite.xml \
  -Dheadless=true \
  -Dhub.url=http://localhost:4444/wd/hub \
  -Dthreads=10

# Tear down
cd docker && docker-compose down
```

### 4. Run on BrowserStack
```bash
export BS_USERNAME=your_username
export BS_ACCESS_KEY=your_key

mvn test -Denv=cloud -Dbrowser=safari -Dsuite=suites/smoke-suite.xml
```

---

## All Maven Properties

| Property | Default | Description |
|---|---|---|
| `-Denv` | `local` | `local` / `grid` / `cloud` |
| `-Dbrowser` | `chrome` | `chrome` / `firefox` / `edge` / `safari` / `ios` |
| `-Dheadless` | `false` | `true` in CI |
| `-Dhub.url` | `http://localhost:4444/wd/hub` | Grid hub URL |
| `-Dthreads` | `5` | Parallel thread count |
| `-Dsuite` | `suites/smoke-suite.xml` | TestNG suite file |
| `-Dcucumber.filter.tags` | `@smoke` | Cucumber tag filter |
| `-Dhighlight` | `false` | Set `true` to flash elements red on click |

---

## Test Tags Reference

| Tag | Meaning | When to run |
|---|---|---|
| `@smoke` | Critical path | Every PR |
| `@regression` | Full coverage | Nightly |
| `@login` | Auth module | Auth team PR |
| `@product` | Product/search | Product team PR |
| `@cart` | Cart module | Cart team PR |
| `@checkout` | Checkout flow | Checkout team PR |
| `@tracking` | Order tracking | Orders team PR |
| `@offers` | Deals/offers | Offers team PR |
| `@p1` | Priority 1 | Always |
| `@p2` | Priority 2 | Nightly |
| `@p3` | Priority 3 | Weekly |
| `@wip` | Work in progress | Skip in CI (`not @wip`) |

---

## Reports

After test run, open:
- **Extent Report**: `target/extent-reports/AmazonTestReport.html`
- **Cucumber Report**: `target/cucumber-reports/report.html`
- **Screenshots**: `target/screenshots/`
- **Logs**: `target/logs/amazon-framework.log`

---

## Module Ownership (CODEOWNERS)

| Module | Package | Team |
|---|---|---|
| Core framework | `src/main/java/com/amazon/framework/` | `@framework-lead` |
| Login / Auth | `src/test/java/com/amazon/login/` | `@auth-team` |
| Product search | `src/test/java/com/amazon/product/` | `@product-team` |
| Cart | `src/test/java/com/amazon/cart/` | `@cart-team` |
| Checkout | `src/test/java/com/amazon/checkout/` | `@checkout-team` |
| Order tracking | `src/test/java/com/amazon/tracking/` | `@orders-team` |
| Offers / Deals | `src/test/java/com/amazon/offers/` | `@offers-team` |

A PR touching `checkout/` from an auth-team member cannot merge without
`@checkout-team` approval. Enforced via GitHub branch protection + CODEOWNERS.

---

## Adding Test Data (Excel)

Create `src/test/resources/testdata/orders.xlsx`:

| email | password | searchProduct | deliveryName | phone | address | city | state | pincode |
|---|---|---|---|---|---|---|---|---|
| user@test.com | Pass@123 | wireless mouse | John Smith | 9876543210 | 123 MG Road | Bangalore | Karnataka | 560001 |

The `OrderFlowTest.@DataProvider` reads this file and runs one parallel test per row.
