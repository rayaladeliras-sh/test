package com.stubhub.domain.inventory.listings.v2.seller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SpiConvertTrackingMessage {
  private Long convertTrackingId;
  private Long batchSize;
}
