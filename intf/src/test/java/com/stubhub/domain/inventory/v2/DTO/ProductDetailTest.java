package com.stubhub.domain.inventory.v2.DTO;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.v2.enums.Operation;

public class ProductDetailTest {
	@Test
	public void testGetterSetters()
	throws IllegalAccessException, InvocationTargetException,
	NoSuchMethodException {
		ProductDetail bean = new ProductDetail();
		bean.setGa(true);
		bean.setMedium("TICKET");
		bean.setProductId(1L);
		bean.setProductType("TICKET");
		bean.setRow("R1");
		bean.setSeat("S1");
		bean.setSection("Upper");
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
	
	@Test
	public void testGetterSettersTicketTrait()
	throws IllegalAccessException, InvocationTargetException,
	NoSuchMethodException {
		TicketTrait bean = new TicketTrait();
		bean.setId("1");
		bean.setName("tickettrait");
		bean.setOperation(Operation.ADD);
		bean.setType("type");
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
