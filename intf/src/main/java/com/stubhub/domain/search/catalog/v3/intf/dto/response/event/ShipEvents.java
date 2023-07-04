
/**
 * Copyright 2014 StubHub, Inc.  All rights reserved.
 */
package com.stubhub.domain.search.catalog.v3.intf.dto.response.event;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.stubhub.common.response.Response;

/**
 * @author poojsharma
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
@XmlType(propOrder = { "numFound", "events", "venueMatched" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ShipEvents extends Response {
	private Long numFound;

	@XmlElementWrapper(name = "events")
	@XmlElement(name = "event")
	private List<ShipEvent> events = new ArrayList<ShipEvent>();

	@XmlElement(name = "venueMatched", required = false)
	private Boolean venueMatched;

	public Long getNumFound() {
		return numFound;
	}

	public void setNumFound(Long numFound) {
		this.numFound = numFound;
	}

	public List<ShipEvent> getEvents() {
		return events;
	}

	public void setEvents(List<ShipEvent> events) {
		this.events = events;
	}

	public Boolean getVenueMatched() {
		return venueMatched;
	}

	public void setVenueMatched(Boolean venueMatched) {
		this.venueMatched = venueMatched;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(events).append(numFound)
				.toHashCode();
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
		ShipEvents other = (ShipEvents) obj;
		return new EqualsBuilder()
		.append(events, other.events)
		.append(numFound, other.numFound).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("numFound", numFound)
		.append("events", events)
		.toString();
	}
}
