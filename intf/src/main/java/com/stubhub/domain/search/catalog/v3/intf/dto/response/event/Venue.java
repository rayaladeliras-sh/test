/**
 * Copyright 2014-2017 StubHub, Inc.  All rights reserved.
 */
package com.stubhub.domain.search.catalog.v3.intf.dto.response.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author runiu
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "venue", propOrder = { "id", "status", "name", "description", "venueUrl", "url", "webURI", "seoURI",
		"latitude", "longitude", "timezone", "jdkTimezone", "hidden", "address1", "address2", "city", "state",
		"postalCode", "country", "venueConfigId" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public class Venue {
	private Long id;
	private String status;
	private String name;
	private String description;
	private String url;
	private String webURI;
	private String seoURI;
	private String venueUrl;
	private Double latitude;
	private Double longitude;
	private String timezone;
	private String jdkTimezone;
	private Boolean hidden;
	private String address1;
	private String address2;
	private String city;
	private String state;
	private String postalCode;
	private String country;
	private Long venueConfigId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public String getVenueUrl() {
		return venueUrl;
	}

	public void setVenueUrl(String venueUrl) {
		this.venueUrl = venueUrl;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
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

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getVenueConfigId() {
		return venueConfigId;
	}

	public void setVenueConfigId(Long venueConfigId) {
		this.venueConfigId = venueConfigId;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(address1).append(address2).append(city).append(country).append(description)
				.append(hidden).append(id).append(latitude).append(longitude).append(name).append(state).append(status)
				.append(url).append(webURI).append(timezone).append(jdkTimezone).append(postalCode)
				.append(venueConfigId).toHashCode();

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
		Venue other = (Venue) obj;
		return new EqualsBuilder().append(address1, other.address1).append(address2, other.address2)
				.append(city, other.city).append(country, other.country).append(description, other.description)
				.append(hidden, other.hidden).append(id, other.id).append(latitude, other.latitude)
				.append(longitude, other.longitude).append(name, other.name).append(state, other.state)
				.append(status, other.status).append(url, other.url).append(webURI, other.webURI)
				.append(timezone, other.timezone).append(jdkTimezone, other.jdkTimezone)
				.append(postalCode, other.postalCode).append(venueConfigId, other.venueConfigId).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("address1", address1)
				.append("address2", address2).append("city", city).append("country", country)
				.append("description", description).append("hidden", hidden).append("id", id)
				.append("latitude", latitude).append("longitude", longitude).append("name", name).append("state", state)
				.append("status", status).append("url", url).append("webURI", webURI).append("seoURI", seoURI)
				.append("timezone", timezone).append("jdkTimezone", jdkTimezone).append("postalCode", postalCode)
				.append("venueConfigId", venueConfigId).toString();
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the webURI
	 */
	public String getWebURI() {
		return webURI;
	}

	/**
	 * @param webURI
	 *            the webURI to set
	 */
	public void setWebURI(String webURI) {
		this.webURI = webURI;
	}

	/**
	 * @param webURI
	 *            the seoURI to set using webURI
	 */
	public void setSeoURI(String webURI) {
		this.seoURI = webURI.split("/")[0];
	}

	/**
	 * @return the seoURI
	 */
	public String getSeoURI() {
		return seoURI;
	}

	/**
	 * @return the jdkTimezone
	 */
	public String getJdkTimezone() {
		return jdkTimezone;
	}

	/**
	 * @param jdkTimezone
	 *            the jdkTimezone to set
	 */
	public void setJdkTimezone(String jdkTimezone) {
		this.jdkTimezone = jdkTimezone;
	}

}
