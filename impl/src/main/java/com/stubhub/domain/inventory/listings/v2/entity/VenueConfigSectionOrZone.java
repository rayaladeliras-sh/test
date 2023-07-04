package com.stubhub.domain.inventory.listings.v2.entity;

public class VenueConfigSectionOrZone {
  
  private Long venueConfigSectionId;
  
  private boolean generalAdmission;
  
  private Long venueConfigZoneId;
  
  public VenueConfigSectionOrZone() {
    super();
  }

  public Long getVenueConfigSectionId() {
    return venueConfigSectionId;
  }

  public void setVenueConfigSectionId(Long venueConfigSectionId) {
    this.venueConfigSectionId = venueConfigSectionId;
  }

  public boolean isGeneralAdmission() {
    return generalAdmission;
  }

  public void setGeneralAdmission(boolean generalAdmission) {
    this.generalAdmission = generalAdmission;
  }

  public Long getVenueConfigZoneId() {
	return  venueConfigZoneId;
  }

  public void setVenueConfigZoneId(Long venueConfigZoneId) {
	this.venueConfigZoneId = venueConfigZoneId;
  }
  
}
