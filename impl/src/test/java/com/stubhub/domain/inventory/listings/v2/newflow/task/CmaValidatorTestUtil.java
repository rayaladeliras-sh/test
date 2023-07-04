package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;

public class CmaValidatorTestUtil {
    public static Object[][] bannedFullValues() {
        String[] badValues = CmaValidator.BAD_FULL_VALUES;
        Object[][] result = new Object[badValues.length][1];

        for (int i = 0; i < badValues.length; i++) {
            result[i][0] = badValues[i];
        }
        return result;
    }

    public static Object[][] bannedPartialValuesForRow() {
        String[] badTerms = CmaValidator.BAD_ROW_TERMS.split(",");
        Object[][] result = new Object[badTerms.length * 4][1];

        for (int i = 0; i < badTerms.length; i++) {
            int baseOffset = i * 4;
            result[baseOffset][0] = badTerms[i];
            result[baseOffset + 1][0] = "prefix" + badTerms[i];
            result[baseOffset + 2][0] = badTerms[i] + "suffix";
            result[baseOffset + 3][0] = "prefix" + badTerms[i] + "suffix";

        }
        return result;
    }

    public static Object[][] bannedPartialValuesForSeat() {
        String[] badTerms = CmaValidator.BAD_SEAT_TERMS.split(",");
        Object[][] result = new Object[badTerms.length * 4][1];

        for (int i = 0; i < badTerms.length; i++) {
            int baseOffset = i * 4;
            result[baseOffset][0] = badTerms[i];
            result[baseOffset + 1][0] = "prefix" + badTerms[i];
            result[baseOffset + 2][0] = badTerms[i] + "suffix";
            result[baseOffset + 3][0] = "prefix" + badTerms[i] + "suffix";

        }
        return result;
    }
}