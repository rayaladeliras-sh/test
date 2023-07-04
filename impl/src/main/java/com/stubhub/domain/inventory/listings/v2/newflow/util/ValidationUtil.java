package com.stubhub.domain.inventory.listings.v2.newflow.util;

import com.stubhub.newplatform.common.entity.Money;

public class ValidationUtil {

	public static boolean isValid(Money money) {
		boolean valid = true;
		if (money != null) {
			valid = money.getAmount() != null && money.getAmount().doubleValue() > 0;
		}

		return valid;
	}
}