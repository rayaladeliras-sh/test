package com.stubhub.domain.inventory.listings.v2.validator;

import java.util.Locale;

import org.junit.Assert;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.listings.v2.util.ResourceManager;

public class ListingTextValidatorUtilTest {

	// Only for coverage. tests covered in listing validators
	//SELLAPI-1092 07/10/15 START
	ResourceManager rm = new ResourceManager();
	ListingTextValidatorUtil listingTextValidatorUtil = new ListingTextValidatorUtil();
	
	@BeforeMethod
	public void setUp(){
		rm.resetInstance();
		ReflectionTestUtils.setField(listingTextValidatorUtil, "resourceManager", rm);
	}
	//SELLAPI-1092 07/10/15 END
	@Test
	public void testRemoveSpecialCharactersFromSection() 
	{
		Assert.assertNotNull(listingTextValidatorUtil.removeSpecialCharactersFromSection("section-100", Locale.US));
		Assert.assertNotNull(listingTextValidatorUtil.removeSpecialCharactersFromRowSeat("row20", Locale.US));
		
		Assert.assertNotNull(listingTextValidatorUtil.removeSpecialCharactersFromSection("section-100", Locale.GERMANY));
		Assert.assertNotNull(listingTextValidatorUtil.removeSpecialCharactersFromRowSeat("row20", Locale.GERMANY));
		String germanSection = "ärÄgéjöpÖküYÜß";
		String newSection = listingTextValidatorUtil.removeSpecialCharactersFromSection(germanSection, Locale.GERMANY);
		Assert.assertTrue( newSection.equals( germanSection) ); 
		
		/*String germanRow = "ärÄgéjöpÖküYÜß-100";
		String newRow = listingTextValidatorUtil.removeSpecialCharactersFromRowSeat(germanRow, Locale.GERMANY) ;*/
		//Assert.assertEquals(germanRow, newRow);

		//Assert.assertTrue(listingTextValidatorUtil.startsWithAlphaNumericCharacter("section-100", Locale.GERMANY));
		
		//boolean notStartsWithAlpha = listingTextValidatorUtil.startsWithAlphaNumericCharacter(newSection, Locale.US);
		//Assert.assertTrue ( notStartsWithAlpha );

		//notStartsWithAlpha = listingTextValidatorUtil.startsWithAlphaNumericCharacter(newSection, Locale.GERMANY);
		//Assert.assertTrue ( notStartsWithAlpha );
	}
	
	@Test
	public void testDecimalInSection () 
	{
		String sec = listingTextValidatorUtil.removeSpecialCharactersFromSection(".3 Distance", Locale.US);
		Assert.assertEquals ( sec, ".3 Distance" );
		
		sec = listingTextValidatorUtil.removeSpecialCharactersFromSection("Distance 0.3", Locale.US);
		Assert.assertEquals ( sec, "Distance 0.3" );

		String germanSection = "ärÄgéjöpÖküYÜß 0.3";
		sec = listingTextValidatorUtil.removeSpecialCharactersFromSection(germanSection, Locale.GERMANY);
		Assert.assertEquals ( sec, germanSection );

		// allow start with decimal
		//Assert.assertTrue( listingTextValidatorUtil.startsWithAlphaNumericCharacter("3 Distance", Locale.US) );
		//Assert.assertTrue( listingTextValidatorUtil.startsWithAlphaNumericCharacter("3 ärÄgéjöpÖküYÜß", Locale.GERMANY) );
	}
	
}


