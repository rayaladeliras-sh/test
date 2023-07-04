package com.stubhub.domain.inventory.v2.DTO;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.testng.annotations.Test;

public class EventInfoTest {
	
	@Test
	public void testGetterSettersTicketTrait()
	throws IllegalAccessException, InvocationTargetException,
	NoSuchMethodException {
		EventInfo bean = new EventInfo();
		bean.setCity("San Francisco");
		bean.setDate("");
		bean.setEventId(1L);
		bean.setName("");
		bean.setVenue("");
		bean.setZipCode("");
		bean.hashCode();
		bean.equals(new EventInfo());
		bean.toString();
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
