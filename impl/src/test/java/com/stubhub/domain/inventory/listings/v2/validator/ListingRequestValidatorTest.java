package com.stubhub.domain.inventory.listings.v2.validator;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;
import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.listings.v2.util.ResourceManager;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.LMSApprovalStatus;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import junit.framework.Assert;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListingRequestValidatorTest extends SHInventoryTest {

	private static final Long EVENT_ID = 1L;
	private ListingRequest requestListing;
	private ListingRequestValidator validator = new ListingRequestValidator();
	private ResourceManager rm = new ResourceManager();
	private ListingTextValidatorUtil listingTextValidatorUtil = new ListingTextValidatorUtil();
	private CmaValidator cmaValidator = new CmaValidator();
	private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

	private MasterStubhubPropertiesWrapper masterStubhubProperties;

	@BeforeMethod
	public void setUp () throws Exception {
		requestListing = new ListingRequest();
		requestListing.setQuantity(2);
		requestListing.setBuyerSeesPerProduct(new com.stubhub.newplatform.common.entity.Money("10"));

		requestListing.setFaceValue(new com.stubhub.newplatform.common.entity.Money("5"));
		requestListing.setPurchasePrice(new com.stubhub.newplatform.common.entity.Money("5"));
		requestListing.setDeliveryOption(DeliveryOption.BARCODE);
		requestListing.setSplitOption(SplitOption.NOSINGLES);
		rm.resetInstance();
		ReflectionTestUtils.setField(listingTextValidatorUtil, "resourceManager", rm);
		ReflectionTestUtils.setField(validator, "listingTextValidatorUtil", listingTextValidatorUtil);
		venueConfigV3ApiHelper = mock(VenueConfigV3ApiHelper.class);
		when(venueConfigV3ApiHelper.getVenueDetails(any(Long.class))).thenReturn(mockVenueConfiguration());
		ReflectionTestUtils.setField(cmaValidator, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
		ReflectionTestUtils.setField(validator, "cmaValidator", cmaValidator);

		masterStubhubProperties = mock(MasterStubhubPropertiesWrapper.class);
		ReflectionTestUtils.setField(validator, "masterStubhubProperties", masterStubhubProperties);
		when(masterStubhubProperties.getProperty("facevalue.required.countries", "GB,LU,AT,DE")).thenReturn("GB,LU,AT,DE");
	}

	@Test
	public void testValidatePositiveScenario() {
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
	}

	@Test
	public void testValidatePricingsValues()
	{
		List<ListingError> errors = null;
		requestListing = new ListingRequest();
		requestListing.setBuyerSeesPerProduct(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);

		requestListing = new ListingRequest();
		requestListing.setFaceValue(new Money("0"));


		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "GB",null);
		assertTrue(errors.size()>0);
		errors =  validator.validate(requestListing, EVENT_ID, true, false, Locale.US, "", null, null, "GB",null);
		assertTrue(errors.size()>0);

		requestListing.setFaceValue(null);
		errors =  validator.validate(requestListing, EVENT_ID, true, false, Locale.US, "", null, null, "GB",null);
		assertTrue(errors.size()>0);

		requestListing.setFaceValue(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());

		requestListing = new ListingRequest();
		requestListing.setPurchasePrice(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);

		requestListing = new ListingRequest();
		requestListing.setStatus(ListingStatus.ACTIVE);
		requestListing.setPricePerProduct(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);

		requestListing = new ListingRequest();
		requestListing.setPayoutPerProduct(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);

		requestListing = new ListingRequest();
		requestListing.setDeliveryOption(DeliveryOption.STH);
		errors =  validator.validate(requestListing, EVENT_ID, true, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);

		requestListing = new ListingRequest();
		requestListing.setStatus(null);
		requestListing.setPricePerProduct(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
		Assert.assertEquals(ErrorCode.INVALID_PRICEPERTICKET, errors.get(0).getCode());


		requestListing = new ListingRequest();
		requestListing.setStatus(ListingStatus.HIDDEN);
		requestListing.setPricePerProduct(new Money("0"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
	}

	@Test
	public void testValidateQuantity() {
		List<ListingError> errors = null;
		requestListing.setQuantity(new Integer(0));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
	}

	@Test
	public void testValidateQuantity_Max() {
		List<ListingError> errors = null;
		requestListing.setQuantity(new Integer("155"));
		errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
	}

	@DataProvider(name = "bannedFullValues")
	public static Object[][] bannedFullValues() {
		String[] badValues = CmaValidator.BAD_FULL_VALUES;
		Object[][] result = new Object[badValues.length][1];

		for (int i = 0; i < badValues.length; i++) {
			result[i][0] = badValues[i];
		}
		return result;
	}

	@Test(dataProvider = "bannedFullValues")
	public void givenCMAListingWithBannedFullValueInRowField_thenItFails(String bannedValue) {
		ListingRequest request = aListingRequest();
		request.setProducts(asList(getProduct(Operation.ADD, bannedValue, "1", ProductType.TICKET, null, "ex")));

		List<ListingError> errors = validator.validate(request, EVENT_ID, true, false, Locale.US, "", null, null, "GB", null);

		assertContainsError(errors, ErrorType.INPUTERROR, ErrorCode.INVALID_ROW_WORDS,
				"Row/Seat contains words that are not allowed", "row");
	}

	public void assertContainsError(List<ListingError> errors, ErrorType type, ErrorCode code, String message, String parameter) {
		boolean found = false;
		for (ListingError error : errors) {
			if (error.getType().equals(type) && error.getCode().equals(code) && error.getMessage().equals(message) && error.getParameter().equals(parameter)) {
				found = true;
				break;
			}
		}
		assertTrue(format("Expected error not found {Type: %s, Code: %s, Message: \"%s\", Parameter: \"%s\"}",
				type, code, message, parameter), found);
	}

	@Test(dataProvider = "bannedFullValues")
	public void givenCMAListingWithBannedFullValueInSeatField_thenItFails(String bannedValue) {
		ListingRequest request = aListingRequest();
		request.setProducts(asList(getProduct(Operation.ADD, "1", bannedValue, ProductType.TICKET, null, "ex")));

		List<ListingError> errors = validator.validate(request, EVENT_ID, true, false, Locale.US, "", null, null, "GB", null);

		assertContainsError(errors, ErrorType.INPUTERROR, ErrorCode.INVALID_SEAT_NUMBER,
				"Row/Seat contains words that are not allowed", "seat");
	}

	private static List<String> sanitizeAndFilterEmptyStrings(String[] badTerms) {
		ResourceManager resourceManager = new ResourceManager();
		ListingTextValidatorUtil listingTextValidatorUtil = new ListingTextValidatorUtil();
		ReflectionTestUtils.setField(listingTextValidatorUtil, "resourceManager", resourceManager);
		List<String> filteredBadTerms = new ArrayList<>();

		for (String badTerm : badTerms) {
			String sanitizedTerm = listingTextValidatorUtil.removeSpecialCharactersFromRowSeat (badTerm, Locale.US);
			if (!sanitizedTerm.isEmpty()) {
				filteredBadTerms.add(sanitizedTerm);
			}
		}
		return filteredBadTerms;
	}

	@DataProvider(name = "bannedPartialValuesForRow")
	public static Object[][] bannedPartialValuesForRow() {
		// Row value is sanitized before perform validation checks, so we need to remove those terms that result in
		// an empty string after sanitization
		List<String> badTerms = sanitizeAndFilterEmptyStrings(CmaValidator.BAD_ROW_TERMS.split(","));
		Object[][] result = new Object[badTerms.size() * 4][1];

		for (int i = 0; i < badTerms.size(); i++) {
			int baseOffset = i * 4;
			String currentTerm = badTerms.get(i);
			result[baseOffset][0] = currentTerm;
			result[baseOffset + 1][0] = "prefix" + currentTerm;
			result[baseOffset + 2][0] = currentTerm + "suffix";
			result[baseOffset + 3][0] = "prefix" + currentTerm + "suffix";
		}
		return result;
	}

	@Test(dataProvider = "bannedPartialValuesForRow")
	public void givenCMAListingWithBannedPartialValueInRowField_thenItFails(String bannedValue) {
		ListingRequest request = aListingRequest();
		request.setProducts(asList(getProduct(Operation.ADD, bannedValue, "1", ProductType.TICKET, null, "ex")));

		List<ListingError> errors = validator.validate(request, EVENT_ID, true, false, Locale.US, "", null, null, "GB", null);

		assertContainsError(errors, ErrorType.INPUTERROR, ErrorCode.INVALID_ROW_WORDS,
				"Row/Seat contains words that are not allowed", "row");
	}

	@DataProvider(name = "bannedPartialValuesFoSeat")
	public static Object[][] bannedPartialValuesForSeat() {
		// Seat value is sanitized before perform validation checks, so we need to remove those terms that result in
		// an empty string after sanitization
		List<String> badTerms = sanitizeAndFilterEmptyStrings(CmaValidator.BAD_SEAT_TERMS.split(","));
		Object[][] result = new Object[badTerms.size() * 4][1];

		for (int i = 0; i < badTerms.size(); i++) {
			int baseOffset = i * 4;
			String currentTerm = badTerms.get(i);
			result[baseOffset][0] = currentTerm;
			result[baseOffset + 1][0] = "prefix" + currentTerm;
			result[baseOffset + 2][0] = currentTerm + "suffix";
			result[baseOffset + 3][0] = "prefix" + currentTerm + "suffix";
		}
		return result;
	}

	@Test(dataProvider = "bannedPartialValuesForSeat")
	public void givenCMAListingWithBannedPartialValueInSeatField_thenItFails(String bannedValue) {
		ListingRequest request = aListingRequest();
		request.setProducts(asList(getProduct(Operation.ADD, "1", bannedValue, ProductType.TICKET, null, "ex")));

		List<ListingError> errors = validator.validate(request, EVENT_ID, true, false, Locale.US, "", null, null, "GB", null);

		assertContainsError(errors, ErrorType.INPUTERROR, ErrorCode.INVALID_SEAT_NUMBER,
				"Row/Seat contains words that are not allowed", "seat");
	}

	private ListingRequest aListingRequest() {
		ListingRequest request = new ListingRequest();
		request.setPricePerProduct(someMoney());
		request.setFaceValue(someMoney());
		request.setSection("Ducks Section");
		request.setEventId("1234");
		return request;
	}

	private Money money(int amount, String currencyCode) {
		Money price = new Money();
		price.setAmount(new BigDecimal(amount));
		price.setCurrency(currencyCode);
		return price;
	}

	private Money someMoney() {
		return money(100, "GBP");
	}


	@Test
	public void testValidateRowGBListingSplChar() {
		ListingRequest req = new ListingRequest();
		req.setPricePerProduct(money(110, "GBP"));
	    req.setExternalListingId("2STH");
		req.setEventId("1234");
	    List<ListingError> errors = null;
		List<Product> products = new ArrayList<Product>();
		products.add(getProduct(Operation.ADD, "Ótimo", "S1", ProductType.TICKET, null, "ex"));
		products.add(getProduct(Operation.ADD, "R1", "?", ProductType.TICKET, null, "ex"));
		req.setProducts(products);
		errors = validator.validate(req, EVENT_ID, true, false, Locale.US, "", null, null, "GB", null);
		assertTrue(errors.size() > 0);
	}

	@Test
	public void testValidateSectionMaxLengthExceeded() {
		requestListing.setSection("burtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloro");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
	}

	/**
	 * Section name should start with alpha numeric
	 */
	@Test
	public void testValidateSectionStartsWithAlphaNumeric() {
		requestListing.setSection("-section");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
	}

	/**
	 * Section name  - hyphen underscore allowed but not in beginning
	 */
	@Test
	public void testValidateSectionContainsUnderscoreHyphen() {
		requestListing.setSection("sec_tion-");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
		Assert.assertEquals("sec_tion-", requestListing.getSection());
	}


	/**
	 * Section name becomes empty - all invalid chars
	 */
	@Test
	public void testValidateEmptySection() {
		requestListing.setSection("%$#@");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
	}

	/**
	 * Special chars be silently replaced in section
	 */
	@Test
	public void testValidateSectionIgnoreSpecialChars() {
		requestListing.setSection("!section&%^");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
	}

	/**
	 * Test product externalId validation
	 */
	@Test
	public void testValidateProducts_externalId () {
		ListingRequest req = new ListingRequest();
		req.setPricePerProduct(new Money("15"));
		ArrayList<Product> products = new ArrayList<Product>();
		products.add( getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null,
				"1234567890123456789012345678901234567890")); // externalId larger than 32
		req.setProducts(products);
		List<ListingError> errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);

		// test char strip
		products.clear();
		products.add( getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null,
				"<external-id-val&;/>12_23-56")); // externalId larger than 32
		req.setProducts(products);
		errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());
		Assert.assertEquals(products.get(0).getExternalId(), "external-id-val/12_23-56");
	}

	@Test
	public void testValidateProductsUniqueTicketNumber() {
		when(masterStubhubProperties.getProperty("uniqueTicket.Number.validation.UK.enabled", "true")).thenReturn("true");

		ListingRequest req = new ListingRequest();
		req.setPricePerProduct(new Money("15"));
		ArrayList<Product> products = new ArrayList<Product>();
		products.add(getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null,
				"1234567890123456789012345")); // externalId larger than 32
		req.setProducts(products);
		List<ListingError> errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "GB",null);
		Assert.assertEquals(0, errors.size());

	}

	/**
	 * Test product externalId validation
	 */
	@Test
	public void testValidateProducts_rowSectionSeat () {
		ListingRequest req = new ListingRequest();
		req.setPricePerProduct(new Money("15"));
		// test char strip
		ArrayList<Product> products = new ArrayList<Product>();
		products.clear();
		products.add( getProduct(Operation.ADD, "R1", "1234567890123456789012345678901234567890", ProductType.TICKET, null, null));
		products.add( getProduct(Operation.ADD, "1234567890123456789012345678901234567890", "", ProductType.TICKET, null, null));
		products.add( getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null, null));
		req.setProducts(products);
		List<ListingError> errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(2, errors.size());

		products.clear();
		products.add( getProduct(Operation.ADD, "R1", "1", ProductType.fromString("xxxx"), null, null)); // productType default == TICKET
		req.setProducts(products);
		errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(0, errors.size());		// no errors

		products.clear();
		products.add( getProduct(Operation.ADD, "R1", "1,2,3", ProductType.TICKET, null, null)); // productType default == TICKET
		req.setProducts(products);
		errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(1, errors.size());	// invalid ',' in seat

		products.clear();
		products.add( getProduct(null, "R1", "1", ProductType.TICKET, null, null)); // productType default == TICKET
		req.setProducts(products);
		errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(1, errors.size());	// invalid operation null
	}

	@Test
	public void testValidateProducts_row() {
		ListingRequest req = new ListingRequest();
		req.setPricePerProduct(new Money("15"));
		req.setSection("sdfsdf");
		req.setFaceValue(new Money(new BigDecimal(12), "GBP"));

		// test char strip
		ArrayList<Product> products = new ArrayList<Product>();
		products.clear();
		products.add( getProduct(Operation.ADD, "", "1234567890123456", ProductType.TICKET, null, null));
		req.setProducts(products);
		List<ListingError> errors =  validator.validate(req, EVENT_ID, true, false, Locale.US, "", null, null, "GB",null);
		Assert.assertEquals(1, errors.size());

	}

	@Test
	public void testValidateProductsAddPredelivery() {
	  ListingRequest req = new ListingRequest();
	  List<Product> products = new ArrayList<Product>();
	  products.add(getProduct(Operation.UPDATE, "R1", "S1", ProductType.TICKET, "123", null));
	  products.add(getProduct(Operation.ADD, "R1", "S2", ProductType.TICKET, null, null));
	  req.setProducts(products);
	  List<ListingError> errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
	  Assert.assertEquals(1, errors.size());
	}

	@Test
	public void testValidateProductsDuplicateExternalSeatId() {
      ListingRequest req = new ListingRequest();
      List<Product> products = new ArrayList<Product>();
      products.add(getProduct(Operation.ADD, "R1", "S1", ProductType.TICKET, null, "ex"));
      products.add(getProduct(Operation.ADD, "R1", "S2", ProductType.TICKET, null, "ex"));
      req.setProducts(products);
      List<ListingError> errors =  validator.validate(req, EVENT_ID, true, false, Locale.US, "", null, null, "US",null);
      Assert.assertEquals(1, errors.size());
    }

	/**
	 * Test product externalId validation
	 */
	@Test
	public void testValidateTicketTraits () {
		ListingRequest req = new ListingRequest();
		req.setPricePerProduct(new Money("15"));
		req.setQuantity(2);	// required for create

		ArrayList<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait(null, null, Operation.ADD ) );
		req.setTicketTraits (traits);
		List<ListingError> errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(1, errors.size());	// id and name cannot be null

		traits.clear();
		traits.add( getTicketTrait("xxxx1", null, Operation.ADD ) );
		req.setTicketTraits (traits);
		errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(1, errors.size());	// invalid integer id

		traits.clear();
		traits.add( getTicketTrait("102", "parking pass", null ) );
		req.setTicketTraits (traits);
		errors =  validator.validate(req, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals(1, errors.size());	// invalid operation
	}


	@Test
	public void testValidateTeaLeafSessionMaxLengthExceeded() {
		requestListing.setTealeafSessionId("burtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloro");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
	}

	@Test
	public void testValidateExternalListingIdMaxLengthExceeded() {
		requestListing.setExternalListingId("burtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloro");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
	}

	@Test
	public void testValidateExternalListingIdSpecialChars() {
		requestListing.setExternalListingId("I.123");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		Assert.assertEquals("I.123", requestListing.getExternalListingId());
	}

	@Test
	public void testValidateThreatMatrixSessionIdMaxLengthExceeded() {
		requestListing.setThreatMatrixSessionId("burtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloroburtiontrwjotiroloro");
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size()>0);
	}

	@Test
	public void testValidateLRNull() {
		List<ListingError> errors =  validator.validate(null, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testValidatePassQtyProductInUpdate() {
		ArrayList<Product> products = new ArrayList<Product>();
		products.add( getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null, "1234")); // externalId larger than 32
		requestListing.setProducts(products);
		List<ListingError> errors =  validator.validate(requestListing, EVENT_ID, false, false, Locale.US, "", null, null, "US",null);
		assertTrue(errors.size() > 0);
	}

	@Test
	public void testValidateLMSApprovalStatusAndCheckAccess_NullLMSApprStatus() {
		ListingRequest lr = new ListingRequest();
		lr.setPricePerProduct(new Money("15"));
		List<ListingError> res = validator.validateLMSApprovalStatusAndCheckAccess(lr, "some_operator", null);
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void testValidateLMSApprovalStatusAndCheckAccess_RightLMSApprStatus() {
		ListingRequest lr = new ListingRequest();
		lr.setPricePerProduct(new Money("15"));
		lr.setLmsApprovalStatus(LMSApprovalStatus.APPROVED.getId());
		List<ListingError> res = validator.validateLMSApprovalStatusAndCheckAccess(lr, "some_operator", ProxyRoleTypeEnum.Super);
		assertTrue(res.isEmpty());
	}

	@Test
	public void testValidateLMSApprovalStatusAndCheckAccess_WrongLMSApprStatus() {
		ListingRequest lr = new ListingRequest();
		lr.setPricePerProduct(new Money("15"));
		lr.setLmsApprovalStatus(Integer.MAX_VALUE);
		List<ListingError> res = validator.validateLMSApprovalStatusAndCheckAccess(lr, "some_operator", ProxyRoleTypeEnum.Super);
		Assert.assertEquals(1, res.size());
		ListingError le = res.get(0);
		Assert.assertEquals(le.getType(), ErrorType.INPUTERROR);
		Assert.assertEquals(le.getCode(), ErrorCode.INVALID_LMS_STATUS);
	}

	@Test(expectedExceptions = ListingBusinessException.class)
	public void testValidateLMSApprovalStatusAndCheckAccess_WrongRoleApprStatus() {
		ListingRequest lr = new ListingRequest();
		lr.setPricePerProduct(new Money("15"));
		lr.setLmsApprovalStatus(LMSApprovalStatus.APPROVED.getId());
		validator.validateLMSApprovalStatusAndCheckAccess(lr, "some_value", ProxyRoleTypeEnum.Fulfillment);
	}

	@Test(expectedExceptions = ListingBusinessException.class)
	public void testValidateLMSApprovalStatusAndCheckAccess_NullRoleApprStatus() {
		ListingRequest lr = new ListingRequest();
		lr.setPricePerProduct(new Money("15"));
		lr.setLmsApprovalStatus(LMSApprovalStatus.APPROVED.getId());
		validator.validateLMSApprovalStatusAndCheckAccess(lr, "some_operator", null);
	}

	@Test(expectedExceptions = ListingBusinessException.class)
	public void testValidateLMSApprovalStatusAndCheckAccess_NullOperatorApprStatus() {
		ListingRequest lr = new ListingRequest();
		lr.setPricePerProduct(new Money("15"));
		lr.setLmsApprovalStatus(LMSApprovalStatus.APPROVED.getId());
		validator.validateLMSApprovalStatusAndCheckAccess(lr, null, ProxyRoleTypeEnum.Super);
	}

	private static VenueConfiguration mockVenueConfiguration() {
		VenueConfiguration venueConfig = new VenueConfiguration();
		venueConfig.setGeneralAdmissionOnly(false);
		venueConfig.setSeatingZones(Collections.EMPTY_LIST);
		return venueConfig;
	}
}
