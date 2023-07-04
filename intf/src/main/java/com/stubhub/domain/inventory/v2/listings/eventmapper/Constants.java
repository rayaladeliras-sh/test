package com.stubhub.domain.inventory.v2.listings.eventmapper;

public class Constants {

  public static final String TIMEZONE_TYPE_LOCAL = "Local";
  public static final String TIMEZONE_TYPE_UTC = "UTC";

  public static final String SHIP_EVENT_MATCHED_MESSAGE = "SUCCESS Ship Event matched";
  public static final String SHIP_EVENT_NOT_MATCHED_MESSAGE = "WARNING Ship event not matched";
  public static final String SHIP_EVENT_MATCHED_SHIP_API_REASON = "Ship Event matched from Api response";
  public static final String SHIP_EVENT_NOT_MATCHED_SHIP_API_REASON = "No Ship Event in Api response";
  public static final String MULTIPLE_SHIP_EVENTS_API_REASON = "Multiple Catalog Search Exact events match from Api response";
  public static final String SHIP_EVENT_NOT_FOUND_REASON = "No Ship Event Api match";
  public static final String MULTIPLE_SHIP_EVENTS_FOUND_MESSAGE =
      "Multiple Catalog Search event match";
  public static final String EXACT_LOCAL_DATE_MATCH_REASON = "Applied Exact Local Date match";
  public static final String EXACT_UTC_DATE_MATCH_REASON = "Applied Exact UTC Date match";
  public static final String EXACT_DATE_TIME_MATCH_REASON = "Applied Exact Date and Time match";
  public static final String EXACT_DATE_RANGE_MATCH_REASON = "Applied Date Range match";
  public static final String ALIASED_MATCH_REASON = "Applied Aliases match";
  public static final String SHIP_EVENT_NOT_FOUND_ALIASES_REASON =
      "No Ship Search Event Aliases matched";
public static final String MULTIPLE_SHIP_EVENTS_LOCAL_DATE_MATCH="Multiple Catalog Search events matched with exact local date match";
public static final String MULTIPLE_SHIP_EVENTS_UTC_DATE_MATCH="Multiple Catalog Search events matched with exact UTC date match";
  public static final String MULTIPLE_SHIP_EXACT_EVENTS_FOUND_MESSAGE =
      "Multiple Catalog Search Exact events match";
  public static final String MULTIPLE_SHIP_ALIASES_EVENTS_FOUND_MESSAGE =
      "Multiple Catalog Search Aliases events match";
  public static final String MULTIPLE_SHIP_DATERANGE_EVENTS_FOUND_MESSAGE =
      "Multiple Catalog Search DateRange events match";

  public static final String SHIP_EVENT_NOT_FOUND_VENUE_FOUND_MESSAGE = "WARNING Venue Found but Event Not Found on Stubhub";
  public static final String VENUE_NOT_FOUND_MESSAGE = "WARNING No Venue Found on Stubhub. Sent for Venue Alias Creation";
  public static final String EVENT_NOT_FOUND_MESSAGE = "WARNING No Event Found on Stubhub. Sent for Event Alias Creation";
  public static final String VENUE_EXISTS_IN_MULTI_LOCALE = "Same Venue found in Multiple US and other Locales";
  public static final String VENUE_EXISTS_IN_MULTI_LOCALE_REASON = "Venue found in Multiple Locales";
}
