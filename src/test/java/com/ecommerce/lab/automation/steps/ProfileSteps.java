package com.ecommerce.lab.automation.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.greaterThan;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ecommerce.lab.automation.pages.ProfilePage;
import com.ecommerce.lab.automation.utils.DriverManager;
import com.ecommerce.lab.automation.utils.SeleniumUtils;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

public class ProfileSteps {

    private WebDriver getDriver() { return DriverManager.getSmartDriver(); }

    private ProfilePage getProfilePage() { return new ProfilePage(getDriver()); }

    @Given("the user is on the profile page")
    public void the_user_is_on_the_profile_page() { getProfilePage().navigateTo(); }

    @Then("the profile page should be displayed")
    public void the_profile_page_should_be_displayed() {
        assertThat(
            "Profile page is not loaded",
            getProfilePage().isPageLoaded(), is(true)
        );
    }

    @Then("the page title should contains {string}")
    public void the_page_title_should_contains(String expectedTitle) {
        String actualTitle = getProfilePage().getPageTitle();
        assertThat("Page title mismatch", actualTitle, containsString(expectedTitle));
    }

    @Then("a success message should be displayed")
    public void a_success_message_should_be_displayed() {
        boolean hasSuccessToast = SeleniumUtils.isToastDisplayed(getDriver(), "success", "");
        // Or check for any success toast
        By successToast = By.cssSelector(".bg-green-600.fixed");
        WebElement toast = SeleniumUtils
            .waitForElement(getDriver(), successToast, SeleniumUtils.DEFAULT_TIMEOUT);
        assertThat("Success message not displayed", toast.isDisplayed(), is(true));
    }

    @Then("the profile display name should be {string}")
    public void the_profile_display_name_should_be(String expectedName) {
        String actualName = getProfilePage().getProfileDisplayName();
        assertThat("Display name mismatch", actualName, is(expectedName));
    }

    @Then("the profile email should be {string}")
    public void the_profile_email_should_be(String expectedEmail) {
        String actualEmail = getProfilePage().getProfileEmail();
        assertThat("Email mismatch", actualEmail, is(expectedEmail));
    }

    @Then("the profile initials should be {string}")
    public void the_profile_initials_should_be(String expectedInitials) {
        String actualInitials = getProfilePage().getProfileInitials();
        assertThat("Initials mismatch", actualInitials, is(expectedInitials));
    }

    @Then("the profile display name should not be empty")
    public void the_profile_display_name_should_not_be_empty() {
        String actualName = getProfilePage().getProfileDisplayName();
        assertThat("Display name is empty", actualName, not(is("")));
    }

    @Then("the profile email should not be empty")
    public void the_profile_email_should_not_be_empty() {
        String actualEmail = getProfilePage().getProfileEmail();
        assertThat("Email is empty", actualEmail, not(is("")));
    }

    @When("the user clicks on the {string} tab")
    public void the_user_clicks_on_the_tab(String tabName) { getProfilePage().clickTab(tabName); }

    @When("the user clicks on Personal Info tab")
    public void the_user_clicks_on_personal_info_tab() { getProfilePage().clickPersonalInfoTab(); }

    @When("the user clicks on Shipping Address tab")
    public void the_user_clicks_on_shipping_address_tab() {
        getProfilePage().clickShippingAddressTab();
    }

    @When("the user clicks on Security tab")
    public void the_user_clicks_on_security_tab() { getProfilePage().clickSecurityTab(); }

    @When("the user clicks on 2FA Settings tab")
    public void the_user_clicks_on_2fa_settings_tab() { getProfilePage().click2FATab(); }

    @When("the user clicks on Store Balance tab")
    public void the_user_clicks_on_store_balance_tab() { getProfilePage().clickStoreBalanceTab(); }

    @Then("the Personal Info tab should be visible")
    public void the_personal_info_tab_should_be_visible() {
        assertThat(
            "Personal Info tab not visible",
            getProfilePage().isPersonalInfoTabVisible(), is(true)
        );
    }

    @Then("the {string} tab should be visible")
    public void the_tab_should_be_visible(String tabName) {
        boolean visible = switch (tabName.toLowerCase()) {
        case "info" -> getProfilePage().isPersonalInfoTabVisible();
        case "address" -> getProfilePage().isShippingAddressTabVisible();
        case "security" -> getProfilePage().isSecurityTabVisible();
        case "2fa" -> getProfilePage().is2FATabVisible();
        case "redeem" -> getProfilePage().isStoreBalanceTabVisible();
        default -> false;
        };
        assertThat(tabName + " tab should be visible", visible, is(true));
    }

    @Then("the store balance should be displayed")
    public void the_store_balance_should_be_displayed() {
        String balance = getProfilePage().getStoreBalance();
        assertThat("Store balance not displayed", balance, not(is("")));
    }

    @Then("the redeem form should be visible")
    public void the_redeem_form_should_be_visible() {
        assertThat("Redeem form not visible", getProfilePage().isRedeemFormDisplayed(), is(true));
    }

    @Then("the Shipping Address tab should be visible")
    public void the_shipping_address_tab_should_be_visible() {
        assertThat(
            "Shipping Address tab not visible",
            getProfilePage().isShippingAddressTabVisible(), is(true)
        );
    }

    @Then("the Security tab should be visible")
    public void the_security_tab_should_be_visible() {
        assertThat(
            "Security tab not visible",
            getProfilePage().isSecurityTabVisible(), is(true)
        );
    }

    @Then("the 2FA Settings tab should be visible")
    public void the_2fa_settings_tab_should_be_visible() {
        assertThat(
            "2FA Settings tab not visible",
            getProfilePage().is2FATabVisible(), is(true)
        );
    }

    @Then("the Store Balance tab should be visible")
    public void the_store_balance_tab_should_be_visible() {
        assertThat(
            "Store Balance tab not visible",
            getProfilePage().isStoreBalanceTabVisible(), is(true)
        );
    }

    @Then("the active tab should be {string}")
    public void the_active_tab_should_be(String expectedTab) {
        String activeTab = getProfilePage().getActiveTabName();
        assertThat("Active tab mismatch", activeTab, is(expectedTab));
    }

    @When("the user updates display name to {string}")
    public void the_user_updates_display_name_to(String newDisplayName) {
        getProfilePage().updateDisplayName(newDisplayName);
    }

    @When("the user enters display name {string}")
    public void the_user_enters_display_name(String displayName) {
        getProfilePage().enterDisplayName(displayName);
    }

    @And("the user submits the personal info form")
    public void the_user_submits_the_personal_info_form() { getProfilePage().submitPersonalInfo(); }

    @Then("the email field should be readonly")
    public void the_email_field_should_be_readonly() {
        assertThat(
            "Email field is not readonly",
            getProfilePage().isEmailInputReadonly(), is(true)
        );
    }

    @Then("the display name field should contain {string}")
    public void the_display_name_field_should_contain(String expectedValue) {
        String actualValue = getProfilePage().getDisplayNameValue();
        assertThat("Display name value mismatch", actualValue, is(expectedValue));
    }

    @When("the user fills address with {string}, {string}, {string}, {string}, {string}")
    public void the_user_fills_address_with(
        String street,
        String city,
        String state,
        String zip,
        String country
    ) {
        getProfilePage().fillAddress(street, city, state, zip, country);
    }

    @And("the user submits the address form")
    public void the_user_submits_the_address_form() { getProfilePage().submitAddress(); }

    @Then("the street field should contain {string}")
    public void the_street_field_should_contain(String expectedValue) {
        assertThat(
            "Street value mismatch",
            getProfilePage().getStreetValue(), is(expectedValue)
        );
    }

    @Then("the city field should contain {string}")
    public void the_city_field_should_contain(String expectedValue) {
        assertThat(
            "City value mismatch",
            getProfilePage().getCityValue(), is(expectedValue)
        );
    }

    @Then("the state field should contain {string}")
    public void the_state_field_should_contain(String expectedValue) {
        assertThat(
            "State value mismatch",
            getProfilePage().getStateValue(), is(expectedValue)
        );
    }

    @Then("the zip field should contain {string}")
    public void the_zip_field_should_contain(String expectedValue) {
        assertThat(
            "ZIP value mismatch",
            getProfilePage().getZipValue(), is(expectedValue)
        );
    }

    @Then("the country field should contain {string}")
    public void the_country_field_should_contain(String expectedValue) {
        assertThat(
            "Country value mismatch",
            getProfilePage().getCountryValue(), is(expectedValue)
        );
    }

    @When("the user changes password from {string} to {string}")
    public void the_user_changes_password_from_to(String currentPassword, String newPassword) {
        getProfilePage().changePassword(currentPassword, newPassword);
    }

    @When("the user enters current password {string}")
    public void the_user_enters_current_password(String password) {
        getProfilePage().enterCurrentPassword(password);
    }

    @When("the user enters new password {string}")
    public void the_user_enters_new_password(String password) {
        getProfilePage().enterNewPassword(password);
    }

    @When("the user confirms password {string}")
    public void the_user_confirms_password(String password) {
        getProfilePage().enterConfirmPassword(password);
    }

    @And("the user submits the password change")
    public void the_user_submits_the_password_change() { getProfilePage().submitPasswordChange(); }

    @Then("the password fields should be of type password")
    public void the_password_fields_should_be_of_type_password() {
        assertThat(
            "Current password field type mismatch",
            getProfilePage().getCurrentPasswordFieldType(), is("password")
        );
        assertThat(
            "New password field type mismatch",
            getProfilePage().getNewPasswordFieldType(), is("password")
        );
    }

    @When("the user toggles 2FA")
    public void the_user_toggles_2fa() { getProfilePage().toggle2FA(); }

    @When("the user enables 2FA")
    public void the_user_enables_2fa() { getProfilePage().enable2FA(); }

    @When("the user disables 2FA")
    public void the_user_disables_2fa() { getProfilePage().disable2FA(); }

    @Then("2FA should be enabled")
    public void two_fa_should_be_enabled() {
        assertThat(
            "2FA is not enabled",
            getProfilePage().is2FAEnabled(), is(true)
        );
    }

    @Then("2FA should be disabled")
    public void two_fa_should_be_disabled() {
        assertThat(
            "2FA is not disabled",
            getProfilePage().is2FAEnabled(), is(false)
        );
    }

    @Then("the 2FA status should show {string}")
    public void the_2fa_status_should_show(String expectedStatus) {
        String actualStatus = getProfilePage().get2FAStatusText();
        assertThat(
            "2FA status mismatch",
            actualStatus, containsString(expectedStatus)
        );
    }

    @Then("the store balance should be {string}")
    public void the_store_balance_should_be(String expectedBalance) {
        String actualBalance = getProfilePage().getStoreBalance();
        assertThat("Store balance mismatch", actualBalance, is(expectedBalance));
    }

    @When("the user redeems gift card code {string}")
    public void the_user_redeems_gift_card_code(String code) {
        getProfilePage().redeemGiftCard(code);
    }

    @When("the user enters gift card code {string}")
    public void the_user_enters_gift_card_code(String code) {
        getProfilePage().enterGiftCardCode(code);
    }

    @And("the user clicks the redeem button")
    public void the_user_clicks_the_redeem_button() { getProfilePage().clickRedeem(); }

    @Then("the gift card code field should contain {string}")
    public void the_gift_card_code_field_should_contain(String expectedCode) {
        String actualCode = getProfilePage().getGiftCardCodeValue();
        assertThat(actualCode, is(expectedCode));
    }

    @Then("the redeem button should be enabled")
    public void the_redeem_button_should_be_enabled() {
        assertThat(
            "Redeem button is not enabled",
            getProfilePage().isRedeemButtonEnabled(), is(true)
        );
    }

    @Then("the redeem history should show activity")
    public void the_redeem_history_should_show_activity() {
        assertThat(
            "Redeem history has no activity",
            getProfilePage().hasRedeemHistory(), is(true)
        );
    }

    @Then("the redeem history should show {string}")
    public void the_redeem_history_should_show(String expectedText) {
        String historyText = getProfilePage().getRedeemHistoryText();
        assertThat(
            "Redeem history text mismatch",
            historyText, containsString(expectedText)
        );
    }

    @Then("all profile tabs should be present")
    public void all_profile_tabs_should_be_present() {
        assertThat(
            "Not all tabs are present",
            getProfilePage().areAllTabsPresent(), is(true)
        );
        assertThat(
            "Tab count should be 5",
            getProfilePage().getProfileTabCount(), is(5)
        );
    }

    @Then("the last login time should be displayed")
    public void the_last_login_time_should_be_displayed() {
        String lastLogin = getProfilePage().getLastLoginTime();
        assertThat(
            "Last login time is empty",
            lastLogin, not(is(""))
        );
    }
}