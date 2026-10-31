package com.mb.modulith.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailUtilsTest {

    private static Stream<String> validEmailAddresses() {
        return Stream.of(
                "user.name+tag@example.com",
                "name@mb.se",
                "simple@domain.org",
                "a@bc.de",
                "user_name@sub.example.com",
                "user&alias*test-1@mail-host.example.net",
                "local@host.example.co.uk"
        );
    }

    private static Stream<String> invalidEmailAddresses() {
        return Stream.of(
                "not-an-email",
                "@missing-local.com",
                "missing-domain@",
                "missing-tld@domain",
                "user@.com",
                "user@domain.c",
                "user@domain.123",
                "user name@example.com",
                "user@exam ple.com",
                "user@@example.com",
                ".user@example.com",
                "user.@example.com"
        );
    }

    private static Stream<InvalidEmailRuleCase> invalidEmailValidationRules() {
        return Stream.of(
                InvalidEmailRuleCase.emptyLocalPart("@example.com"),
                InvalidEmailRuleCase.emptyDomainPart("local@"),
                InvalidEmailRuleCase.multipleAtSigns("user@domain@example.com"),
                InvalidEmailRuleCase.consecutiveDotsInLocalPart("user..name@example.com"),
                InvalidEmailRuleCase.consecutiveDotsInDomain("user@example..com"),
                InvalidEmailRuleCase.tldTooShort("user@example.c"),
                InvalidEmailRuleCase.tldTooLong("user@example.verylongt"),
                InvalidEmailRuleCase.emptyTld("user@example."),
                InvalidEmailRuleCase.emptyTld("user@host.com."),
                InvalidEmailRuleCase.nonAlphabeticTld("user@domain.co1"),
                InvalidEmailRuleCase.nonAlphabeticTld("user@example.12")
        );
    }

    @Test
    void getEmailDomain_ShouldReturnDomain_WhenEmailContainsAtSign() {
        // Act
        String domain = EmailUtils.getEmailDomain("user@mb.com");

        // Assertions
        assertEquals("mb.com", domain);
    }

    @Test
    void getEmailDomain_ShouldReturnSubdomainHost_WhenEmailHasMultipleDomainLabels() {
        // Act
        String domain = EmailUtils.getEmailDomain("user@mail.example.co.uk");

        // Assertions
        assertEquals("mail.example.co.uk", domain);
    }

    @Test
    void getEmailDomain_ShouldReturnPartAfterFirstAtSign_WhenLocalPartContainsSpecialChars() {
        // Act
        String domain = EmailUtils.getEmailDomain("user.name+tag@example.com");

        // Assertions
        assertEquals("example.com", domain);
    }

    @ParameterizedTest
    @MethodSource("validEmailAddresses")
    void isValid_ShouldReturnTrue_WhenEmailIsWellFormed(String emailAddress) {
        // Act
        boolean valid = EmailUtils.isValid(emailAddress);

        // Assertions
        assertTrue(valid);
    }

    @ParameterizedTest
    @MethodSource("invalidEmailAddresses")
    void isValid_ShouldReturnFalse_WhenEmailIsInvalid(String emailAddress) {
        // Act
        boolean valid = EmailUtils.isValid(emailAddress);

        // Assertions
        assertFalse(valid);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void isValid_ShouldReturnFalse_WhenValueIsNullOrBlank(String emailAddress) {
        // Assertions
        assertTrue(emailAddress == null || emailAddress.isBlank());
        assertFalse(EmailUtils.isValid(emailAddress));
    }

    @ParameterizedTest
    @MethodSource("invalidEmailValidationRules")
    void isValid_ShouldReturnFalse_WhenEmailBreaksValidationRule(InvalidEmailRuleCase testCase) {
        // Arrange
        testCase.assertStructuralViolation();

        // Assertions
        assertFalse(EmailUtils.isValid(testCase.email()), testCase.rule());
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTldHasMinimumLength() {
        // Act
        boolean valid = EmailUtils.isValid("user@example.co");

        // Assertions
        assertTrue(valid);
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTldHasMaximumLength() {
        // Act
        boolean valid = EmailUtils.isValid("user@example.company");

        // Assertions
        assertTrue(valid);
    }

    private record InvalidEmailRuleCase(String email, String rule, Runnable structuralCheck) {

        static InvalidEmailRuleCase emptyLocalPart(String email) {
            return new InvalidEmailRuleCase(email, "empty local part", () ->
                    assertEquals(0, email.indexOf('@'), "local part should be empty"));
        }

        static InvalidEmailRuleCase emptyDomainPart(String email) {
            return new InvalidEmailRuleCase(email, "empty domain part", () ->
                    assertTrue(email.endsWith("@"), "domain part should be empty"));
        }

        static InvalidEmailRuleCase multipleAtSigns(String email) {
            return new InvalidEmailRuleCase(email, "multiple @ signs", () -> {
                int first = email.indexOf('@');
                assertTrue(first >= 0);
                assertTrue(email.indexOf('@', first + 1) >= 0, "expected a second @");
            });
        }

        static InvalidEmailRuleCase consecutiveDotsInLocalPart(String email) {
            return new InvalidEmailRuleCase(email, "consecutive dots in local part", () ->
                    assertTrue(email.substring(0, email.indexOf('@')).contains("..")));
        }

        static InvalidEmailRuleCase consecutiveDotsInDomain(String email) {
            return new InvalidEmailRuleCase(email, "consecutive dots in domain", () ->
                    assertTrue(email.substring(email.indexOf('@') + 1).contains("..")));
        }

        static InvalidEmailRuleCase tldTooShort(String email) {
            return new InvalidEmailRuleCase(email, "TLD too short", () -> {
                String tld = email.substring(email.lastIndexOf('.') + 1);
                assertTrue(tld.length() < 2, "TLD should be shorter than 2 characters");
            });
        }

        static InvalidEmailRuleCase tldTooLong(String email) {
            return new InvalidEmailRuleCase(email, "TLD too long", () -> {
                String tld = email.substring(email.lastIndexOf('.') + 1);
                assertTrue(tld.length() > 7, "TLD should be longer than 7 characters");
            });
        }

        static InvalidEmailRuleCase emptyTld(String email) {
            return new InvalidEmailRuleCase(email, "empty TLD label", () -> {
                String domainPart = email.substring(email.indexOf('@') + 1);
                String tld = domainPart.substring(domainPart.lastIndexOf('.') + 1);
                assertTrue(tld.isEmpty(), "TLD label should be empty");
            });
        }

        static InvalidEmailRuleCase nonAlphabeticTld(String email) {
            return new InvalidEmailRuleCase(email, "TLD not alphabetic or wrong length", () -> {
                String tld = email.substring(email.lastIndexOf('.') + 1);
                assertFalse(tld.isEmpty(), "TLD should be non-empty for this case");
                assertFalse(tld.matches("[a-zA-Z]{2,7}"), "TLD should not match DOMAIN_TLD pattern");
            });
        }

        void assertStructuralViolation() {
            structuralCheck.run();
        }
    }
}
