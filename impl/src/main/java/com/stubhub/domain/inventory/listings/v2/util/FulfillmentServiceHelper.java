package com.stubhub.domain.inventory.listings.v2.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.common.exception.RecordNotFoundException;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.ListingFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod;
import com.stubhub.domain.inventory.listings.v2.entity.DeliveryType;
import com.stubhub.domain.inventory.listings.v2.entity.ExpectedDeliveryDate;
import com.stubhub.domain.inventory.listings.v2.enums.ConfirmOptionEnum;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryMethodEnum;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryOptionEnum;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryTypeEnum;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;


@Component("fulfillmentServiceHelper")
public class FulfillmentServiceHelper {

	@Autowired
	private FulfillmentServiceAdapter fulfillmentServiceAdapter;

	@Autowired
	private SvcLocator svcLocator;

	private static final Logger log = LoggerFactory.getLogger(FulfillmentServiceHelper.class);

	private static final Integer LMS_PENDING_APPROVAL = 1;
	private static final long FM_BARCODE_PRE_STH = 1L;
	private static final long FM_BARCODE_PRE = 2L;
	private static final long FM_BARCODE = 3L;
	private static final long FM_PDF_PRE = 4L;
	private static final long FM_PDF = 5L;
	private static final long FM_FEDEX = 6L;
	private static final long FM_LMS = 7L;
	private static final long FM_WILLCALL = 8L;
	private static final long FM_LMS_PRE = 9L;
	private static final long FM_UPS = 10L;
	private static final long FM_ROYALMAIL = 11L;
	private static final long FM_SHIPPING = 12L;
	private static final long FM_FLASHSEAT = 13L;
	private static final long FM_FLASHSEAT_NON_INSTANT = 14L;
	private static final long FM_LOCAL_DELIVERY = 17L;
	private static final long FM_MOBILE_TRANSFER = 18L;
	private static final long FM_FLASH_TRANSFER = 19L;
	private static final long FM_MOBILE_INSTANT = 20L;
	private static final long FM_MOBILE_NON_INSTANT = 21L;
	private static final long FM_COURIER = 15L;

	private static final String SPACE = " ";
	private final static String SELLER_ID_HEADER=" sellerId=";
	private final static String APP_NAME_HEADER=" appName=";

	/**
	 * populateFulfillmentOptions
	 * @param listing
	 * @throws RecordNotFoundException
	 */
	public boolean populateFulfillmentOptions(Listing listing) 
	{		
		EventFulfillmentWindowResponse efwResponse = fulfillmentServiceAdapter.getFulfillmentWindowsShape(listing.getEventId(), 
				listing.getSellerContactId());
		List<FulfillmentWindow> fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
		return populateFulfillmentOptions (listing, fulfillmentWindows);
	}	
	
	/**
	 * populate Fulfillment for listing after passing fulfillmentWindows
	 * @param listing
	 * @throws RecordNotFoundException
	 */
	public boolean populateFulfillmentOptions(Listing listing, List<FulfillmentWindow> fulfillmentWindows) {
		
		Calendar saleEndDate = null;
		Calendar predeliveryDate = null;

		// LEGACY: The lmsApproval flag is set to execute the call for LMS approval 
		if ( listing.getIsLmsApproval() == true ) {
			
			// check and change approval status
			lmsApprovalAndFMDMList ( listing, fulfillmentWindows );
		}
		else {
			// If no FulfillmentMethod passed, figure it out 
			if ( listing.getFulfillmentMethod() == null ) {
				List<Long> fulfillmentMethodIds = new ArrayList<Long>();
				if(fulfillmentWindows != null) {
					for (FulfillmentWindow fw : fulfillmentWindows) {
						fulfillmentMethodIds.add(fw.getFulfillmentMethodId());
					}

					if (fulfillmentMethodIds.contains(FM_WILLCALL)) {
						listing.setFulfillmentMethod(FulfillmentMethod.OTHERPREDELIVERY);
						listing.setTicketMedium(TicketMedium.PAPER.getValue());
					}

					if(listing.getListingSource() != null && listing.getListingSource().intValue() == 8) {//STHGen3
						if(fulfillmentMethodIds.contains(FM_BARCODE_PRE_STH)) {
							listing.setFulfillmentMethod(FulfillmentMethod.BARCODEPREDELIVERYSTH);
							listing.setTicketMedium(TicketMedium.BARCODE.getValue());
						}
						else {
							return false;
						}
					}
					
					// figure out the FM and take passed EI in consideration 
					setFMWithElectronicIndicator ( listing, fulfillmentMethodIds );
				}
			}		
			
			// From FulfillmentMethod, figure out FM DM list and saleEndDate 
			FulfillmentMethod selectFulfillmentMethod = listing.getFulfillmentMethod();
			boolean isTrustedSeller=false;
			if (selectFulfillmentMethod != null) {
				String selectedFulfillment = selectFulfillmentMethod.name();
				log.info("Listing values selectFulfillmentMethodName=\"" + selectedFulfillment + "\" deliveryOption=\""+listing.getDeliveryOption()+"\"");
				if (fulfillmentWindows != null) {
					Collections.sort(fulfillmentWindows);
					
					List<FulfillmentMethod> shippingBasedFMs = new ArrayList<>();
					shippingBasedFMs.add(FulfillmentMethod.EVENTCARD);
					shippingBasedFMs.add(FulfillmentMethod.SEASONCARD);
					shippingBasedFMs.add(FulfillmentMethod.RFID);
					shippingBasedFMs.add(FulfillmentMethod.WRISTBAND);
					shippingBasedFMs.add(FulfillmentMethod.GUESTLIST);
					
					for (FulfillmentWindow window : fulfillmentWindows) {
						if(selectFulfillmentMethod == FulfillmentMethod.FLASHSEAT
								|| selectFulfillmentMethod == FulfillmentMethod.MOBILETRANSFER || selectFulfillmentMethod == FulfillmentMethod.MOBILE) {
							break;
						} else if (shippingBasedFMs.contains(selectFulfillmentMethod) || selectFulfillmentMethod == FulfillmentMethod.LOCALDELIVERY) {
						    break;
						} else if (selectFulfillmentMethod == FulfillmentMethod.LMS && (window.getFulfillmentMethodId() == FM_LMS_PRE)) {
							if (predeliveryDate == null) {
								predeliveryDate = window.getEndTime();
							} else {
								if (predeliveryDate.before(window.getEndTime())) {
									predeliveryDate = window.getEndTime();
								}
							}
							if (log.isDebugEnabled()) {
								log.debug("fulfillmentMethod=" + window.getFulfillmentMethodName() + " saleEndDate=" + predeliveryDate);
							}
						} else if ((selectFulfillmentMethod == FulfillmentMethod.OTHERPREDELIVERY && window.getFulfillmentMethodId() == FM_WILLCALL)) {
							if (predeliveryDate == null) {
								predeliveryDate = window.getEndTime();
							} else {
								if (predeliveryDate.before(window.getEndTime())) {
									predeliveryDate = window.getEndTime();
								}
							}
							if (log.isDebugEnabled()) {
								log.debug("fulfillmentMethod=" + window.getFulfillmentMethodName() + " saleEndDate=" + predeliveryDate);
							}
						} else if ((selectFulfillmentMethod == FulfillmentMethod.BARCODE && window.getFulfillmentMethodId() == FM_BARCODE_PRE)
								|| (selectFulfillmentMethod == FulfillmentMethod.BARCODEPREDELIVERYSTH && window.getFulfillmentMethodId() == FM_BARCODE_PRE_STH)) {
							if (predeliveryDate == null) {
								predeliveryDate = window.getEndTime();
							} else {
								if (predeliveryDate.before(window.getEndTime())) {
									predeliveryDate = window.getEndTime();
								}
							}
							listing.setTicketMedium(TicketMedium.BARCODE.getValue());
							if (log.isDebugEnabled()) {
								log.debug("fulfillmentMethod=" + window.getFulfillmentMethodName() + " saleEndDate=" + predeliveryDate);
							}
						} else if (selectFulfillmentMethod == FulfillmentMethod.PDF && window.getFulfillmentMethodId() == FM_PDF_PRE) {
							if (predeliveryDate == null) {
								predeliveryDate = window.getEndTime();
							} else {
								if (predeliveryDate.before(window.getEndTime())) {
									predeliveryDate = window.getEndTime();
								}
							}
							if (log.isDebugEnabled()) {
								log.debug("fulfillmentMethod=" + window.getFulfillmentMethodName() + " saleEndDate=" + predeliveryDate);
							}

						} else if(selectFulfillmentMethod == FulfillmentMethod.UPS && window.getFulfillmentMethodId() == FM_LMS) {
						    if (saleEndDate == null) {
						        saleEndDate = window.getEndTime();
                            } else {
                                if (saleEndDate.before(window.getEndTime())) {
                                    saleEndDate = window.getEndTime();
                                }
                            }
						} else if (window.getFulfillmentMethodName().equalsIgnoreCase(selectedFulfillment) ||
									(FulfillmentMethod.SHIPPING.getName().equalsIgnoreCase(window.getFulfillmentTypeName()) && FulfillmentMethod.SHIPPING.getName().equalsIgnoreCase(selectedFulfillment))) {
							if (saleEndDate == null) {
								saleEndDate = window.getEndTime();
							} else {
								if (saleEndDate.before(window.getEndTime())) {
									saleEndDate = window.getEndTime();
								}
							}
							if (log.isDebugEnabled()) {
								log.debug("fulfillmentMethod=" + window.getFulfillmentMethodName() + " saleEndDate=" + saleEndDate);
							}
						} else {
							continue;
						}
					}
					
					StringBuffer fmDMList = new StringBuffer();
					listing.setConfirmOption(ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
					
					if(selectFulfillmentMethod == FulfillmentMethod.FLASHSEAT) {
						FulfillmentWindow fwindow = getFlashWindow(fulfillmentWindows);
						if(fwindow != null) {
							if(fwindow.getFulfillmentMethodId() == FM_FLASHSEAT) {
								listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
								listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
								listing.setInhandDate(DateUtil.getNowCalUTC());
								predeliveryDate = fwindow.getEndTime();
								
							} else {
								listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
							    saleEndDate = fwindow.getEndTime();
							}
							
							populateFulfillmentDetails(fwindow, fmDMList, listing);
						}
						
					}
					
					if(selectFulfillmentMethod == FulfillmentMethod.MOBILETRANSFER) {
                        FulfillmentWindow window = null;
                        for (FulfillmentWindow fw : fulfillmentWindows) {
                            if(fw.getFulfillmentMethodId() == FM_MOBILE_TRANSFER) {
                                window = fw;
                                break;
                            } 
                        }
                        if(window != null) {
                            listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
                            saleEndDate = window.getEndTime();
                            populateFulfillmentDetails(window, fmDMList, listing);
                        } else {
                            updateTicketMedium(listing, fulfillmentWindows);
                            predeliveryDate = getEndDateForPredeliveryFulfillmentMethods(fulfillmentWindows);
                            saleEndDate = getEndDateForManualFulfillmentMethods(fulfillmentWindows);
                            if(listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()) {
                                evaluatePredeliveryFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
                            } else {
                                evaluateManualFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
                            }
                        }
                    }
					
					if(selectFulfillmentMethod == FulfillmentMethod.MOBILE) {
					    FulfillmentWindow mobileInstant = null;
					    FulfillmentWindow mobileNonInstant = null;
					    for (FulfillmentWindow fw : fulfillmentWindows) {
					        if(fw.getFulfillmentMethodId() == FM_MOBILE_INSTANT) {
					            predeliveryDate = fw.getEndTime();
					            mobileInstant = fw;
					        } else if(fw.getFulfillmentMethodId() == FM_MOBILE_NON_INSTANT) {
					            saleEndDate = fw.getEndTime();
					            mobileNonInstant = fw;
					        }
					    }
					    if(listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()) {
					        if(mobileInstant != null) {
					            populateFulfillmentDetails(mobileInstant, fmDMList, listing);
					        }
					    } else {
                            if(mobileNonInstant != null) {
                                populateFulfillmentDetails(mobileNonInstant, fmDMList, listing);
                            }
                        }
					    if(predeliveryDate == null && saleEndDate == null) {
					        updateTicketMedium(listing, fulfillmentWindows);
					        predeliveryDate = getEndDateForPredeliveryFulfillmentMethods(fulfillmentWindows);
					        saleEndDate = getEndDateForManualFulfillmentMethods(fulfillmentWindows);
					        if(listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()) {
					            evaluatePredeliveryFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
					        } else {
					            evaluateManualFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
					        }
					    }
					}
					
					if(shippingBasedFMs.contains(selectFulfillmentMethod)) {
                      listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
                      List<FulfillmentWindow> windows = new ArrayList<FulfillmentWindow>();
                      for (FulfillmentWindow fw : fulfillmentWindows) {
                        if (fw.getTicketMedium() != null && fw.getTicketMedium().equalsIgnoreCase(selectFulfillmentMethod.name())) {
                          windows.add(fw);
                        }
                      }
                      
                      if(windows.size() == 1 && windows.get(0).getFulfillmentMethodId() == FM_LMS_PRE) {
                        FulfillmentWindow window = windows.get(0);
                        if (predeliveryDate == null) {
                          predeliveryDate = window.getEndTime();
                        } else {
                          if (predeliveryDate.before(window.getEndTime())) {
                              predeliveryDate = window.getEndTime();
                          }
                        }
                        fillFmDMList(fmDMList, window);
                        listing.setTicketMedium(TicketMedium.valueOf(selectFulfillmentMethod.name().toUpperCase()).getValue());
                      } else {
                        for(FulfillmentWindow fw : windows) {
                          if(fw.getFulfillmentMethodId() == FM_LMS_PRE) {
                            if(listing.isLmsExtensionRequired()) {
                              fillFmDMList(fmDMList, fw);
                            }
                            continue;
                          }
                          
                          if (saleEndDate == null) {
                            saleEndDate = fw.getEndTime();
                          } else {
                            if (saleEndDate.before(fw.getEndTime())) {
                              saleEndDate = fw.getEndTime();
                            }
                          }
                          
                          fillFmDMList(fmDMList, fw);
                        }
                        
                        if(saleEndDate != null) {
                          listing.setTicketMedium(TicketMedium.valueOf(selectFulfillmentMethod.name().toUpperCase()).getValue());
                        } else {
                          saleEndDate = evaluatePaperFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
                          listing.setTicketMedium(TicketMedium.PAPER.getValue());
                        }
                      }
                      
                    }
					
					if(selectFulfillmentMethod == FulfillmentMethod.LOCALDELIVERY) {
					    listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
						FulfillmentWindow window = null;
						for (FulfillmentWindow fw : fulfillmentWindows) {
						    if(fw.getFulfillmentMethodId() == FM_LOCAL_DELIVERY && TicketMedium.PAPER.name().equalsIgnoreCase(fw.getTicketMedium())) {
                                window = fw;
                                break;
                            }
                        }
						if(window != null) {
							saleEndDate = window.getEndTime();
							populateFulfillmentDetails(window, fmDMList, listing);
						} else {
                            saleEndDate = evaluatePaperFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
                            listing.setTicketMedium(TicketMedium.PAPER.getValue());
                        }
						
					}
					
					for (FulfillmentWindow window : fulfillmentWindows) {
						if (selectFulfillmentMethod == FulfillmentMethod.FLASHSEAT
								|| selectFulfillmentMethod == FulfillmentMethod.MOBILETRANSFER || selectFulfillmentMethod == FulfillmentMethod.MOBILE) {
							break;
						}
						else if (shippingBasedFMs.contains(selectFulfillmentMethod) || selectFulfillmentMethod == FulfillmentMethod.LOCALDELIVERY) {
						    break;
						}
						else if (selectFulfillmentMethod == FulfillmentMethod.BARCODE
						        || listing.getTicketMedium() == TicketMedium.BARCODE.getValue()) {
							if ((listing.getDeliveryOption() == null || listing.getDeliveryOption() == DeliveryOptionEnum.MANUAL_DELIVERY.getDeliveryOption())
									&& window.getFulfillmentMethodId() == FM_BARCODE) { // e-delivery
								fillFmDMList(fmDMList, window);
								
							}
							else if (listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()
									&& window.getFulfillmentMethodId() == FM_BARCODE_PRE) { // pre-delivery
								fillFmDMList(fmDMList, window);
								listing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
							}
						} 
						else if (selectFulfillmentMethod == FulfillmentMethod.PDF) {
							if ((listing.getDeliveryOption() == null || listing.getDeliveryOption() == DeliveryOptionEnum.MANUAL_DELIVERY.getDeliveryOption())
									&& window.getFulfillmentMethodId() == FM_PDF) { // e-delivery
								fillFmDMList(fmDMList, window);
							}
							else if (listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()
									&& window.getFulfillmentMethodId() == FM_PDF_PRE) { // pre-delivery
								fillFmDMList(fmDMList, window);
								listing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
								
							}
						}
						else if (selectFulfillmentMethod == FulfillmentMethod.LMS) {
							if(window.getFulfillmentMethodId() == FM_LMS_PRE || window.getFulfillmentMethodId() == FM_LMS){
								if(window.getFulfillmentMethodId() == FM_LMS){
									isTrustedSeller=true;
								}
								fillFmDMList(fmDMList, window);
							}
						} else if (selectFulfillmentMethod == FulfillmentMethod.OTHERPREDELIVERY) {
							fillFmDMList(fmDMList, window);
						} else if (selectFulfillmentMethod == FulfillmentMethod.BARCODEPREDELIVERYSTH) {
							if (window.getFulfillmentMethodId() == FM_BARCODE_PRE_STH) { // pre-delivery
								fillFmDMList(fmDMList, window);
								listing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
							}
						

						}
						else if (selectFulfillmentMethod.name().equals(window.getFulfillmentMethodName()) ||
									(FulfillmentMethod.SHIPPING.getName().equalsIgnoreCase(window.getFulfillmentTypeName()) && FulfillmentMethod.SHIPPING.getName().equalsIgnoreCase(selectFulfillmentMethod.name()))) {
							fillFmDMList(fmDMList, window);
						}

						if (listing.getTicketMedium()!=null && listing.getTicketMedium().intValue() == TicketMedium.PAPER
								.getValue()
								&& window.getFulfillmentMethodId().longValue() == FM_LMS_PRE
								&& listing.isLmsExtensionRequired()) {
							fillFmDMList(fmDMList, window);
							listing.setLmsApprovalStatus(LMS_PENDING_APPROVAL);
							
						}

						if (selectFulfillmentMethod != FulfillmentMethod.LMS && listing.getTicketMedium()!=null && listing.getTicketMedium().intValue() == TicketMedium.PAPER
								.getValue()
								&& window.getFulfillmentMethodId().longValue() == FM_LMS) {
							fillFmDMList(fmDMList, window);
						}

					}
					
					if(barcodeFallback(selectFulfillmentMethod)) {
                        if(predeliveryDate == null && saleEndDate == null) {
                            updateTicketMedium(listing, fulfillmentWindows);
                            predeliveryDate = getEndDateForPredeliveryFulfillmentMethods(fulfillmentWindows);
                            saleEndDate = getEndDateForManualFulfillmentMethods(fulfillmentWindows);
                            if(listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()) {
                                evaluatePredeliveryFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
                            } else {
                                evaluateManualFulfillmentMethods(listing, fulfillmentWindows, fmDMList);
                            }
                        }
                    }
					
					if (fmDMList.length() > 0 && fmDMList.charAt(fmDMList.length() - 1) == '|') {
						listing.setFulfillmentDeliveryMethods(fmDMList.substring(0, fmDMList.length() - 1));
					} else {
						listing.setFulfillmentDeliveryMethods(fmDMList.toString());
					}
					
				}
				if(predeliveryDate != null) {
					listing.setPredeliveryAvailable(true);
				}
				
				listing.setLmsApprovalStatus(null); // reset this flag if the delivery option changed from LMS to other method
				if (saleEndDate == null && predeliveryDate != null) { // LMS PreDelivery
					saleEndDate = predeliveryDate;
					if (!isWillCallOrFlashPredelivery(selectFulfillmentMethod, fulfillmentWindows)) {
						listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
						log.info("set listing={} to incomplete as no saleEndDate and has predeliveryDate and not willcall or flashPredelivery", listing.getId());
					}
				}
				boolean isLmsPredelivery = false;
				if(listing.getFulfillmentDeliveryMethods().contains("|9,") || listing.getFulfillmentDeliveryMethods().startsWith("9,")) {
					isLmsPredelivery = true;
				}
				if (selectedFulfillment.equalsIgnoreCase(FulfillmentMethod.LMS.getName()) ||
				        (listing.isLmsExtensionRequired() && isLmsPredelivery)) {
				    
					if(isTrustedSeller) {
						listing.setLmsApprovalStatus(null);
					}
					else {
						listing.setLmsApprovalStatus(LMS_PENDING_APPROVAL);
					}
				}
				if (predeliveryDate != null && listing.getDeliveryOption() != null && listing.getDeliveryOption() == 1l) {
					saleEndDate = predeliveryDate;
				}
			}
			if (saleEndDate == null) {
				ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, ErrorEnum.DELIVERY_OPTION_NOT_SUPPORTED.getMessage(), "deliveryOption");
				throw new ListingBusinessException(listingError);
			}
			if (listing.getEndDate() != null) {
				Calendar eventCalendar = Calendar.getInstance(listing.getEvent().getJdkTimeZone());
				eventCalendar.set(Calendar.YEAR, listing.getEndDate().get(Calendar.YEAR));
				eventCalendar.set(Calendar.MONTH, listing.getEndDate().get(Calendar.MONTH));
				eventCalendar.set(Calendar.DAY_OF_MONTH, listing.getEndDate().get(Calendar.DAY_OF_MONTH));
				eventCalendar.set(Calendar.HOUR_OF_DAY, listing.getEndDate().get(Calendar.HOUR_OF_DAY));
				eventCalendar.set(Calendar.MINUTE, listing.getEndDate().get(Calendar.MINUTE));
				eventCalendar.set(Calendar.SECOND, listing.getEndDate().get(Calendar.SECOND));
				eventCalendar.set(Calendar.MILLISECOND, listing.getEndDate().get(Calendar.MILLISECOND));
				Calendar calendar = DateUtil.convertCalendarToUtc(eventCalendar);
				listing.setEndDate(calendar);

				if(listing.getEndDate().before(DateUtil.getNowCalUTC())) {
					ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_SALE_ENDATE, "Invalid Sale End Date", "saleEndDate");
					throw new ListingBusinessException(listingError);
				}

				if (saleEndDate.before(listing.getEndDate())) {
					// EXTSELL-2 the sale end date sent by the seller at the time of listing creation is after the fulfillment window, adjust the sale end date and accept the listing
					StringBuilder logString = new StringBuilder();
					logString.append("Adjusting listing request end date with FF sale end date");
					logString.append(getContextFromHeader()).append(SPACE);
					logString.append("FFEndDate=").append((saleEndDate.get(Calendar.MONTH)+1)+ "/" + saleEndDate.get(Calendar.DAY_OF_MONTH) + "/"+ saleEndDate.get(Calendar.YEAR)).append(SPACE);
					logString.append("listingEndDate=").append((listing.getEndDate().get(Calendar.MONTH)+1)+ "/" + listing.getEndDate().get(Calendar.DAY_OF_MONTH) + "/"+ listing.getEndDate().get(Calendar.YEAR)).append(SPACE);
					logString.append("Adjusted endDate=").append(new SimpleDateFormat("MM/dd/yyyy").format(listing.getEndDate().getTime())).append(SPACE);
					logString.append("externalListingId=").append(listing.getExternalId()).append(SPACE);
					logString.append("eventId=").append(listing.getEventId()).append(SPACE);
					logString.append("eventDate=").append((listing.getEvent().getEventDate().get(Calendar.MONTH)+1)+ "/" +listing.getEvent().getEventDate().get(Calendar.DAY_OF_MONTH) + "/"+ listing.getEvent().getEventDate().get(Calendar.YEAR)).append(SPACE);
					logString.append("quantity=").append(listing.getQuantity()).append(SPACE);
					logString.append("deliveryOption="+ listing.getDeliveryOption()).append(SPACE);
					if(listing.getListPrice()!=null){
						logString.append("listPrice=").append(listing.getListPrice().getAmount()).append(SPACE).append("currency=").append(listing.getListPrice().getCurrency()).append(SPACE);
					}
					log.info(logString.toString());
					listing.setEndDate(saleEndDate);
					listing.setSaleEndDateAdjusted(true);
				}
				if (!hasTimeComponent(listing.getEndDate()) && listing.getEvent() != null) {
					if(hasSameDate(listing.getEndDate(), listing.getEvent().getEventDate())) {
						listing.setEndDate(saleEndDate);
					} else {
						listing.getEndDate().set(Calendar.HOUR_OF_DAY, 23);
						listing.getEndDate().set(Calendar.MINUTE, 59);
						listing.getEndDate().set(Calendar.SECOND, 59);
					}
				}
			} else {
				listing.setEndDate(saleEndDate);
			}
		}
		//(LDD-430) Listings Fulfillment Extension issues
		//listing.setSaleEndDateIndicator(Boolean.TRUE);
		
		return true;
	}
	
	public String calculateFmDmList(Listing listing, List<FulfillmentWindow> fulfillmentWindows) {
      StringBuffer fmDMList = new StringBuffer();
      FulfillmentMethod selectFulfillmentMethod = listing.getFulfillmentMethod();
      if (fulfillmentWindows != null && selectFulfillmentMethod != null) {
          for (FulfillmentWindow window : fulfillmentWindows) {
              if (selectFulfillmentMethod == FulfillmentMethod.PDF && window.getFulfillmentMethodId() == FM_PDF_PRE) {
                  fillFmDMList(fmDMList, window);
              } else if ((selectFulfillmentMethod == FulfillmentMethod.MOBILE || selectFulfillmentMethod == FulfillmentMethod.MOBILETRANSFER) && window.getFulfillmentMethodId() == FM_MOBILE_INSTANT) {
                  fillFmDMList(fmDMList, window);
              }
          }
      }

      if (fmDMList.length() > 0 && fmDMList.charAt(fmDMList.length() - 1) == '|') {
          return (fmDMList.substring(0, fmDMList.length() - 1));
      } else {
          return fmDMList.toString();
      }
    }
	
	private FulfillmentWindow getFlashWindow(List<FulfillmentWindow> fulfillmentWindows) {
		FulfillmentWindow fwindow = null;
		for (FulfillmentWindow fw : fulfillmentWindows) {
			if(fw.getFulfillmentMethodId() == FM_FLASHSEAT_NON_INSTANT) {
				fwindow = fw;
				break;
			}
		}
		if(fwindow == null) {
			for (FulfillmentWindow fw : fulfillmentWindows) {
				if(fw.getFulfillmentMethodId() == FM_FLASHSEAT) {
					//this case results in PENDING LOCK listing
					fwindow = fw;
					break;
				}
			}
		}
		if(fwindow == null) {
			for (FulfillmentWindow fw : fulfillmentWindows) {
				if(fw.getFulfillmentMethodId() == FM_FLASH_TRANSFER) {
					fwindow = fw;
					break;
				}
			}
		}
		return fwindow;
	}
	
	private Calendar evaluatePaperFulfillmentMethods(Listing listing, List<FulfillmentWindow> fulfillmentWindows, StringBuffer fmDmList) {
	    Calendar saleEndDate = null;
	    for(FulfillmentWindow fw : fulfillmentWindows) {
	        if((TicketMedium.PAPER.name().equalsIgnoreCase(fw.getTicketMedium())) && (FulfillmentMethod.UPS.getName().equalsIgnoreCase(fw.getFulfillmentTypeName())
	                || FulfillmentMethod.SHIPPING.getName().equalsIgnoreCase(fw.getFulfillmentTypeName()) || fw.getFulfillmentMethodId() == FM_LMS)) {
	            if (saleEndDate == null) {
	                saleEndDate = fw.getEndTime();
	            } else {
	                if (saleEndDate.before(fw.getEndTime())) {
	                    saleEndDate = fw.getEndTime();
	                }
	            }
	            fillFmDMList(fmDmList, fw);
	        }
	    }
	    return saleEndDate;
	}
	
	private Calendar getEndDateForManualFulfillmentMethods(List<FulfillmentWindow> fulfillmentWindows) {
	  Calendar saleEndDate = null;
      for(FulfillmentWindow fw : fulfillmentWindows) {
        if(fw.getFulfillmentMethodId() == FM_BARCODE) {
          if (saleEndDate == null) {
              saleEndDate = fw.getEndTime();
          } else {
              if (saleEndDate.before(fw.getEndTime())) {
                  saleEndDate = fw.getEndTime();
              }
          }
        }
      }
      return saleEndDate;
	}
	
	private Calendar getEndDateForPredeliveryFulfillmentMethods(List<FulfillmentWindow> fulfillmentWindows) {
      Calendar predeliveryDate = null;
      for(FulfillmentWindow fw : fulfillmentWindows) {
        if(fw.getFulfillmentMethodId() == FM_BARCODE_PRE) {
          if (predeliveryDate == null) {
              predeliveryDate = fw.getEndTime();
          } else {
              if (predeliveryDate.before(fw.getEndTime())) {
                  predeliveryDate = fw.getEndTime();
              }
          }
        }
      }
      return predeliveryDate;
    }
	
	private void evaluateManualFulfillmentMethods(Listing listing, List<FulfillmentWindow> fulfillmentWindows, StringBuffer fmDmList) {
	    for(FulfillmentWindow fw : fulfillmentWindows) {
	        if(fw.getFulfillmentMethodId() == FM_BARCODE) {
	            fillFmDMList(fmDmList, fw);
	        }
	    }
	}
	
	private void evaluatePredeliveryFulfillmentMethods(Listing listing, List<FulfillmentWindow> fulfillmentWindows, StringBuffer fmDmList) {
	    for(FulfillmentWindow fw : fulfillmentWindows) {
	        if(fw.getFulfillmentMethodId() == FM_BARCODE_PRE) {
	            fillFmDMList(fmDmList, fw);
	            listing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
	        }
	    }
	}
	
	private void populateFulfillmentDetails(FulfillmentWindow window, StringBuffer fmDmList, Listing listing) {
		fillFmDMList(fmDmList, window);
		String ticketMedium = window.getTicketMedium();
		if(ticketMedium != null) {
			listing.setTicketMedium(TicketMedium.valueOf(ticketMedium.toUpperCase()).getValue());
		}
		if(listing.getDeliveryOption() != null && listing.getDeliveryOption() == DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()) {
		    listing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
		}
	}
	
	private void updateTicketMedium(Listing listing, List<FulfillmentWindow> fulfillmentWindows) {
	    List<Long> fmIDs = new ArrayList<Long>();
	    fmIDs.add(FM_BARCODE);
	    fmIDs.add(FM_BARCODE_PRE);
	    for(FulfillmentWindow fw : fulfillmentWindows) {
	        if(fmIDs.contains(fw.getFulfillmentMethodId())) {
	            String ticketMedium = fw.getTicketMedium();
	            if(ticketMedium != null) {
	                listing.setTicketMedium(TicketMedium.valueOf(ticketMedium.toUpperCase()).getValue());
	            }
	            break;
	        }
	    }
	}

	private boolean isWillCallOrFlashPredelivery(FulfillmentMethod selectFulfillmentMethod, List<FulfillmentWindow> fulfillmentWindows) {
		List<Long> fulfillmentMethodIds = new ArrayList<Long>();
		for(FulfillmentWindow fulfillmentWindow : fulfillmentWindows){
			fulfillmentMethodIds.add(fulfillmentWindow.getFulfillmentMethodId());
		}
		
		boolean isWillCall = fulfillmentMethodIds.contains(FM_WILLCALL) && selectFulfillmentMethod == FulfillmentMethod.OTHERPREDELIVERY;
		boolean isFlashPredelivery = fulfillmentMethodIds.contains(FM_FLASHSEAT) && selectFulfillmentMethod == FulfillmentMethod.FLASHSEAT;
		
		return (isWillCall || isFlashPredelivery);
	}

	private boolean isLmsExtensionRequired(Listing listing) {
		return listing.getTicketMedium()!=null && listing.getTicketMedium().intValue() == TicketMedium.PAPER.getValue() && listing.isLmsExtensionRequired();
	}
	
	private void fillFmDMList(StringBuffer fmDMList, FulfillmentWindow window) {
		fmDMList.append(window.getFulfillmentMethodId());
		fmDMList.append(",");
		fmDMList.append(window.getDeliveryMethodId());
		fmDMList.append(",");
		fmDMList.append(window.getBaseCost());
		fmDMList.append(",");
		fmDMList.append(",");
		fmDMList.append(getDateFormat().format(window.getEndTime().getTime()));
		fmDMList.append("|");
	}
	
	/**
	 * Figure out the FM and take passed EI in consideration 
	 * @param listing
	 * @param fmMethodIds
	 */
	private void setFMWithElectronicIndicator ( Listing listing, List<Long> fmMethodIds  ) 
	{
		// try distinguish between isElectronic notPassed (2), passedTrue (1), passedFalse (0)
		int isElectronic = 2;
		if ( listing.getIsElectronicDelivery()!=null && listing.getIsElectronicDelivery()==true ) {
			isElectronic = 1;
		}
		else if ( listing.getIsElectronicDelivery()!=null && listing.getIsElectronicDelivery()==false ) {
			isElectronic = 0;
		}

		log.info("setFMWithElectronicIndicator fmMethodIds={}", fmMethodIds);
		
		boolean isPdfFM     = fmMethodIds.contains(FM_PDF) || fmMethodIds.contains(FM_PDF_PRE);
		boolean isUPSFM     = fmMethodIds.contains(FM_UPS);
		boolean isShipping  = fmMethodIds.contains(FM_SHIPPING) || fmMethodIds.contains(FM_ROYALMAIL) || fmMethodIds.contains(FM_COURIER);
		boolean isLmsFM     = fmMethodIds.contains(FM_LMS) || fmMethodIds.contains(FM_LMS_PRE);
		boolean isBarcodeFM = fmMethodIds.contains(FM_BARCODE) || fmMethodIds.contains(FM_BARCODE_PRE);
		boolean isMobileTransferFM = fmMethodIds.contains(FM_MOBILE_TRANSFER) ;

		// Electronic Delivery true
		if ( isElectronic==1 ||  isElectronic==2 ){
		
			if (isBarcodeFM) {
				listing.setFulfillmentMethod(FulfillmentMethod.BARCODE);
				listing.setTicketMedium(TicketMedium.BARCODE.getValue());
			} 
			else if (isPdfFM) {	
				listing.setFulfillmentMethod(FulfillmentMethod.PDF);
				listing.setTicketMedium(TicketMedium.PDF.getValue());
			}			
			else if ( isUPSFM ) {
				listing.setFulfillmentMethod(FulfillmentMethod.UPS);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}
			else if ( isShipping ) {
				listing.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}
			else if ( isLmsFM ) {
				listing.setFulfillmentMethod(FulfillmentMethod.LMS);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}
			else if ( isMobileTransferFM ) {
				listing.setFulfillmentMethod(FulfillmentMethod.MOBILETRANSFER);
				listing.setTicketMedium(TicketMedium.EXTMOBILE.getValue());
			}
			else{
				log.info("No Fulfilment method is provided for the listingId= " + listing.getId());
			}
		}

		//Electronic Delivery false
		if ( isElectronic==0  ) {
			
			if ( isUPSFM ) {
				listing.setFulfillmentMethod(FulfillmentMethod.UPS);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}else if ( isShipping ) {
				listing.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			} 
			else if ( isLmsFM ) {
				listing.setFulfillmentMethod(FulfillmentMethod.LMS);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}
			//listing is created with electronicdelivery flag is false and fulfilment artifact is null for barcode scenario.
			else if (isBarcodeFM) {
				listing.setFulfillmentMethod(FulfillmentMethod.BARCODE);
				listing.setTicketMedium(TicketMedium.BARCODE.getValue());
			}
			
			else if(isPdfFM ) {
				
				ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, ErrorEnum.DELIVERY_OPTION_NOT_SUPPORTED.getMessage(), "");
				throw new ListingBusinessException(listingError);
				
			}
			else{
				log.info("No Fulfilment method is provided for the listingId= " + listing.getId());
			}
		}
	}
	
	/**
	 * Based on LMS approval set the FMDM list 
	 * @param listing
	 * @param fulfillmentWindows
	 */
	private void lmsApprovalAndFMDMList ( Listing listing, List<FulfillmentWindow> fulfillmentWindows) 
	{
		String fmDmList = listing.getFulfillmentDeliveryMethods();
		if ( listing.getLmsApprovalStatus() == 2) {
			StringBuffer fmDm = new StringBuffer();
			listing.setSystemStatus(ListingStatus.ACTIVE.name());
			if(fulfillmentWindows != null) {
				for (FulfillmentWindow window : fulfillmentWindows) {
					if(window.getFulfillmentMethodId() == FM_LMS_PRE) {
						listing.setEndDate(window.getEndTime());
						fmDm.append(window.getFulfillmentMethodId());
						fmDm.append(",");
						fmDm.append(window.getDeliveryMethodId());
						fmDm.append(",");
						fmDm.append(window.getBaseCost());
						fmDm.append(",");
						fmDm.append(",");
						fmDm.append(getDateFormat().format(window.getEndTime().getTime()));
						break;
					}
				}
				if(StringUtils.trimToNull(fmDmList) == null) {
					fmDmList = fmDm.toString();
				} else if(!(fmDmList.startsWith("9,") || fmDmList.contains("|9,"))) {
					fmDmList = fmDmList.concat("|").concat(fmDm.toString());
				}
			} else {
				ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, ErrorEnum.DELIVERY_OPTION_NOT_SUPPORTED.getMessage(), "");
				throw new ListingBusinessException(listingError);
			}
		} 
		else if(listing.getLmsApprovalStatus() == 4) {
			if(StringUtils.trimToNull(fmDmList) != null) {
				int start = -1;
				if(fmDmList.startsWith("9,")) {
					start = fmDmList.indexOf("9,");
				}
				if(start == -1) {
					start = fmDmList.indexOf("|9,");
				}
				if(start != -1) {
					int end = fmDmList.indexOf("|", start+1);
					if(end == -1) {
						fmDmList = fmDmList.substring(0, start);
					} else {
						fmDmList = fmDmList.substring(0, start).concat(fmDmList.substring(end));
					}
					if(fmDmList.startsWith("|")) {
						fmDmList = fmDmList.substring(1);
					}
				}
			}

			Calendar endDate = DateUtil.getNowCalUTC();
			if(fulfillmentWindows != null && StringUtils.trimToNull(fmDmList) != null) {
				for(FulfillmentWindow window : fulfillmentWindows) {
					if(window.getFulfillmentMethodId() == FM_UPS) {
						if(window.getEndTime().after(endDate)) {
							endDate = window.getEndTime();
						}
					}
				}
			}
			listing.setEndDate(endDate);

		}
		listing.setFulfillmentDeliveryMethods(fmDmList);
	}

	private static SimpleDateFormat getDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		sdf.setLenient(false);
		return sdf;
	}

	private boolean hasTimeComponent(Calendar calendar) {
		if (calendar.get(Calendar.HOUR_OF_DAY) == 0
				&& calendar.get(Calendar.MINUTE) == 0
				&& calendar.get(Calendar.SECOND) == 0) {
			return false;
		}
		return true;
	}

	private boolean hasSameDate(Calendar calendar1, Calendar calendar2) {
		if ((calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
				&& calendar1.get(Calendar.MONTH) == calendar2
						.get(Calendar.MONTH) && calendar1
				.get(Calendar.DAY_OF_MONTH) == calendar2
				.get(Calendar.DAY_OF_MONTH)))
			return true;
		return false;
	}

	/**
	 * Returns the saleEndDate for the corresponding fulfillment Window
	 *
	 * @param listing
	 * @return Calendar
	 */
	public Calendar calculateSaleEndDate(Listing listing, List<FulfillmentWindow> fulfillmentWindows) {
		Calendar saleEndDate = null;
		Calendar predeliveryDate = null;
		// determine if the configured delivery option is available
		if(fulfillmentWindows == null) {
		  EventFulfillmentWindowResponse efwResponse = fulfillmentServiceAdapter.getFulfillmentWindowsShape(listing.getEventId(), 
              listing.getSellerContactId());
          fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
		}
		
		if (listing.getFulfillmentMethod() != null) {
			String selectedFulfillment = listing.getFulfillmentMethod().getName();
			//SELLAPI-1300 09/18/15 START
			//when a trusted seller is in the process of creating a paper ticket listing,
			//in first few steps INCOMPLETE, UPS listing will be created
			//in the last screen when seller attempts to change sale end date to an
			//earlier LMS end date, available end date should be later than UPS end date
			//so unless UPS is changed to LMS, exact end date cannot be determined
			String fmDmList = null;
			if (listing.getFulfillmentDeliveryMethods() != null) {
				fmDmList = listing.getFulfillmentDeliveryMethods();
				if (fmDmList.startsWith("7,") || fmDmList.contains("|7,")) {
					if(selectedFulfillment.contains(FulfillmentMethod.UPS.getName())) {
						selectedFulfillment = FulfillmentMethod.LMS.getName();
					}
				}
			}
			//SELLAPI-1300 09/18/15 END
			if (fulfillmentWindows != null) {
				Collections.sort(fulfillmentWindows);
				for (FulfillmentWindow window : fulfillmentWindows) {
					if (listing.getFulfillmentMethod() == FulfillmentMethod.LMS
							&& (window.getFulfillmentMethodId() == FM_LMS_PRE)) {
						if (predeliveryDate == null) {
							predeliveryDate = window.getEndTime();
						} else {
							if (predeliveryDate.before(window.getEndTime())) {
								predeliveryDate = window.getEndTime();
							}
						}
						log.info("listingId= " + listing.getId() + " fulfillmentMethod="
								+ window.getFulfillmentMethodName()
								+ " saleEndDate=" + predeliveryDate);
					} else if (listing.getFulfillmentMethod() == FulfillmentMethod.BARCODE
							&& (window.getFulfillmentMethodId() == FM_BARCODE_PRE)) {
						if (predeliveryDate == null) {
							predeliveryDate = window.getEndTime();
						} else {
							if (predeliveryDate.before(window.getEndTime())) {
								predeliveryDate = window.getEndTime();
							}
						}
						log.info("listingId= " + listing.getId() + " fulfillmentMethod="
								+ window.getFulfillmentMethodName()
								+ " saleEndDate=" + predeliveryDate);
					} else if (listing.getFulfillmentMethod() == FulfillmentMethod.PDF
							&& (window.getFulfillmentMethodId() == FM_PDF_PRE)) {
						if (predeliveryDate == null) {
							predeliveryDate = window.getEndTime();
						} else {
							if (predeliveryDate.before(window.getEndTime())) {
								predeliveryDate = window.getEndTime();
							}
						}
						log.info("listingId= " + listing.getId() + " fulfillmentMethod="
								+ window.getFulfillmentMethodName()
								+ " saleEndDate=" + predeliveryDate);
					} else if ((listing.getFulfillmentMethod() == FulfillmentMethod.MOBILE
					              || listing.getFulfillmentMethod() == FulfillmentMethod.MOBILETRANSFER)
                            && (window.getFulfillmentMethodId() == FM_MOBILE_INSTANT)) {
					    if (predeliveryDate == null) {
                            predeliveryDate = window.getEndTime();
                        } else {
                            if (predeliveryDate.before(window.getEndTime())) {
                                predeliveryDate = window.getEndTime();
                            }
                        }
                        log.info("listingId= " + listing.getId() + " fulfillmentMethod="
                                + window.getFulfillmentMethodName()
                                + " saleEndDate=" + predeliveryDate);
					} else if (window.getFulfillmentMethodName().equals(
							selectedFulfillment)) {
						if (saleEndDate == null) {
							saleEndDate = window.getEndTime();
						} else {
							if (saleEndDate.before(window.getEndTime())) {
								saleEndDate = window.getEndTime();
							}
						}
						log.info("listingId= " + listing.getId() + " fulfillmentMethod="
								+ window.getFulfillmentMethodName()
								+ " saleEndDate=" + saleEndDate);
					} else {
						continue;
					}
				}
			}
			if (saleEndDate == null && predeliveryDate != null) { // LMS PreDelivery
				saleEndDate = predeliveryDate;
			}
			if (predeliveryDate != null
					&& listing.getDeliveryOption() == 1l) {
				saleEndDate = predeliveryDate;
			}
		}
		return saleEndDate;
	}
	
	public List<DeliveryMethod> getDeliveryMethodsForListingId(Long listingId,
			Long buyerContactId, Calendar inHandDate, boolean inHand,
			Event eventInfo) {
		List<DeliveryMethod> deliveryMethodsList = new ArrayList<DeliveryMethod>();
		
		Calendar localInHandDate = inHandDate;
		if (localInHandDate == null) {
			localInHandDate = Calendar.getInstance();
		}
		
		ListingFulfillmentWindowResponse lfwResponse = fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(listingId, buyerContactId);
		    
		if (lfwResponse != null && lfwResponse.getFulfillmentWindows() != null) {
		    FulfillmentWindowResponse[] fwResponse = lfwResponse.getFulfillmentWindows().toArray(new FulfillmentWindowResponse[0]);
		    if(fwResponse != null && fwResponse.length > 0) {
		        deliveryMethodsList = getDeliveryMethodsFromFulfillmentWindows(
                fwResponse, listingId, localInHandDate,
                eventInfo.getEventDate(), inHand, eventInfo.getJdkTimeZone());
		    }
		} else {
		    log.info("message=\"No fulfillment windows returned\" listingId=" + listingId);
		    return deliveryMethodsList;
		}
		return deliveryMethodsList;
	}
	
    private List<DeliveryMethod> getDeliveryMethodsFromFulfillmentWindows(FulfillmentWindowResponse[] windows, Long listingId,
        Calendar inHandDate, Calendar eventDate, boolean inHand, TimeZone venueTimeZone) {
        List<DeliveryMethod> deliveryMethodList = new ArrayList<DeliveryMethod>();
        for (FulfillmentWindowResponse window : windows) {
            DeliveryMethod dm = new DeliveryMethod();
            dm.setName(window.getDeliveryMethodDisplayName());
            dm.setId(window.getDeliveryMethod().getId());
            dm.setDeliveryMethodEnum(DeliveryMethodEnum.getDeliveryMethodEnumByName(window.getDeliveryMethod().getName()));
            
            DeliveryType deliveryType = new DeliveryType();
            deliveryType.setId(window.getDeliveryMethod().getDeliveryTypeId());
            deliveryType.setName(window.getDeliveryMethod().getDeliveryTypeName());
            deliveryType.setDeliveryTypeEnum(DeliveryTypeEnum.getDeliveryTypeEnumByName(deliveryType.getName()));
            dm.setDeliveryType(deliveryType);
            
            String venueTimeZoneStr = null;
            if (venueTimeZone != null) {
                venueTimeZoneStr = venueTimeZone.getID();
            }
            dm.setExpectedDeliveryDate(getExpectedDeliveryDate(
                dm, inHandDate, null, eventDate, inHand, venueTimeZoneStr));
            
            deliveryMethodList.add(dm);
        }
        
        return deliveryMethodList;
    }
	
	private ExpectedDeliveryDate getExpectedDeliveryDate(
			DeliveryMethod deliveryMethod, Calendar inHandDate,
			Calendar transactionDate, Calendar eventDate, boolean inHand,
			String venueTimeZone) {
		ExpectedDeliveryDate expectedDeliveryDate = new ExpectedDeliveryDate();
		Calendar expectedDate = null;
		Calendar sysdate = getNowCalUTC();
		if (venueTimeZone != null) {
			try {
				Calendar newCal = DateUtil.convertCalendarToNewTimeZone(
						sysdate, TimeZone.getTimeZone(venueTimeZone));
				if (newCal != null) {
					sysdate = newCal;
				}
			} catch (Exception e) {
				log.error(
						"message=\"Error occured in converting calendar instance to Timezone\"",
						e);
			}
		}
		Calendar inHandDateCopy = new GregorianCalendar();
		if (inHandDate != null) {
			inHandDateCopy = (Calendar) inHandDate.clone();
		}
		if (inHandDate != null && inHandDate.after(sysdate)) {
			expectedDate = (Calendar) sysdate.clone();
			expectedDate.setTimeInMillis(inHandDateCopy.getTimeInMillis());
		} else {// case where is no inhanddate or if the inHandDate is in the
				// past and there is no transactionDate.
			expectedDate = (Calendar) sysdate.clone();
			inHandDateCopy = (Calendar) sysdate.clone();
		}
		// TODO move string out to resource bundle
		if (deliveryMethod.getDeliveryMethodEnum() == DeliveryMethodEnum.PickupEventDay
				|| deliveryMethod.getDeliveryMethodEnum() == DeliveryMethodEnum.WillCall) {
			expectedDeliveryDate.setExpectedDeliveryDesc("Day of the event");
			if (eventDate != null) {
				expectedDate = (Calendar) sysdate.clone();
				expectedDate.setTimeInMillis(eventDate.getTimeInMillis());
			}
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Description);
		} else if (deliveryMethod.getDeliveryMethodEnum() == DeliveryMethodEnum.Pickup) {
			expectedDeliveryDate
					.setExpectedDeliveryDesc("2 hours before event start time");
			if (eventDate != null) {
				expectedDate = (Calendar) sysdate.clone();
				expectedDate.setTimeInMillis(eventDate.getTimeInMillis());
				expectedDate.add(Calendar.HOUR_OF_DAY, -2);
			}
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Description);
		} else if (deliveryMethod.getDeliveryMethodEnum() == DeliveryMethodEnum.OffSitePickup) {
			expectedDeliveryDate.setExpectedDeliveryDesc("Day of the event");
			if (eventDate != null) {
				expectedDate = (Calendar) sysdate.clone();
				expectedDate.setTimeInMillis(eventDate.getTimeInMillis());
			}
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Description);
		} else if (deliveryMethod.getDeliveryMethodEnum() == DeliveryMethodEnum.Courier) {
			expectedDeliveryDate.setExpectedDeliveryDesc("48 hours");
			if (eventDate != null) {
				expectedDate = (Calendar) sysdate.clone();
				expectedDate.setTimeInMillis(eventDate.getTimeInMillis());
				expectedDate.add(Calendar.HOUR_OF_DAY, -48);
			}
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Description);
		} else if (deliveryMethod.getDeliveryMethodEnum() == DeliveryMethodEnum.Email) {
			expectedDeliveryDate
					.setExpectedDeliveryDesc("3 hours before event start time");
			if (eventDate != null) {
				expectedDate = (Calendar) sysdate.clone();
				expectedDate.setTimeInMillis(eventDate.getTimeInMillis());
				expectedDate.add(Calendar.HOUR_OF_DAY, -3);
			}
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Description);
		} else if (deliveryMethod.getDeliveryType() != null
				&& deliveryMethod.getDeliveryType().getDeliveryTypeEnum() == DeliveryTypeEnum.UPS) {
			expectedDate = calculateUpsExpectedDeliveryDate(
					expectedDeliveryDate, deliveryMethod, expectedDate,
					inHandDateCopy, eventDate, inHand);
			expectedDeliveryDate.setExpectedDate(expectedDate);
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Date);
		} else if (deliveryMethod.getDeliveryType() != null
				&& deliveryMethod.getDeliveryType().getDeliveryTypeEnum() == DeliveryTypeEnum.Electronic) {
			expectedDate = calculateElectronicExpectedDeliveryDate(
					expectedDeliveryDate, deliveryMethod, expectedDate,
					inHandDateCopy, eventDate, inHand);
			expectedDeliveryDate.setExpectedDate(expectedDate);
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Date);
		} else if (deliveryMethod.getDeliveryType() != null
				&& deliveryMethod.getDeliveryType().getDeliveryTypeEnum() == DeliveryTypeEnum.ElectronicInstantDownload) {
			expectedDeliveryDate.setExpectedDeliveryDesc("Immediately");
			expectedDate = (Calendar) sysdate.clone();
			expectedDeliveryDate
					.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Description);
		}
		expectedDeliveryDate.setExpectedDate(expectedDate);
		return expectedDeliveryDate;
	}
	
	/**
	 * CalculateUpsExpecteddate. Only WORLDWIDE_EXPRESS_SAVER expected date is
	 * calculated.
	 * 
	 * @param expectedDeliveryDate
	 * @param deliveryMethod
	 * @param expectedDate
	 * @param inHandDate
	 * @param eventDate
	 * @param inHand
	 * @return
	 */
	private Calendar calculateUpsExpectedDeliveryDate(
			ExpectedDeliveryDate expectedDeliveryDate,
			DeliveryMethod deliveryMethod, Calendar expectedDate,
			Calendar inHandDate, Calendar eventDate, boolean inHand) {
		
		//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
		Calendar lExpectedDate = expectedDate;
		if (inHand) {
			lExpectedDate.add(Calendar.HOUR_OF_DAY, 144);
		} else {
			lExpectedDate = inHandDate;
			lExpectedDate.add(Calendar.HOUR_OF_DAY, 96);
		}
		expectedDeliveryDate
				.setExpectedDeliveryType(ExpectedDeliveryDate.ExpectedDeliveryType.Date);
		// Moving UPS delivery date to business dates.Exclude Saturday and
		// Sunday.
		moveUpsDate2BizDate(lExpectedDate);
		// trunk to day
		String dayFormat = "yyyy-MM-dd";
		Calendar lEventDate = eventDate;
		try {
			lEventDate = parseStringToUtcCalendar(
					formatCalendar(eventDate, dayFormat), dayFormat);
		} catch (ParseException e) {
			// should not go here
			log.error("", e);
		}
		if (!lExpectedDate.before(lEventDate)) {
			Calendar bufferDate = new GregorianCalendar();
			bufferDate.setTimeInMillis(lEventDate.getTimeInMillis());
			int dayOfWeek = lEventDate.get(Calendar.DAY_OF_WEEK);
			// If the event day is Sunday or Monday ,treat it as Friday for
			// computing delivery.
			if (dayOfWeek == Calendar.SUNDAY) {
				bufferDate.add(Calendar.HOUR_OF_DAY, -48);
			} else if (dayOfWeek == Calendar.MONDAY) {
				bufferDate.add(Calendar.HOUR_OF_DAY, -72);
			} else {
				bufferDate.add(Calendar.HOUR_OF_DAY, -24);
			}
			lExpectedDate = bufferDate;
		}
		return lExpectedDate;
	}

	/**
	 * calculates the deliverydate for the deliveryMethods of type 'Electronic'
	 * 
	 * @param expectedDeliveryDate
	 * @param deliveryMethod
	 * @param expectedDate
	 * @param inHandDate
	 * @param eventDate
	 * @param inHand
	 * @return
	 */
	private Calendar calculateElectronicExpectedDeliveryDate(
			ExpectedDeliveryDate expectedDeliveryDate,
			DeliveryMethod deliveryMethod, Calendar expectedDate,
			Calendar inHandDate, Calendar eventDate, boolean inHand) {

		//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
		Calendar lExpectedDate = expectedDate;
		if (inHand) {
			expectedDate.add(Calendar.HOUR_OF_DAY, 72);
		} else {
			expectedDate.setTimeInMillis(inHandDate.getTimeInMillis());
			expectedDate.add(Calendar.HOUR_OF_DAY, 24);
		}
		if (expectedDate.after(eventDate)) {
			GregorianCalendar inHandplus1day = new GregorianCalendar();
			inHandplus1day.setTimeInMillis(inHandDate.getTimeInMillis());
			inHandplus1day.add(Calendar.HOUR_OF_DAY, 24);
			lExpectedDate = inHandplus1day;
			if (eventDate.before(lExpectedDate)) {
				lExpectedDate = eventDate;
			}
		}
		return lExpectedDate;
	}

	private void moveUpsDate2BizDate(Calendar cal) {
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY) {
			cal.add(GregorianCalendar.DATE, 1);
			return;
		}
		if (dayOfWeek == Calendar.SATURDAY) {
			cal.add(GregorianCalendar.DATE, 2);
		}
	}

	public Calendar getNowCalByTimeZone(final TimeZone timezone) {
		return Calendar.getInstance(timezone);
	}

	public Calendar getNowCalUTC() {
		return getNowCalByTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public String formatCalendar(final Calendar calendar, final String format) {
		return formatCalendar(calendar, format, calendar.getTimeZone());
	}

	public String formatCalendar(final Calendar calendar, final String format,
			final TimeZone targetTimeZone) {
		if ((calendar == null) || (format == null)) {
			return null;
		}
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		simpleDateFormat.setTimeZone(targetTimeZone);
		return simpleDateFormat.format(calendar.getTime());
	}

	public Calendar parseStringToUtcCalendar(final String dateString,
			final String format) throws ParseException {
		return parseStringToCalendar(dateString, format,
				TimeZone.getTimeZone("UTC"));
	}
	
	public Calendar parseStringToCalendar(final String dateString,
			final String format, final TimeZone timezone) throws ParseException {
		final Calendar cal = Calendar.getInstance(timezone);
		final SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		sdf.setTimeZone(timezone);
		cal.setTime(sdf.parse(dateString));
		return cal;
	}
	
	public void validateAndSetInHandDate(Listing listing, Calendar eihDate, Calendar lihDate) {
		Event event = listing.getEvent();
		Calendar eventDateUTC = DateUtil.convertCalendarToUtc(event.getEventDate());

		//Validate and set inhand date
		if (listing.getInhandDate() != null) {
			// get current UTC date
			Calendar currentDateUTCBOD = DateUtil.getNowCalUTC();
			setHourMinuteSeconds ( currentDateUTCBOD, 0, 0, 0);
			
			Calendar currentDateUTCEOD = DateUtil.getNowCalUTC();
			setHourMinuteSeconds ( currentDateUTCEOD, 23, 59, 59);
			
			Calendar listingIHDate = listing.getInhandDate() ;
			
			// convert the passed IHDate (assumed as TZ of event) to UTC date
			Calendar listingInhandDateEventLocalBOD = new GregorianCalendar(event.getJdkTimeZone());
			listingInhandDateEventLocalBOD.set(listingIHDate.get(Calendar.YEAR), listingIHDate.get(Calendar.MONTH), 
					listingIHDate.get(Calendar.DAY_OF_MONTH),0,0,0);
			Calendar listingInhandDateUTCBOD = DateUtil.convertCalendarToNewTimeZone(listingInhandDateEventLocalBOD, TimeZone.getTimeZone("UTC"));

			Calendar listingInhandDateEventLocaleEOD = new GregorianCalendar(event.getJdkTimeZone());
			listingInhandDateEventLocaleEOD.set(listingIHDate.get(Calendar.YEAR), listingIHDate.get(Calendar.MONTH), 
					listingIHDate.get(Calendar.DAY_OF_MONTH),23,59,59);
			Calendar listingInhandDateUTCEOD = DateUtil.convertCalendarToUtc(listingInhandDateEventLocaleEOD);
			
			// IH date cannot be passed the event date 
			// SELLAPI-3007 do not validate eventDate if inHandDate is today or in the past
			if (listingInhandDateUTCBOD.after(currentDateUTCEOD) && listingInhandDateUTCBOD.after(eventDateUTC) && !listing.isAdjustInhandDate()) {
				ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_INHANDDATE,
						ErrorEnum.INVALID_INHAND_DATE.getMessage(), "inhandDate");
				throw new ListingBusinessException(listingError);
			}		
						//Commenting out to fix SELLAPI-1879 -Inhand date of relisted listing should have inhand date of original listing
						listingIHDate=listingInhandDateUTCBOD;
						log.info("@@@@value of inhand date"+listingIHDate);
						
						// If IH date is before event EIHD, then set to EIHD
						if (eihDate != null && listingInhandDateUTCEOD.before(eihDate)) {
							// We used to issue error here but lets be lenient  
							//ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_INHANDDATE, ErrorEnum.INVALID_INHAND_DATE.getMessage(), "inhandDate");
							//throw new ListingBusinessException(listingError);
							listingIHDate = eihDate;
						}
						else {
							/*
							 * If todays date is before LIH and if listing in-hand/delivery date
							 * does not fall between todays date and LIH, then throw exception
							 */
							StringBuilder logLihDate = new StringBuilder();
							logLihDate.append(lihDate.get(Calendar.MONTH)+1).append("/").append(lihDate.get(Calendar.DAY_OF_MONTH)).append("/").append(lihDate.get(Calendar.YEAR));
							if (currentDateUTCEOD.before(lihDate)){
								if(listingInhandDateUTCBOD.after(lihDate)) {
									if(listing.isAdjustInhandDate()){
										StringBuilder logString = buildLogDataString(listing, listingIHDate,eventDateUTC, logLihDate);
										log.info(logString.toString());
										listingIHDate=lihDate;
										listing.setInHandDateAdjusted(true);
										
									}else{
										ListingError listingError = new ListingError(ErrorType.INPUTERROR, 
												ErrorCode.INVALID_INHANDDATE, ErrorEnum.INVALID_INHAND_DATE.getMessage(), "inhandDate");
										throw new ListingBusinessException(listingError);
									}
								}
							}
							// if current date is GT or EQ LIH date 
							else{
								if(listingInhandDateUTCBOD.after(currentDateUTCEOD)){
							
								// if listing date is after current date, issue error
									//EXTSELL-4 Adjust IHD to LIHD
									if(listing.isAdjustInhandDate()){
										StringBuilder logString = buildLogDataString(listing, listingIHDate,eventDateUTC, logLihDate);
										log.info(logString.toString());
										listingIHDate=currentDateUTCBOD;
										listing.setInHandDateAdjusted(true);
									}else{
										ListingError listingError = new ListingError(ErrorType.INPUTERROR, 
												ErrorCode.INVALID_INHANDDATE, ErrorEnum.INVALID_INHAND_DATE.getMessage(), "inhandDate");
										throw new ListingBusinessException(listingError);
									}
								}
							}
						}


			listing.setInhandDate(listingIHDate);
			
			if(!listing.getInhandDate().after(currentDateUTCEOD)) {
				listing.setDeclaredInhandDate(listingIHDate);
			}				
							
		}else{
			if (listing.getInhandDate() == null && DateUtil.getNowCalUTC().before(lihDate)) {
				listing.setInhandDate(lihDate);
			}
			else if (listing.getInhandDate() == null) {
				listing.setInhandDate(org.apache.commons.lang.time.DateUtils.truncate(DateUtil.getNowCalUTC(), Calendar.DATE));
			}			
		}
		listing.setInhandDateValidated(true);
	}
	
	private StringBuilder buildLogDataString(Listing listing,Calendar listingIHDate, Calendar eventDateUTC,	StringBuilder logLihDate) {
		StringBuilder logString = new StringBuilder();
		try {
			logString.append("Adjusting listing request in hand date with LIHD");
			logString.append(getContextFromHeader()).append(SPACE);
			logString.append("LIHD=").append(logLihDate).append(SPACE);
			logString.append("listingIHDate=").append(new StringBuilder().append(listingIHDate.get(Calendar.MONTH)+1).append("/").append(listingIHDate.get(Calendar.DAY_OF_MONTH)).append("/").append(listingIHDate.get(Calendar.YEAR))).append(SPACE);
			logString.append("Adjusted listingIHDate=").append(logLihDate).append(SPACE);
			logString.append("externalListingId=").append(listing.getExternalId()).append(SPACE);
			logString.append("eventId=").append(listing.getEventId()).append(SPACE);
			logString.append("eventDate=").append(new SimpleDateFormat("MM/dd/yyyy").format(eventDateUTC.getTime())).append(SPACE);
			logString.append("quantity=").append(listing.getQuantity()).append(SPACE);
			logString.append("deliveryOption="+ listing.getDeliveryOption()).append(SPACE);
			if(listing.getListPrice()!= null){
				logString.append("listPrice=").append(listing.getListPrice().getAmount()).append(SPACE).append("currency=").append(listing.getListPrice().getCurrency()).append(SPACE);
			}
		} catch (Exception e) {
			log.warn("Error while logging the adjusted in hand date");
		}
		return logString;
	}

	/**
	 * To set BOD and EOD for calendar 
	 * @param cal
	 * @param hour
	 * @param min
	 * @param sec
	 */
	private void setHourMinuteSeconds ( Calendar cal, int hour, int min, int sec )
	{
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);		
	}
	
	private String getContextFromHeader() {
		SHServiceContext serviceContext = (SHServiceContext) SHThreadLocals
				.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

		StringBuilder logStringFromHeader = new StringBuilder();
		if(serviceContext != null){
			ExtendedSecurityContext esc = serviceContext.getExtendedSecurityContext();
			if (esc != null) {
				logStringFromHeader.append(SELLER_ID_HEADER).append(esc.getUserId()).append(APP_NAME_HEADER).append(esc.getApplicationName());
			}
		}else
			logStringFromHeader.append(SELLER_ID_HEADER).append("NA").append(APP_NAME_HEADER).append("NA").toString();
		
	   return logStringFromHeader.toString();
	}
	
	
	/**
	 * check if the ticket is type of paper ticket	
	 * @param listing
	 */
		
	public boolean isShipping(Listing listing){
		if( listing.getTicketMedium() != null  && (TicketMedium.PAPER.getValue() == listing.getTicketMedium().intValue() ||
			 TicketMedium.EVENTCARD.getValue() == listing.getTicketMedium().intValue() ||
			 TicketMedium.SEASONCARD.getValue() == listing.getTicketMedium().intValue() ||
			 TicketMedium.RFID.getValue() == listing.getTicketMedium().intValue() ||
			 TicketMedium.WRISTBAND.getValue() == listing.getTicketMedium().intValue() ||
			 TicketMedium.GUESTLIST.getValue() == listing.getTicketMedium().intValue())){
				log.info("api_domain=inventory, api_method=isTypeOfPaperTicket, ticketMedium="+ listing.getTicketMedium().intValue());
				return true;
			}else{
				return false;
			}
		
	}
	
	private boolean barcodeFallback(FulfillmentMethod fm) {
	    if(fm == null) {
	        return false;
	    }
	    
	    if(fm == FulfillmentMethod.LMS || fm == FulfillmentMethod.PDF || fm == FulfillmentMethod.UPS || fm == FulfillmentMethod.SHIPPING) {
	        return true;
	    }
	    
	    return false;
	}
}
