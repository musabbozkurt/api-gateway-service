package com.mb.paymentservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardUtils {

    /**
     * The Luhn algorithm (modulus 10 or "mod 10" algorithm) is used to validate credit card numbers.
     * <p>
     * Algorithm steps:
     * 1. Starting from the last digit and moving backwards, double every second digit.
     * 2. If the doubled value is greater than 9, add its two digits together (or subtract 9).
     * 3. Sum all the digits.
     * 4. If the total ends in zero, the card number is valid.
     * <p>
     * Example test cases:
     * - 4242 4242 4242 6742 → true
     * - 1111 1111 1111 1111 → false
     */
    public static boolean isCardValid(long cardNumber) {
        int sum = 0;
        boolean doubleDigit = false;
        while (cardNumber > 0) {
            int digit = (int) (cardNumber % 10);
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
            cardNumber /= 10;
        }
        return sum % 10 == 0;
    }
}
