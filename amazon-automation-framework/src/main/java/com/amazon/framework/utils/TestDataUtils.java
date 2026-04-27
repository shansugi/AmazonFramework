package com.amazon.framework.utils;

import com.github.javafaker.Faker;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

/**
 * TestDataUtils — dynamic test data generation using Java Faker.
 *
 * Use when you need UNIQUE data per test run (e.g. new user registration,
 * addresses that shouldn't conflict with previous test runs).
 * Use ExcelUtils when you need SPECIFIC, controlled data sets.
 *
 * Usage:
 *   String email = TestDataUtils.generateEmail();
 *   Map<String,String> addr = TestDataUtils.generateIndianAddress();
 */
public class TestDataUtils {

    private static final Faker faker = new Faker(new Locale("en-IND"));
    private static final Faker fakerUS = new Faker(new Locale("en-US"));

    private TestDataUtils() {}

    // ─────────────────────────────────────────────────────────────────────────
    // USER DATA
    // ─────────────────────────────────────────────────────────────────────────
    public static String generateEmail() {
        return "test_" + System.currentTimeMillis() + "@qatest.com";
    }

    public static String generateName() {
        return faker.name().fullName();
    }

    public static String generatePhone() {
        // Indian mobile: starts with 6-9, 10 digits
        return "9" + faker.number().digits(9);
    }

    public static String generatePassword() {
        return "Test@" + faker.number().digits(6);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADDRESS DATA
    // ─────────────────────────────────────────────────────────────────────────
    public static Map<String, String> generateIndianAddress() {
        Map<String, String> address = new HashMap<>();
        address.put("name",    faker.name().fullName());
        address.put("phone",   generatePhone());
        address.put("line1",   faker.address().streetAddress());
        address.put("city",    randomFrom("Bangalore", "Mumbai", "Delhi", "Chennai", "Hyderabad"));
        address.put("state",   "Karnataka");
        address.put("pincode", "56" + faker.number().digits(4));
        return address;
    }

    /** Predefined address for stable tests (use when exactness matters). */
    public static Map<String, String> getFixedTestAddress() {
        Map<String, String> address = new HashMap<>();
        address.put("name",    "QA Test User");
        address.put("phone",   "9876543210");
        address.put("line1",   "123 Test Street, Brigade Road");
        address.put("city",    "Bangalore");
        address.put("state",   "Karnataka");
        address.put("pincode", "560001");
        return address;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD DATA (test card numbers — safe for test environments only)
    // ─────────────────────────────────────────────────────────────────────────
    public static Map<String, String> getTestCardDetails() {
        Map<String, String> card = new HashMap<>();
        card.put("number", "4111 1111 1111 1111"); // Visa test card
        card.put("name",   faker.name().fullName());
        card.put("month",  "12");
        card.put("year",   "2028");
        card.put("cvv",    "123");
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRODUCT SEARCH TERMS
    // ─────────────────────────────────────────────────────────────────────────
    public static String randomProduct() {
        return randomFrom(
            "wireless earbuds", "USB-C hub", "laptop stand",
            "phone case", "keyboard", "mouse", "webcam",
            "desk lamp", "notebook", "pen drive"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────────────────────────────────
    @SafeVarargs
    private static <T> T randomFrom(T... items) {
        return items[(int) (Math.random() * items.length)];
    }
}
