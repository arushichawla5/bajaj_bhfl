package com.chitkara.bfhl.service;

import com.chitkara.bfhl.dto.BfhlRequest;
import com.chitkara.bfhl.dto.BfhlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link BfhlService}.
 *
 * Business rules:
 * <ul>
 *   <li>Each token in "data" is classified as a number-string, alphabet-string, or special-char string.</li>
 *   <li>A token is numeric  if ALL characters in it are digits (e.g. "334").</li>
 *   <li>A token is alphabetic if ALL characters in it are letters (e.g. "ABCD").</li>
 *   <li>Everything else is a special character token.</li>
 *   <li>Numbers are split into odd / even based on the numeric value of the token.</li>
 *   <li>Alphabetic tokens are returned upper-cased.</li>
 *   <li>Sum is the arithmetic sum of all numeric tokens, returned as a string.</li>
 *   <li>concat_string: collect all individual alphabetic characters from the input
 *       (expanding multi-char tokens), reverse the list, then apply alternating caps
 *       starting with UPPER for index 0 of the reversed list.</li>
 * </ul>
 */
@Service
public class BfhlServiceImpl implements BfhlService {

    // ── user identity (externalised so they are easy to change) ──────────────
    @Value("${app.user.full-name:john_doe}")
    private String fullName;

    @Value("${app.user.dob:17091999}")
    private String dob;

    @Value("${app.user.email:john@xyz.com}")
    private String email;

    @Value("${app.user.roll-number:ABCD123}")
    private String rollNumber;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public BfhlResponse process(BfhlRequest request) {

        List<String> data = request.getData();

        List<String> oddNumbers       = new ArrayList<>();
        List<String> evenNumbers      = new ArrayList<>();
        List<String> alphabets        = new ArrayList<>();
        List<String> specialChars     = new ArrayList<>();
        List<Character> allAlphaChars = new ArrayList<>();   // for concat_string
        long numericSum               = 0;

        for (String token : data) {

            if (isNumeric(token)) {
                // ── numeric token ───────────────────────────────────────────
                long value = Long.parseLong(token);
                numericSum += value;

                if (value % 2 == 0) {
                    evenNumbers.add(token);
                } else {
                    oddNumbers.add(token);
                }

            } else if (isAlphabetic(token)) {
                // ── alphabetic token ────────────────────────────────────────
                alphabets.add(token.toUpperCase());

                // collect individual characters for concat_string
                for (char c : token.toCharArray()) {
                    allAlphaChars.add(c);
                }

            } else {
                // ── special character token ─────────────────────────────────
                specialChars.add(token);
            }
        }

        return BfhlResponse.builder()
                .isSuccess(true)
                .userId(buildUserId())
                .email(email)
                .rollNumber(rollNumber)
                .oddNumbers(oddNumbers)
                .evenNumbers(evenNumbers)
                .alphabets(alphabets)
                .specialCharacters(specialChars)
                .sum(String.valueOf(numericSum))
                .concatString(buildConcatString(allAlphaChars))
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns true if every character in the token is a digit.
     * Handles multi-digit strings like "334".
     */
    private boolean isNumeric(String token) {
        if (token == null || token.isEmpty()) return false;
        for (char c : token.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /**
     * Returns true if every character in the token is a letter.
     * Handles multi-char strings like "ABCD".
     */
    private boolean isAlphabetic(String token) {
        if (token == null || token.isEmpty()) return false;
        for (char c : token.toCharArray()) {
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }

    /**
     * Builds the user_id string: {full_name_ddmmyyyy}, all lowercase.
     * E.g. "john_doe_17091999"
     */
    private String buildUserId() {
        return (fullName.toLowerCase() + "_" + dob).toLowerCase();
    }

    /**
     * Builds the concat_string:
     * 1. Take all individual alphabetic characters collected during processing
     *    (in the order they appeared in the input).
     * 2. Reverse the list.
     * 3. Apply alternating caps: index 0 → UPPER, index 1 → lower, index 2 → UPPER, …
     *
     * Example from spec:
     *   Input alphabets in order: a, R  → reversed: R, a → alternating: R (upper), a (lower) → "Ra"
     *   Input alphabets in order: a, y, b → reversed: b, y, a → "ByA"
     */
    private String buildConcatString(List<Character> chars) {
        if (chars.isEmpty()) return "";

        // reverse
        List<Character> reversed = new ArrayList<>(chars);
        java.util.Collections.reverse(reversed);

        // alternating caps
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reversed.size(); i++) {
            char c = reversed.get(i);
            if (i % 2 == 0) {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
