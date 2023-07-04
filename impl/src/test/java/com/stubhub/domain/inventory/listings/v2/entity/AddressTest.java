package com.stubhub.domain.inventory.listings.v2.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.entity.Address;

public class AddressTest {
	@Test
	public void testGetterSetters()
	throws IllegalAccessException, InvocationTargetException,
	NoSuchMethodException {
		Address   bean = new Address("streetAddress1", "streetAddress", "streetAddress3", "city", "state", "US","2222", "1234");
		Map<String, Object> map = cast(PropertyUtils.describe(bean));
		Set<String> fields = map.keySet();
		for (Object o : fields) {
			String key = (String) o;
			Object value = map.get(o);
			String message = key + " = " + value;
			if (!key.equals("class")) {
				PropertyUtils.setSimpleProperty(bean, key, value);
				Object vvalue = PropertyUtils.getSimpleProperty(bean, key);
				message += " (" + vvalue + ")";
			}

		}
	}

	@SuppressWarnings("unchecked")
	private static final < X > X cast( Object o )
	{
		return ( X ) o;
	}
}
