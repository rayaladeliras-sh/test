package com.stubhub.domain.inventory.listings.v2.entity;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.util.ClassFinder;
import com.stubhub.domain.inventory.listings.v2.util.GetterSetterTester;

public class EntitySetterGetterTest {
	
	private GetterSetterTester tester;
	private ClassFinder classFinder;
	
	@BeforeMethod
	public void setup() {
		tester = new GetterSetterTester();
		classFinder = new ClassFinder();
	}
	
	/**
	 * find all classes in the entity package and call the setters/getters
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void testGettersAndSetters() throws IOException, ClassNotFoundException {
		List<Class> clazzes = classFinder.findClassesInPackage("com.stubhub.domain.inventory.listings.v2.entity");
		for (Class clazz: clazzes) {
			tester.testClass(clazz);
		}
	}
}
