package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CmaValidatorTest extends TestCase {

  private final static Long EVENT_ID = 1L;

  CmaValidator cmaValidator;
  private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

  @Before
  public void setUp() {
    cmaValidator = new CmaValidator();
    venueConfigV3ApiHelper = mock(VenueConfigV3ApiHelper.class);
    when(venueConfigV3ApiHelper.getVenueDetails(any(Long.class))).thenReturn(mockVenueConfiguration());
    ReflectionTestUtils.setField(cmaValidator, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
  }

  @Test
  public void testBlankRowsAreNotValid() {
    assertFalse(cmaValidator.isValidRow(EVENT_ID, "", ""));
  }

  @Test
  public void testNullRowsAreNotValid() {
    assertFalse(cmaValidator.isValidRow(EVENT_ID, (String) null, ""));
  }

  @Test
  public void testGeneralAdmissionNARowsForBlankGeneralAdmissionSection() {
    assertTrue(cmaValidator.isValidRow(EVENT_ID, "N/A", ""));
  }

  @Test
  public void testGeneralAdmissionRowsForBlankGeneralAdmissionSection() {
    assertTrue(cmaValidator.isValidRow(EVENT_ID, "N/A", ""));
  }

  @Test
  public void testGeneralAdmissionRowsForNullGeneralAdmissionSection() {
    assertTrue(cmaValidator.isValidRow(EVENT_ID, "N/A", null));
  }

  @Test
  public void testGeneralAdmissionNARowsForNullGeneralAdmissionSection() {
    assertTrue(cmaValidator.isValidRow(EVENT_ID, "NA", null));
  }

  @Test
  public void testNoGeneralAdmissionNARowsForNullGeneralAdmissionSection() {
    assertFalse(cmaValidator.isValidRow(EVENT_ID, "N!A", null));
  }

  @Test
  public void test00BannedValue() {
    assertFalse(cmaValidator.isValidRow(EVENT_ID, "00", null));
  }

  @Test
  public void test000BannedValue() {
    assertFalse(cmaValidator.isValidRow(EVENT_ID, "000", null));
  }

  @Test
  public void testValidRowForNullSection() {
    assertTrue(cmaValidator.isValidRow(EVENT_ID, "I am valid", null));
  }

  @Test
  public void testValidRowForNonNullSection() {
    assertTrue(cmaValidator.isValidRow(EVENT_ID, "I am valid", "Section Ducks"));
  }

  private static VenueConfiguration mockVenueConfiguration() {
    VenueConfiguration venueConfig = new VenueConfiguration();
    venueConfig.setGeneralAdmissionOnly(false);
    venueConfig.setSeatingZones(Collections.EMPTY_LIST);
    return venueConfig;
  }
}