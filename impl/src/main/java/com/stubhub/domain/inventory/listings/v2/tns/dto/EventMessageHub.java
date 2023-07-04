package com.stubhub.domain.inventory.listings.v2.tns.dto;

import java.util.ArrayList;
import java.util.List;

public class EventMessageHub {

	private String id;
	private String description;
	private String dateLocal;
	private boolean timeSubjectToChange;
	private String venue;
	private String city;
	private String state;
	private String countryCode;
	private String performerId;
	private String performerName;
	private String secondaryPerformerId;
	private String secondaryPerformerName;
	private List<String> categoriesIds = new ArrayList<String>();
	private List<String> groupingsIds = new ArrayList<String>();
	private List<String> performersIds = new ArrayList<String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDateLocal() {
		return dateLocal;
	}

	public void setDateLocal(String dateLocal) {
		this.dateLocal = dateLocal;
	}

	public boolean getTimeSubjectToChange() {
		return timeSubjectToChange;
	}

	public void setTimeSubjectToChange(boolean timeSubjectToChange) {
		this.timeSubjectToChange = timeSubjectToChange;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getPerformerId() {
		return performerId;
	}

	public void setPerformerId(String performerId) {
		this.performerId = performerId;
	}

	public String getPerformerName() {
		return performerName;
	}

	public void setPerformerName(String performerName) {
		this.performerName = performerName;
	}

	public String getSecondaryPerformerId() {
		return secondaryPerformerId;
	}

	public void setSecondaryPerformerId(String secondaryPerformerId) {
		this.secondaryPerformerId = secondaryPerformerId;
	}

	public String getSecondaryPerformerName() {
		return secondaryPerformerName;
	}

	public void setSecondaryPerformerName(String secondaryPerformerName) {
		this.secondaryPerformerName = secondaryPerformerName;
	}

	public List<String> getCategoriesIds() {
		return new ArrayList<>(categoriesIds);
	}

	public void setCategoriesIds(List<String> categoriesIds) {
		this.categoriesIds.addAll(categoriesIds);
	}

	public List<String> getGroupingsIds() {
		return new ArrayList<>(groupingsIds);
	}

	public void setGroupingsIds(List<String> groupingsIds) {
		this.groupingsIds.addAll(groupingsIds);
	}

	public List<String> getPerformersIds() {
		return new ArrayList<>(performersIds);
	}

	public void setPerformersIds(List<String> performersIds) {
		this.performersIds.addAll(performersIds);
	}

}
