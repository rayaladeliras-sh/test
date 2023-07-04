package com.stubhub.domain.inventory.v2.bulk.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name = "bulkJob")
@JsonRootName(value = "bulkJob")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "jobStatus",
    "listingStatus"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkJobStatusRequest {
  
  @XmlElement(name = "jobStatus", required = true)
  public String jobStatus;
  
  @XmlElement(name = "listingStatus", required = false)
  public String listingStatus;

  public String getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }

  public String getListingStatus() {
    return listingStatus;
  }

  public void setListingStatus(String listingStatus) {
    this.listingStatus = listingStatus;
  }
  
}
