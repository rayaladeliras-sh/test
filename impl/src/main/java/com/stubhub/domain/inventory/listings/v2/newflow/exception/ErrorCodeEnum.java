package com.stubhub.domain.inventory.listings.v2.newflow.exception;

public enum ErrorCodeEnum {
	accessToLmsapprovalStatusDenied(""),
	addNotallowedPiggybackListing(""),
	alreadyListed(""),
	barcodeAlreadyUsed(""),
	bulkJobNotFound(""),
	communicationError(""),
	configurationError(""),
	decliningPriceNotAllowed(""),
	deleteNotallowedPiggybackListing(""),
	deliveryOptionNotSupported("The delivery option is not supported, either it's expired or not available for this event"),
	duplicateBarcodeError(""),
	duplicateExternalListingId(""),
	duplicateFulfillmentArtifactError(""),
	duplicateSectionRow(""),
	duplicateSectionRowSeat("A listing with the same section, row, and seat is already available on StubHub for the event"),
	eventExpired("The event has expired"),
	eventNotActive("Event is not Active"),
	eventNotMapped(""),
	hideSeatsNotAllowed(""),
	incorrectQuantityOfSeats(""),
	incorrectSeatsPredelivery(""),
	inputError("ExternalListingId is needed for Bulk Listing Request"),
	inputListingEmpty(""),
	inputSizeExceeded(""),
	invalidBarcode(""),
	invalidBarcodeFormatLength(""),
	invalidBobid(""),
	invalidCcid("Listing ccGuid is not matched with any of the payment instruments"),
	invalidContactId(""),
	invalidCredentials(""),
	invalidDateFormat("Invalid date format"),
	invalidCurrency("Invalid currency"),
	invalidDeliveryoption("Delivery Option is missing OR invalid"),
	invalidDisplayPriceperticket("Invalid price"),
	invalidDomainId(""),
	invalidEventDate(""),
	invalidEventid(""),
	invalidFaceValue("Invalid Face value"),
	invalidFileName(""),
	invalidFmid(""),
	invalidInhanddate("The in hand date provided is after the latest possible in hand date or before the earliest possible in hand date for the event"),
	invalidItem(""),
	invalidListingid("Listing was created by different seller"),
	invalidListings(""),
	invalidLmsStatus(""),
	invalidNetPayout(""),
	invalidOrderId(""),
	invalidOrderRelistStatus(""),
	invalidPaymentType("Invalid payment type"),
	invalidPdfFile(""),
	invalidPiggybackRow("Invalid piggyback number of seats. Minimum of 2 is required"),
	invalidPrice(""),
	invalidPriceperticket("Invalid price"),
	invalidQuantity("The quantity is less than 1"),
	invalidGAQuantity("Cannot increase quantity to add seats for non GA listing"),
	invalidRequest(""),
	invalidRequestContent(""),
	invalidRequestFormat(""),
	invalidRow(""),
	invalidRowWords(""),
	invalidSaleEndate(""),
	invalidSaleid(""),
	invalidSeatNumber(""),
	invalidSeatNumbers(""),
	invalidSection(""),
	invalidSectionWords(""),
	invalidSellerid(""),
	invalidSiteId(""),
	invalidSplitOption(""),
	invalidSplitTickettraitCombination(""),
	invalidSplitValue("Invalid split quantity"),
	invalidSrsMapping(""),
	invalidStartEndPrice(""),
	invalidState(""),
	invalidStatus(""),
	invalidSthInventory(""),
	invalidTicketTrait(""),
	listingActionNotallowed(""),
	listingAlreadySold(""),
	listingExpired("The listing has expired"),
	listingNotActive("Listing has been deleted"),
	listingNotFound("Cannot find listing id"),
	listingPriceTooLow(""),
	listingPriceTooHigh(""),
	listpriceExceededMaxlimit(""),
	maxAllowedExceeded("The quantity is more than 150"),
	maxLengthExceeded(""),
	missingEventId(""),
	missingEventInfo(""),
	missingExternalListingId(""),
	missingListingId(""),
	missingOrderId(""),
	missingQuantity(""),
	missingTicketData(""),
	multipleEventsFound(""),
	multipleParkingPasses(""),
	multipleParkingPassesNotSupported("Cannot add multiple parking passes to a listing"),
	multipleShipAliasesEventsFound(""),
	multipleVenuesFound(""),
	noArtifactFound(""),
	notAllowedToList(""),
	noFulfillmentWindowsAvailable("No fulfillment windows are available"),
	orderCancelled(""),
	parkingPassNotSupported("Parking pass is not supported for this event"),
	pricingApiError("An internal processing error occurred in the system"),
	seatCannotBeNull(""),
	seatNumbersNotallowed(""),
	sellerCreditCardNotfound(""),
	sellerDefaultCheckpaymentContactidNotfound(""),
	sellerDefaultContactNotfound(""),
	sellerDefaultPaymenttypeNotfound(""),
	sellerNoPiInfo(""),
	sellerNotAuthorized(""),
	shipEventNotMapped(""),
	sla02(""),
	sla03(""),
	sla04(""),
	startAndEndPriceRequired(""),
	systemError("System error happened while processing"),
	taxpayerError(""),
	ticketOperationInvalid(""),
	ticketsPdfsMismatch(""),
	ticketTypeNotSupported(""),
	unknownError(""),
	updateNotallowedPiggybackListing(""),
	updateNotallowedSinglePdfFile(""), invalidFraudCheckStatus("");
	
	private String description;
	
	ErrorCodeEnum(String description) {    
    	this.description=description;
    }

	public String getDescription() {
		return description;
	}
}
