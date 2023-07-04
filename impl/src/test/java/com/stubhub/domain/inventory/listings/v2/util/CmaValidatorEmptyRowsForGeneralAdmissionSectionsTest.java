package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingSection;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingZone;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CmaValidatorEmptyRowsForGeneralAdmissionSectionsTest {

  private final static Long EVENT_ID = 1L;

  CmaValidator cmaValidator;

  private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

  private static final List<String> VALID_GA_SECTIONS = Arrays.asList("American Express VIP Summer Garden",
          "General Admission", "Gold Circle East", "The All VIP Terrace", "Ultimate Bar Diamond", "VIP Diamond View",
          "Feuerzone", "Standard Ticket", "Gold Ticket", "VIP Hydeaway", "Adult Admission and Grandstand",
          "Adult Admission Only", "American Express Summer Garden", "Bedroom Pod", "Bronze Camping",
          "Circle Unreserved", "Front Pitch Standing", "Furlong Club", "Furlong Club Queen Ann Enclosure",
          "Gold Camping", "Gold Circle East", "Gold Circle West", "Gold NO Camping", "Gold Ticket",
          "Grandstand Admission Lawn", "Grandstand Seating", "Hallway Pod", "King Edward VII Enclosure", "Kitchen Pod",
          "Pitch Standing", "Queen Anne Enclosure", "Rear Pitch Standing", "Royal Ascot Village",
          "Royal Enclosure Lawn", "Royal Enclosure Seating", "Silver Camping", "Silver Ring", "Stalls Standing",
          "Standard Camping", "Standard GA", "Standard NO Camping", "Standard Ticket", "Standing Floor",
          "The ALL Terrace", "The All VIP Terrace", "The Royal Ascot Village Hospitality Package",
          "Ultimate Bar Diamond", "Village Enclosure", "VIP", "VIP Ticket", "Windsor Enclosure",
          "Young Person Admission Only");

  @Before
  public void setUp() {
    cmaValidator = new CmaValidator();
    venueConfigV3ApiHelper = mock(VenueConfigV3ApiHelper.class);
    when(venueConfigV3ApiHelper.getVenueDetails(any(Long.class))).thenReturn(mockVenueConfiguration());
    ReflectionTestUtils.setField(cmaValidator, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
  }

  @Test
  public void testGeneralAdmissionNARowWithValidGASections() {
    for (String section : VALID_GA_SECTIONS) {
      assertTrue("Section \'" + section + "\' should be valid", cmaValidator.isValidRow(EVENT_ID, "NA", section));
    }
  }

  @Test
  public void testGeneralAdmissionNARowWithInvalidGASections() {
    String section = "INVALID_SECTION";
    assertFalse("Section \'" + section + "\' should be invalid", cmaValidator.isValidRow(EVENT_ID, "NA", section));
  }

  private static VenueConfiguration mockVenueConfiguration() {
    VenueConfiguration venueConfig = new VenueConfiguration();
    venueConfig.setGeneralAdmissionOnly(false);
    SeatingZone seatingZone = new SeatingZone();
    List<SeatingSection> seatingSections = new ArrayList<>();
    for (String section : VALID_GA_SECTIONS) {
      SeatingSection seatingSection = new SeatingSection();
      seatingSection.setName(section);
      seatingSection.setGeneralAdmission(true);
      seatingSections.add(seatingSection);
    }
    seatingZone.setSeatingSections(seatingSections);
    venueConfig.setSeatingZones(Arrays.asList(seatingZone));
    return venueConfig;
  }
}