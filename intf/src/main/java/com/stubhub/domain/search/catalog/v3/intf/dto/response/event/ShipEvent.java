/**
 * Copyright 2014-2106 StubHub, Inc.  All rights reserved.
 */
package com.stubhub.domain.search.catalog.v3.intf.dto.response.event;

import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.stubhub.domain.search.catalog.v3.intf.common.Alias;

/**
 * @author poojsharma
 * @author runiu
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "id", "status", "locale", "name", "description", "eventUrl", "eventDateLocal", "eventDateUTC",
		"venue", "score", "displayAttributes", "aliases", "gameType", "excludeBulkListings" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ShipEvent {

	private Integer id;
	private String status;
	private String locale;
	private String name;
	private String description;
	private String eventUrl;
	private String eventDateLocal;
	private Calendar eventDateUTC;
	private Venue venue;
	private Float score;
	private DisplayAttributes displayAttributes;

	@XmlElementWrapper(name = "aliases", required = false)
	@XmlElement(name = "alias", required = false)
	private List<Alias> aliases;

	private String gameType;
	private String excludeBulkListings = Boolean.FALSE.toString();

	
	public List<Alias> getAliases() {
		return this.aliases;
	}

	public ShipEvent() {
		this.displayAttributes = new DisplayAttributes();
	}

	public Integer getId() {
		return id;
	}

	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getEventUrl() {
		return eventUrl;
	}

	public void setEventUrl(String eventUrl) {
		this.eventUrl = eventUrl;
	}

	public String getEventDateLocal() {
		return eventDateLocal;
	}

	public void setEventDateLocal(String eventDateLocal) {
		this.eventDateLocal = eventDateLocal;
	}

	public Calendar getEventDateUTC() {
		return eventDateUTC;
	}

	public void setEventDateUTC(Calendar eventDateUTC) {
		this.eventDateUTC = eventDateUTC;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setVenueId(Integer venueId) {
		initVenue();
		this.getVenue().setId(Long.valueOf(venueId));
	}

	public void setVenueName(String venueName) {
		initVenue();
		this.getVenue().setName(venueName);
	}

	public void setVenueUrl(String venueUrl) {
		initVenue();
		this.getVenue().setVenueUrl(venueUrl);
	}

	
	public void setVenueConfigurationId(String venueConfigurationId) {
		initVenue();
		this.getVenue().setVenueConfigId(Long.valueOf(venueConfigurationId));
	}

	
	public void setLatitude(Double latitude) {
		initVenue();
		this.getVenue().setLatitude(latitude);
	}

	
	public void setLongitude(Double longitude) {
		initVenue();
		this.getVenue().setLongitude(longitude);
	}

	
	public void setJdkTimezone(String jdkTimezone) {
		initVenue();

		this.getVenue().setTimezone(jdkTimezone);
	}

	
	public void setAddr1(String addr1) {
		initVenue();
		this.getVenue().setAddress1(addr1);
	}

	
	public void setAddr2(String addr2) {
		initVenue();
		this.getVenue().setAddress2(addr2);
	}

	
	public void setCity(String city) {
		initVenue();
		this.getVenue().setCity(city);
	}

	public void setState(String state) {
		initVenue();
		this.getVenue().setState(state);
	}

	public void setCountry(String country) {
		initVenue();
		this.getVenue().setCountry(country);
	}

	public void setPostalCode(String postalCode) {
		initVenue();
		this.getVenue().setPostalCode(postalCode);
	}

	private void initVenue() {
		if (this.getVenue() == null) {
			this.setVenue(new Venue());
		}
	}

	@JsonIgnore
	public String getIdentifier() {
		return String.valueOf(id);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id).append("name", name)
				.append("description", description).append("eventUrl", eventUrl).append("status", status)
				.append("locale", locale).append("eventDateLocal", eventDateLocal).append("eventDateUTC", eventDateUTC)
				.append("venue", venue).append("score", score).append("displayAttributes", displayAttributes)
				.append("aliases", aliases).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(eventDateLocal).append(eventDateUTC).append(description).append(eventUrl)
				.append(id).append(locale).append(name).append(status).append(venue).append(score)
				.append(displayAttributes).append(aliases).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ShipEvent other = (ShipEvent) obj;
		return new EqualsBuilder().append(id, other.id).append(name, other.name).append(status, other.status)
				.append(description, other.description).append(eventUrl, other.eventUrl)
				.append(eventDateLocal, other.eventDateLocal).append(eventDateUTC, other.eventDateUTC)
				.append(locale, other.locale).append(score, other.locale).append(venue, other.venue)
				.append(displayAttributes, other.displayAttributes).append(aliases, other.aliases).isEquals();
	}

	/**
	 * @return the score
	 */
	public Float getScore() {
		return score;
	}

	/**
	 * @param score
	 *            the score to set
	 */
	public void setScore(Float score) {
		this.score = score;
	}

	/**
	 * @return the displayAttributes
	 */
	public DisplayAttributes getDisplayAttributes() {
		return displayAttributes;
	}

	/**
	 * @param displayAttributes
	 *            the displayAttributes to set
	 */
	public void setDisplayAttributes(DisplayAttributes displayAttributes) {
		this.displayAttributes = displayAttributes;
	}

	public void setHidden(int hidden) {
		displayAttributes.setIsHidden(hidden > 0);
	}

	/**
	 * @return the gameType
	 */
	public String getGameType() {
		return gameType;
	}

	/**
	 * @param gameType
	 *            the gameType to set
	 */
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	/**
	 * @param aliases
	 *            the aliases to set
	 */
	public void setAliases(List<Alias> aliases) {
		this.aliases = aliases;
	}

	/**
	 * @return the excludeBulkListings
	 */
	public String getExcludeBulkListings() {
		return excludeBulkListings;
	}

	/**
	 * @param excludeBulkListings the excludeBulkListings to set
	 */
	public void setExcludeBulkListings(String excludeBulkListings) {
		this.excludeBulkListings = excludeBulkListings;
	}

}
