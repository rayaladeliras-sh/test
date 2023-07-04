package com.stubhub.domain.inventory.listings.v2.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.util.ReflectionTestUtils;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class CmaValidatorBannedRowsTest {

  private final String row;
  private final static Long EVENT_ID = 1L;

  CmaValidator cmaValidator;

  private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

  public CmaValidatorBannedRowsTest(String row) {
    this.row = row;
  }

  @Before
  public void setUp() {
    cmaValidator = new CmaValidator();
    venueConfigV3ApiHelper = mock(VenueConfigV3ApiHelper.class);
    ReflectionTestUtils.setField(cmaValidator, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
  }

  @Parameters()
  public Collection<Object[]> badTermsProvider() {
    return Arrays.asList(
        new Object[] {"Platea"}, new Object[] {"FRONT"}, new Object[] {"Swallows"}, new Object[] {"stand"}, new Object[] {"Ótimo"}, new Object[] {"GOLD"}, new Object[] {"tobc1"}, new Object[] {"GA"},
        new Object[] {"Platino"}, new Object[] {"5x"}, new Object[] {"N.A"}, new Object[] {"TOP"}, new Object[] {"Gold"}, new Object[] {"Poltriniss"}, new Object[] {"1x"}, new Object[] {"Undecided"}, new Object[] {"Good"}, new Object[] {"Recht"},
        new Object[] {"TBA"}, new Object[] {"NA"}, new Object[] {"Unreserved"}, new Object[] {"POPUL"},
        new Object[] {"Reihe"}, new Object[] {"TBC"}, new Object[] {"PLAT"}, new Object[] {"Vip"}, new Object[] {"Innen"}, new Object[] {"Reihe C"}, new Object[] {"TBD"}, new Object[] {"ToBeConf"}, new Object[] {"Visitor"}, new Object[] {"Libre"},
        new Object[] {"Seiten"}, new Object[] {"CENTER"}, new Object[] {"Student"}, new Object[] {"Arena"}, new Object[] {"Mitte"}, new Object[] {"GRADA"}, new Object[] {"Front"}, new Object[] {"Suite"}, new Object[] {"Free"}, new Object[] {"Neben"},
        new Object[] {"Steh"}, new Object[] {"STEHPLATZ"}, new Object[] {"Bassi"}, new Object[] {"Debout"}, new Object[] {"Entrada"}, new Object[] {"BEST"}, new Object[] {"BE$T"}, new Object[] {"+"}, new Object[] {"?"},
        new Object[] {"Unknown"}, new Object[] {"none"}, new Object[] {"pit"}, new Object[] {"Seated"}, new Object[] {"Together"}, new Object[] {"Confirmed"}, new Object[] {"TC"}, new Object[] {"Tobeconfirmed"}, new Object[] {"TOB"}, new Object[] {"TBT"}, new Object[] {"Nice"}
    );
  }

  @Test
  public void testBannedTerms() {
    when(venueConfigV3ApiHelper.getVenueDetails(any(Long.class))).thenReturn(mockVenueConfiguration());
    assertFalse("Parameter: " + row + " should not be valid", cmaValidator.isValidRow(EVENT_ID, row, "section ducks"));
  }

  private static VenueConfiguration mockVenueConfiguration() {
    VenueConfiguration venueConfig = new VenueConfiguration();
    venueConfig.setGeneralAdmissionOnly(false);
    venueConfig.setSeatingZones(Collections.EMPTY_LIST);
    return venueConfig;
  }
}