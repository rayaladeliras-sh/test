package com.stubhub.domain.inventory.listings.v2.validator;

import java.util.ArrayList;
import java.util.List;

import com.stubhub.domain.inventory.common.util.ListingError;

public class InputValidator {
	private List<Validator> validators;
	private List<ListingError> errors = new ArrayList<ListingError>();
	
	public void addValidator(Validator validator){
		if(validators == null){
			validators = new ArrayList<Validator>();
		}
		validators.add(validator);
	}
	
	public void validate(){
		for(Validator validator:validators){
			validator.validate();
			errors.addAll(validator.getErrors());
		}
	}
	
	public List<ListingError> getErrors(){
		return errors;
	}
	
	
}
