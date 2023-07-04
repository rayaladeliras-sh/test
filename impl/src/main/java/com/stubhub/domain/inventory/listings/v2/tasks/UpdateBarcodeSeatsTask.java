package com.stubhub.domain.inventory.listings.v2.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.entity.ErrorDetail;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import org.slf4j.MDC;

public class UpdateBarcodeSeatsTask implements CallableInventoryTask<Listing> {

  private SHAPIContext apiContext;
  private PrimaryIntegrationUtil primaryIntegrationUtil;
  private SeatProductsContext seatProdContext;
  private final Map<String, String> context;
  private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();
  private final static Logger log = Logger.getLogger(UpdateBarcodeSeatsTask.class);

  public UpdateBarcodeSeatsTask(SeatProductsContext seatProdContext, SHAPIContext apiContext,
      PrimaryIntegrationUtil primaryIntegrationUtil) {
    this.apiContext = apiContext;
    this.primaryIntegrationUtil = primaryIntegrationUtil;
    this.seatProdContext = seatProdContext;
    this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
  }

  @Override
  public Listing call() throws Exception {
    MDC.setContextMap(this.context);
    // All added ticket seats to db are stored here (used for cleanup if the verification fails)
    // Notify primary about barcodes
    ErrorDetail errorDetail = null;
    errorDetail = primaryIntegrationUtil.verifyAndPersistBarcodes(
        seatProdContext.getCurrentListing(), seatProdContext.getBarcodeSeatProductList(),seatProdContext.isValidateBarcode());
    if (errorDetail != null) {
      ListingError listingError = new ListingError(ErrorType.INPUTERROR, errorDetail.getErrorCode(),
          errorDetail.getErrorDescription(), "barcode", errorDetail.getCategoryId(),
          errorDetail.getCategoryDescription(), errorDetail.getErrorId(),
          errorDetail.getSubErrorCode());

      throw new ListingBusinessException(listingError);
    }

    log.debug("END validateBarcodes task");
    return seatProdContext.getCurrentListing();
  }

  @Override
  public boolean ifNeedToRunTask() {
    List<SeatProduct> barcodeSeats = seatProdContext.getBarcodeSeatProductList();
    return barcodeSeats != null && barcodeSeats.size() > 0;
  }
}
