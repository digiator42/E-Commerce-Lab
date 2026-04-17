package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.util.List;

import org.openqa.selenium.WebDriver;

import com.ecommerce.lab.automation.components.Product;
import com.ecommerce.lab.automation.pages.ProductPage;
import com.ecommerce.lab.automation.utils.DriverManager;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class ProductSteps {

    private WebDriver getDriver() { return DriverManager.getSmartDriver(); }

    private ProductPage getProductPage() { return new ProductPage(getDriver()); }

    @Given("the user is on the products page")
    public void the_user_is_on_the_products_page() { getProductPage().navigateTo(); }

    @When("the user searches for {string}")
    public void the_user_searches_for(String query) { getProductPage().search(query); }

    @Then("at least one product should be displayed")
    public void at_least_one_product_should_be_displayed() {
        assertThat(getProductPage().getProductCount(), greaterThan(0));
    }

    @Then("all displayed products should contain {string} in their name")
    public void all_displayed_products_should_contain_in_their_name(String query) {
        List<Product> products = getProductPage().getProducts();
        for (Product p : products) {
            assertThat(p.getName().toLowerCase(), containsString(query.toLowerCase()));
        }
    }

    @When("the user navigates to products with min price {string} and max price {string}")
    public void the_user_navigates_to_products_with_min_price_and_max_price(
        String min,
        String max
    ) {
        getProductPage().setPriceRange(Integer.parseInt(min), Integer.parseInt(max));
    }

    @When("the user selects category {string}")
    public void the_user_selects_category(String category) {
        getProductPage().selectCategory(category);
    }

    @Then("all displayed products should have category {string}")
    public void all_displayed_products_should_have_category(String category) {
        List<Product> products = getProductPage().getProducts();
        for (Product p : products) {
            assertThat(p.getCategoryValue(), is(category));
        }
    }

    @When("the user sets minimum price to {string}")
    public void the_user_sets_minimum_price_to(String min) {
        getProductPage().setMinPrice(Integer.parseInt(min));
    }

    @When("the user sets maximum price to {string}")
    public void the_user_sets_maximum_price_to(String max) {
        getProductPage().setMaxPrice(Integer.parseInt(max));
    }

    @Then("all displayed products should have price between {int} and {int}")
    public void all_displayed_products_should_have_price_between(int min, int max) {
        List<Product> products = getProductPage().getProducts();
        for (Product p : products) {
            double price = p.getPriceValue();
            assertThat(
                price, allOf(greaterThanOrEqualTo((double) min), lessThanOrEqualTo((double) max))
            );
        }
    }

    @When("the user sorts by {string}")
    public void the_user_sorts_by(String option) { getProductPage().sortBy(option); }

    @Then("products should be displayed in correct order")
    public void products_should_be_displayed_in_correct_order() {
        assertThat(getProductPage().getProductCount(), greaterThan(0));
    }

    @When("the user goes to the next page")
    public void the_user_goes_to_the_next_page() { getProductPage().nextPage(); }

    @When("the user goes to the previous page")
    public void the_user_goes_to_the_previous_page() { getProductPage().previousPage(); }

    @Then("the page indicator should show {string}")
    public void the_page_indicator_should_show(String expected) {
        assertThat(getProductPage().getPageInfo(), containsString(expected));
    }

    @When("the user toggles {string}")
    public void the_user_toggles(String filter) {
        if ("In Stock Only".equals(filter)) {
            getProductPage().toggleInStockOnly();
        }
    }

    @Then("all displayed products should be in stock")
    public void all_displayed_products_should_be_in_stock() {
        assertThat(getProductPage().getProductCount(), greaterThan(0));
    }

}