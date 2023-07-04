package com.stubhub.domain.inventory.listings.v2.impl;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.util.ClassFinder;
import com.stubhub.domain.inventory.listings.v2.util.GetterSetterTester;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.inventory.v2.DTO.Fee;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.ListingResponse;

public class DTOTest {
	@SuppressWarnings("rawtypes")
	private Class[] testClasses = {BulkJobResponse.class, BulkListingInternal.class,
		ListingResponse.class, BulkListingRequest.class,
		BulkListingResponse.class, EventInfo.class, Fee.class};
	
	private GetterSetterTester tester;
	private ClassFinder classFinder;
	
	@BeforeMethod
	public void setup()
	{
		tester = new GetterSetterTester();
		classFinder = new ClassFinder();
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testDtoClasses()
	{
		for (Class clazz : testClasses){
			tester.testClass(clazz);
		}
	}
	
	/**
	 * find all classes in the entity package and call the setters/getters
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void testGettersAndSetters() throws IOException, ClassNotFoundException
	{
		List<Class> clazzes = classFinder.findClassesInPackage("com.stubhub.domain.inventory.v2.DTO");
		for(Class clazz: clazzes){
			if(!clazz.getName().contains("ListingControllerResponse")){
				tester.testClass(clazz);
			}
		}
	}
}