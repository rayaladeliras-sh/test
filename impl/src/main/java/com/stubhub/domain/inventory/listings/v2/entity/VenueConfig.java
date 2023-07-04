package com.stubhub.domain.inventory.listings.v2.entity;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingZone;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;

import java.util.ArrayList;
import java.util.List;

public class VenueConfig {

    private final boolean isGeneralAdmissionOnly;
    private final List<SeatingSection> seatingSections;

    public VenueConfig(boolean isGeneralAdmissionOnly, List<SeatingSection> seatingSections) {
        this.isGeneralAdmissionOnly = isGeneralAdmissionOnly;
        this.seatingSections = seatingSections;

        if (this.seatingSections.isEmpty()) {
            this.seatingSections.add(SeatingSection.DEFAULT);
        }
    }

    public VenueConfig(VenueConfiguration venueConfiguration) {
        this.isGeneralAdmissionOnly = venueConfiguration.getGeneralAdmissionOnly();
        this.seatingSections = new ArrayList<>();

        if (venueConfiguration.getSeatingZones() != null) {
            for (SeatingZone seatingZone : venueConfiguration.getSeatingZones()) {
                if (seatingZone.getSeatingSections() != null) {
                    for (com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingSection seatingSection : seatingZone.getSeatingSections()) {
                        this.seatingSections.add(new SeatingSection(seatingSection.getName(), seatingSection.getGeneralAdmission()));
                    }
                }
            }
        }

        if (this.seatingSections.isEmpty()) {
            this.seatingSections.add(SeatingSection.DEFAULT);
        }
    }

    public boolean isGeneralAdmissionOnly() {
        return isGeneralAdmissionOnly;
    }

    public boolean isGeneralAdmission(String sectionName) {
        for (SeatingSection seatingSection : seatingSections) {
            if (seatingSection.getName().equalsIgnoreCase(sectionName)) {
                return seatingSection.getGeneralAdmission();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "VenueConfig {" +
                "isGeneralAdmissionOnly=" + isGeneralAdmissionOnly +
                ", seatingSections=" + seatingSections +
                '}';
    }
}
