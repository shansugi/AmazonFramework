package com.amazon.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * CucumberRunner — TestNG runner for all BDD feature files.
 *
 * Parallel execution: @DataProvider(parallel = true) makes Cucumber
 * scenarios run in parallel threads. Combined with TestNG parallel="methods"
 * in testng.xml, this gives full parallel BDD execution.
 *
 * Run a specific tag:  mvn test -Dcucumber.filter.tags="@smoke"
 * Run a specific team: mvn test -Dcucumber.filter.tags="@checkout"
 */
@CucumberOptions(
    features  = "src/test/resources/features",
    glue      = {
        "com.amazon.login.stepdefs",
        "com.amazon.product.stepdefs",
        "com.amazon.cart.stepdefs",
        "com.amazon.checkout.stepdefs",
        "com.amazon.tracking.stepdefs",
        "com.amazon.offers.stepdefs",
        "com.amazon.hooks"
    },
    tags      = "${cucumber.filter.tags:@smoke}",
    plugin    = {
        "pretty",
        "html:target/cucumber-reports/report.html",
        "json:target/cucumber-reports/report.json",
        "junit:target/cucumber-reports/report.xml",
        "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
    },
    monochrome = true,
    dryRun    = false
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
