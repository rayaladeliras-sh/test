package com.stubhub.domain.inventory.listings.v2.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
//SELLAPI-1092 7/15/15 made as Spring component
@Component("listingTextValidatorUtil")
public class ListingTextValidatorUtil {
	
	//SELLAPI-1092 7/10/15 - ResourceManager is converted as spring bean and autowired in this class
	@Autowired
	private ResourceManager resourceManager;

	/**
	 * This method is used to remove special characters from section
	 * 
	 * It takes in a string input and returns a string which is free of special characters. 
	 * Allowed characters in the output string are a-zA-Z0-9\\s\\-\\_ 
	 * @param input
	 * @return
	 */
	//SELLAPI-1092 7/08/15 removed static
	public String removeSpecialCharactersFromSection(final String input, Locale locale) {	
		if(input == null) {  
			return null;  
		}
		String pattern = resourceManager.getResource (
				ResourceManager.regex_section_specialChars, locale);
		return input.replaceAll( pattern, "");
	}
	
	/**
	 * This method is used to remove special characters from rows and seats. 
	 * Comma is allowed in rows and seats.	 * 
	 * Allowed characters in the output string are a-zA-Z0-9\\s\\-\\_\\, 
	 * 
	 * Row/Seat might have comma separated tokens. Some tokens might become empty strings because of filtering out.
	 * For example: "1, $, 2" becomes "1, ,2". 
	 * @param input
	 * @return
	 */
	//SELLAPI-1092 7/08/15 removed static
	public String removeSpecialCharactersFromRowSeat(final String input, Locale locale) {
		if(input == null) {
			return null;
		}
		String pattern = resourceManager.getResource (
				ResourceManager.regex_rowseat_specialChars, locale);
        try{
            return input.replaceAll( pattern, "");
        }
        catch(Exception e){
            // Parsing failed, chars not replaced
            return input;
        }
	}
	
	/**
	 * Takes in a string as input and returns true if the string starts with a valid alpha numeric character (a-zA-Z0-9)
	 * Otherwise returns a false;
	 * @param input
	 * @return
	 */
	//SELLAPI-1092 7/08/15 removed static
	/*public boolean startsWithAlphaNumericCharacter(final String input, Locale locale) {
		if(input == null) {
			return false;
		}
		String pattern = resourceManager.getResource (
				ResourceManager.regex_section_startsWith, locale);
		return !input.trim().matches( pattern );
	}*/
	
	/**
	 * strip all HTML characters
	 * @param input
	 * @return
	 */
	public static String stripSpecialCharactersForId ( String input )
	{	
		return input.replaceAll("[^a-zA-Z0-9\\s\\_\\-\\,\\/]", "");
	}

}
