package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.TimeZone;

public class EventInfo {
	
	private Long eventId;
	
	private String name;
	
	private String status;
	
	private Calendar eventDate;
	
	private TimeZone timeZone;
	
	private Currency currency;
	
	private Long venueConfigId;
	
	private String venueDesc;
	
	private String country;
	
	private boolean isIntegrated;
	
	private boolean isParkingOnlyEvent;
	
	private boolean gaIndicator;
	
	private boolean sectionScrubbing;
	
	private boolean rowScrubbing;
	
	private List<TicketTrait> ticketTraits;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Calendar getEventDate() {
		return eventDate;
	}

	public void setEventDate(Calendar eventDate) {
		this.eventDate = eventDate;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Long getVenueConfigId() {
		return venueConfigId;
	}

	public void setVenueConfigId(Long venueConfigId) {
		this.venueConfigId = venueConfigId;
	}

	public String getVenueDesc() {
		return venueDesc;
	}

	public void setVenueDesc(String venueDesc) {
		this.venueDesc = venueDesc;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	public boolean isIntegrated() {
		return isIntegrated;
	}

	public void setIntegrated(boolean isIntegrated) {
		this.isIntegrated = isIntegrated;
	}
	
	public boolean isParkingOnlyEvent() {
		return isParkingOnlyEvent;
	}

	public void setParkingOnlyEvent(boolean isParkingOnlyEvent) {
		this.isParkingOnlyEvent = isParkingOnlyEvent;
	}
	
	public boolean isGaIndicator() {
		return gaIndicator;
	}

	public void setGaIndicator(boolean gaIndicator) {
		this.gaIndicator = gaIndicator;
	}

	public boolean isSectionScrubbing() {
		return sectionScrubbing;
	}

	public void setSectionScrubbing(boolean sectionScrubbing) {
		this.sectionScrubbing = sectionScrubbing;
	}

	public boolean isRowScrubbing() {
		return rowScrubbing;
	}

	public void setRowScrubbing(boolean rowScrubbing) {
		this.rowScrubbing = rowScrubbing;
	}

	public List<TicketTrait> getTicketTraits() {
		return new ArrayList<TicketTrait>(ticketTraits);
	}

	public void setTicketTraits(List<TicketTrait> ticketTraits) {
		this.ticketTraits = new ArrayList<TicketTrait>(ticketTraits);
	}
	
}
