package com.stubhub.domain.inventory.listings.v2.tns.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListingEvalResponseMessage {
    private Long listingId;
    private AsyncListingFraudDecision fraudCheckStatus;
}
