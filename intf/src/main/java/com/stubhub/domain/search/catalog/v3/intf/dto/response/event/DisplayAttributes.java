/**
 * Copyright 2014 StubHub, Inc.  All rights reserved.
 */
package com.stubhub.domain.search.catalog.v3.intf.dto.response.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * @author runiu
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "isHidden", "hideEventDate", "hideEventTime", "integratedEventInd", "primaryName" })
@JsonSerialize(include = Inclusion.NON_NULL)
public class DisplayAttributes {
	private Boolean isHidden = Boolean.FALSE;
	private Boolean hideEventDate = Boolean.FALSE;
	private Boolean hideEventTime = Boolean.FALSE;
	private Long integratedEventInd;
	private String primaryName;

	/**
	 * @return the isHidden
	 */
	public Boolean getIsHidden() {
		return isHidden;
	}

	/**
	 * @param isHidden
	 *            the isHidden to set
	 */
	public void setIsHidden(Boolean isHidden) {
		this.isHidden = isHidden;
	}

	/**
	 * @return the hideEventDate
	 */
	public Boolean getHideEventDate() {
		return hideEventDate;
	}

	/**
	 * @param hideEventDate
	 *            the hideEventDate to set
	 */
	public void setHideEventDate(Boolean hideEventDate) {
		this.hideEventDate = hideEventDate;
	}

	/**
	 * @return the hideEventTime
	 */
	public Boolean getHideEventTime() {
		return hideEventTime;
	}

	/**
	 * @param hideEventTime
	 *            the hideEventTime to set
	 */
	public void setHideEventTime(Boolean hideEventTime) {
		this.hideEventTime = hideEventTime;
	}

	/**
	 * @return the integratedEventInd
	 */
	public Long getIntegratedEventInd() {
		return integratedEventInd;
	}

	/**
	 * @param integratedEventInd
	 *            the integratedEventInd to set
	 */
	public void setIntegratedEventInd(Long integratedEventInd) {
		this.integratedEventInd = integratedEventInd;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("isHidden", isHidden)
				.append("hideEventDate", hideEventDate).append("hideEventTime", hideEventTime)
				.append("integratedEventInd", integratedEventInd).append("primaryName", primaryName).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(isHidden).append(hideEventDate).append(hideEventTime)
				.append(integratedEventInd).append(primaryName).toHashCode();
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

		DisplayAttributes other = (DisplayAttributes) obj;
		return new EqualsBuilder().append(isHidden, other.isHidden).append(hideEventDate, other.hideEventDate)
				.append(hideEventTime, other.hideEventTime).append(integratedEventInd, other.integratedEventInd)
				.append(primaryName, other.primaryName).isEquals();
	}

	/**
	 * @return the primaryName
	 */
	public String getPrimaryName() {
		return primaryName;
	}

	/**
	 * @param primaryName
	 *            the primaryName to set
	 */
	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}
}
