# /src/test/resources/features/login/Login.feature
# Owned by: @auth-team
# CODEOWNERS: /src/test/resources/features/login/ @auth-team

@login @smoke
Feature: Amazon Login


  @p0 @smokei
  Scenario Outline: Successful login with valid credentials
   Given I am on the Amazon homepage
    
  @p2
  Scenario: Login with invalid credentials shows error
    When I enter email "invalid@test.com"
    And I click Continue
    And I enter password "wrongpassword"
    And I click Sign In
    Then I should see an error message containing "incorrect"

  @p2
  Scenario: Login with empty email shows validation
    When I enter email ""
    And I click Continue
    Then I should see a validation error

  @p3 @data-driven
  Scenario Outline: Login attempt with multiple invalid users
    When I login with email "<email>" and password "<password>"
    Then I should see an error message containing "<expectedError>"

    Examples: from Excel testdata/login.xlsx sheet InvalidLogins
      | email          | password   | expectedError |
      | bad@email.com  | pass123    | incorrect     |
      | no@exist.com   | test456    | incorrect     |
