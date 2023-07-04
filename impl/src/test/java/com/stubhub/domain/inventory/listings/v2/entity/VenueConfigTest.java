package com.stubhub.domain.inventory.listings.v2.entity;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingZone;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class VenueConfigTest {

    @Test
    public void venue_with_no_ga_section_found() {
        // given
        String sectionName = "section";
        SeatingSection seatingSection = new SeatingSection(sectionName, false);
        // when
        VenueConfig venueConfig = new VenueConfig(false, Arrays.asList(seatingSection));
        // then
        Assert.assertFalse(venueConfig.isGeneralAdmission(sectionName), "Given section should NOT be GA");
    }

    @Test
    public void venue_with_ga_section_found() {
        // given
        String sectionName = "section";
        SeatingSection seatingSection = new SeatingSection(sectionName, true);
        // when
        VenueConfig venueConfig = new VenueConfig(false, Arrays.asList(seatingSection));
        // then
        Assert.assertTrue(venueConfig.isGeneralAdmission(sectionName), "Given section should be GA");
    }

    @Test
    public void venue_without_section() {
        // given
        VenueConfig venueConfig = new VenueConfig(false, new ArrayList<SeatingSection>());
        // then
        Assert.assertTrue(venueConfig.isGeneralAdmission(SeatingSection.DEFAULT.getName()), "Given section should be GA, as the default section value is true");
    }

    @Test
    public void constructor_with_no_zones_returns_default_section() {
        // given
        SeatingZone seatingZone = new SeatingZone();
        seatingZone.setSeatingSections(new ArrayList<com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingSection>());
        VenueConfiguration venueConfiguration = new VenueConfiguration();
        venueConfiguration.setGeneralAdmissionOnly(true);
        venueConfiguration.setSeatingZones(Arrays.asList(seatingZone));
        // when
        VenueConfig venueConfig = new VenueConfig(venueConfiguration);
        //then
        Assert.assertTrue(venueConfig.isGeneralAdmission(SeatingSection.DEFAULT.getName()), "Given section should be GA, as the default section value is true");

    }
}
