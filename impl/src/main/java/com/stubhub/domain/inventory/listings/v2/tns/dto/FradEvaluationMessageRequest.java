package com.stubhub.domain.inventory.listings.v2.tns.dto;

public class FradEvaluationMessageRequest {
	
	private String action;
	private Value value;
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}

}
