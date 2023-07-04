/**
 * 
 */
package com.stubhub.domain.inventory.v2.bulk.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.domain.inventory.common.util.Response;

/**
 * @author sjayaswal
 *
 */
@XmlRootElement(name = "bulkListingResponse")
@JsonRootName(value = "bulkListingResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "jobGuid", "tealeafSessionId","threatMatrixSessionId"
		})
public class BulkListingResponse extends Response {

	@XmlElement(name = "jobGuid", required = true)
	private Long jobGuid;

	/**
	 * @return the jobGuid
	 */
	public Long getJobGuid() {
		return jobGuid;
	}

	/**
	 * @param jobGuid the jobGuid to set
	 */
	public void setJobGuid(Long jobGuid) {
		this.jobGuid = jobGuid;
	}
	
	
}
