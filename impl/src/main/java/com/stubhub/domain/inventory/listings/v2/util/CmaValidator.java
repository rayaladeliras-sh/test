package com.stubhub.domain.inventory.listings.v2.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.listings.v2.entity.VenueConfig;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.v2.DTO.Product;

@Component
public class CmaValidator {

    @Autowired
    private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

    public static final String BAD_ROW_TERMS = "Platea,FRONT,Swallows,stand,Ótimo,GOLD,tobc1,GA,Platino,5x,N.A,TOP,Gold,Poltriniss,1x,Undecided,Good,Recht,TBA,NA,Unreserved,POPUL,"
            + "Reihe,TBC,PLAT,Vip,Innen,Reihe C,TBD,ToBeConf,Visitor,Libre,Seiten,CENTER,Student,Arena,Mitte,GRADA,Front,Suite,Free,Neben,Steh,STEHPLATZ,Bassi,Debout,Entrada,BEST,BE$T,+,?,"
            + "!,Unknown,none,pit,Seated,Together,Confirmed,TC,Tobeconfirmed,TOB,TBT,Nice";

    public static final String BAD_SEAT_TERMS = "Platea,FRONT,Swallows,stand,Ótimo,X,GOLD,tobc1,Platino,5x,N.A,TOP,Gold,Poltriniss,1x,N/A,Undecided,Good,Recht,TBA,NA,Unreserved,POPUL,"
            + "Reihe,TBC,PLAT,Vip,Innen,Reihe C,TBD,ToBeConf,Visitor,Libre,Seiten,CENTER,Student,Arena,Mitte,GRADA,Front,Suite,Free,Neben,Steh,STEHPLATZ,Bassi,Debout,Entrada,BEST,BE$T,+,?,"
            + "!,Unknown,none,pit,Seated,Together,Confirmed,TC,Tobeconfirmed,TOB,TBT,Nice";

    public static final String[] BAD_FULL_VALUES = {"00", "000"};

    public static final List<String> GENERAL_ADMISSION_ROW_COMMON_VALUES = Collections.unmodifiableList(Arrays.asList("NA", "N/A"));

    public boolean isValidRow(Long eventId, Product product, String section) {
        return isValidRow(eventId, product.getRow(), section);
    }

    public boolean isValidRow(Long eventId, String row, String section) {
        if (StringUtils.isBlank(row)) {
            return false;
        }

        if (isGeneralAdmissionSection(eventId, section) && isGeneralAdmissionRow(row)) {
            return true;
        }

        if (!StringUtils.isAlphanumericSpace(row)) {
            return false;
        }

        if (isBannedFullValue(row)) {
            return false;
        }

        String lowerCaseValue = row.toLowerCase();
        for (String bannedTerm : BAD_ROW_TERMS.toLowerCase().split(",")) {
            if(lowerCaseValue.contains(bannedTerm)) {
                return false;
            }
        }

        return true;
    }

    private boolean isGeneralAdmissionRow(String row) {
        return GENERAL_ADMISSION_ROW_COMMON_VALUES.contains(row.trim().toUpperCase(Locale.ENGLISH));
    }

    private boolean isGeneralAdmissionSection(Long eventId, String section) {
        if (null == section || StringUtils.isBlank(section)) {
            return true;
        }

        VenueConfiguration venueConfigDTO = venueConfigV3ApiHelper.getVenueDetails(eventId);
        VenueConfig venueConfig = new VenueConfig(venueConfigDTO);

        return venueConfig.isGeneralAdmission(section);
    }

    public boolean isValidSeat(String value) {
        if (!StringUtils.isAlphanumericSpace(value)) {
            return false;
        }

        if (isBannedFullValue(value)) {
            return false;
        }

        String lowerCaseValue = value.toLowerCase();
        for (String bannedTerm : BAD_SEAT_TERMS.toLowerCase().split(",")) {
            if (lowerCaseValue.contains(bannedTerm)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBannedFullValue(String value) {
        for (String bannedValue : BAD_FULL_VALUES) {
            if (bannedValue.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
