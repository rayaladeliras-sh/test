package com.stubhub.domain.inventory.listings.v2.tns.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;

public abstract class AbstractFraudAPIHelper extends FraudRestAPIHelper {

	private static final Logger log = LoggerFactory.getLogger(AbstractFraudAPIHelper.class);
	private static final String DOMAIN = "inventory";

	protected static final String CUSTOMER_V2_GET_API_URL = "customer.details.api.url";
	protected static final String CUSTOMER_V2_GET_API_URL_DEFAULT = "https://api.stubcloudprod.com/user/customers/v2/{customerGUID}";

	@Autowired
	protected ObjectMapper objectMapper;

	public AbstractFraudAPIHelper() {
		super();
	}

	public String getUserGuidFromUid(Long customerId) {
		String customerGUID = null;
		String customerGUIDUrl = null;
		boolean successFlag = false;
		try {
			log.info("api_domain={}, api_method={}, calling,  userId={} ", DOMAIN, "getUserGuidFromUid", customerId);
			final int maxRetryCount = Integer.parseInt(getProperty("customer.guid.retryCount", "1"));
			ObjectMapper userGuidObjectMapper = new ObjectMapper();
			userGuidObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			userGuidObjectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);
			for (int i = 0; i <= maxRetryCount; i++) {
				try {
					customerGUIDUrl = getProperty("customer.guid.api.url",
							"https://api-int.stubprod.com/user/customers/v2/{customerId}/guid");
					if (!StringUtils.isBlank(customerGUIDUrl)) {
						customerGUIDUrl = customerGUIDUrl.replaceAll("\\{customerId\\}", String.valueOf(customerId));
						log.info(
								"api_domain={}, api_method={}, message=\"calling getCustomerGuid\",  userId={} url={} maxRetryCount={}",
								DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount);
						SHMonitor mon = SHMonitorFactory.getMonitor();
						ResponseEntity<String> responseEntity = null;
						try {
							mon.start();
							responseEntity = callAPI(null, String.class, customerGUIDUrl, HttpMethod.GET,
									"getCustomerGuid");
						} catch (Exception e) {
							log.error(
									"api_domain={}, api_method={}, message=\"Error while calling getCustomerGuid\",  userId={} url={} maxRetryCount={} exception={}",
									DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount,
									ExceptionUtils.getFullStackTrace(e), e);
						} finally {
							mon.stop();
							log.info(SHMonitoringContext.get() + " _operation=getUserGuidFromUid"
									+ " _message= service call for customerId=" + customerId + "  _respTime="
									+ mon.getTime());
						}
						if (responseEntity != null) {
							if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
								log.info(
										"api_domain={}, api_method={}, message=\"GetCustomerGuid call success\",  userId={} url={} maxRetryCount={} responseCode={}",
										DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount,
										responseEntity.getStatusCode());
								String responseString = responseEntity.getBody();
								JsonNode json = userGuidObjectMapper.readTree(responseString);
								if (json != null) {
									JsonNode customerNode = json.get("customer");
									if (customerNode != null) {
										customerGUID = (customerNode.get("userCookieGuid")).getTextValue();
										successFlag = true;
										break;
									}
								}
							} else {
								log.error(
										"api_domain={}, api_method={}, message=\"Invalid Reponse Code while calling getCustomerGuid\",  userId={} url={} maxRetryCount={} responseCode={}",
										DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount,
										responseEntity.getStatusCode());
							}
						}
					} else {
						log.error(
								"api_domain={}, api_method={}, message=\"Invalid Reponse  while calling getCustomerGuid\",  userId={} url={} maxRetryCount={} ",
								DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount);
					}
				} catch (Exception e) {
					if (i == maxRetryCount) {
						log.error(
								"api_domain={}, api_method={}, message=\"Exception  while calling getCustomerGuid\",  userId={} url={} maxRetryCount={} exception={} ",
								DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount,
								ExceptionUtils.getFullStackTrace(e), e);
						SHRuntimeException shException = new SHSystemException(
								"An internal processing error occurred in the system", e);
						shException.setErrorCode("inventory.listings.systemError");
						throw shException;
					}

				}
			}
			if (StringUtils.isBlank(customerGUID)) {
				log.error(
						"api_domain={}, api_method={}, message=\"Unknown Exception  while calling getCustomerGuid\",  userId={} url={} maxRetryCount={} ",
						DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, maxRetryCount);
				SHBadRequestException shException = new SHBadRequestException("Unable to find GUID for " + customerId);
				shException.setErrorCode("inventory.listings.badRequest");
				throw shException;
			}
		} catch (Exception e) {
			log.error(
					"api_domain={}, api_method={}, message=\"Exception  while calling getCustomerGuid\",  userId={} url={}  exception={} ",
					DOMAIN, "getUserGuidFromUid", customerId, customerGUIDUrl, ExceptionUtils.getFullStackTrace(e), e);
			SHRuntimeException shException = new SHSystemException(
					"An internal processing error occurred in the system", e);
			shException.setErrorCode("inventory.listings.systemError");
			throw shException;
		} finally {
			log.info("api_domain={}, api_method={}, complete,  userId={} succes={} customerGUID={} ", DOMAIN,
					"getUserGuidFromUid", customerId, successFlag, customerGUID);
		}
		return customerGUID;
	}

//	public boolean deactivateCustomer(String userGuid, String deactivationReason) {
//		String customerApiUrl = null;
//		boolean successFlag = false;
//		log.info("api_domain={}, api_method={}, calling,  userGuid={},  deactivationReason={}", DOMAIN,
//				"deactivateCustomer", userGuid, deactivationReason);
//		try {
//			customerApiUrl = getProperty(CUSTOMER_V2_GET_API_URL, CUSTOMER_V2_GET_API_URL_DEFAULT);
//			customerApiUrl = customerApiUrl.replace("{customerGUID}", userGuid);
//			Map<String, Object> deactivationRequest = buildUserDeactivationRequest(deactivationReason);
//			SHMonitor mon = SHMonitorFactory.getMonitor();
//			ResponseEntity<String> customerResponse = null;
//			String responseString = null;
//			log.info(
//					"api_domain={}, api_method={}, message=\"Calling Update Customer API\", apiURI={}, userGuid={}, deactivationReason={} ",
//					DOMAIN, "deactivateCustomer", customerApiUrl, userGuid, deactivationReason);
//			try {
//				mon.start();
//				customerResponse = callAPI(deactivationRequest, String.class, customerApiUrl, HttpMethod.PUT,
//						"deactivateCustomer");
//			} catch (Exception e) {
//				log.error(
//						"api_domain={} api_method={} message=\"Exception while making updateCustomer API call\" userGuid={} exception=\"{}\" ",
//						DOMAIN, "deactivateCustomer", userGuid, ExceptionUtils.getFullStackTrace(e), e);
//			} finally {
//				mon.stop();
//				log.info(SHMonitoringContext.get()
//						+ "api_domain={} api_method={} _message=\"Called updateCustomer api\", customerApiUrl={}, userGuid={},   _respTime={}",
//						DOMAIN, "deactivateCustomer", customerApiUrl, userGuid, mon.getTime());
//			}
//			if (customerResponse != null) {
//				if (HttpStatus.Series.SUCCESSFUL.equals(customerResponse.getStatusCode().series())) {
//					log.info(
//							"api_domain={} api_method={} message=\" updateCustomer api call successful for \" userGuid={}  respCode={} ",
//							DOMAIN, "deactivateCustomer", userGuid, customerResponse.getStatusCode());
//					successFlag = true;
//				} else {
//					log.error(
//							"api_domain={} api_method={} message=\"Invalid Response Code while calling updateCustomer api\" userGuid={} respCode={} responseString={}",
//							DOMAIN, "deactivateCustomer", userGuid, customerResponse.getStatusCode(),
//							customerResponse.getBody());
//				}
//			} else {
//				log.error(
//						"api_domain={} api_method={} message=\"Invalid Response Code while calling updateCustomer api\" userGuid={} responseString={}",
//						DOMAIN, "deactivateCustomer", userGuid, responseString);
//			}
//		} catch (Exception e) {
//			log.error(
//					"api_domain={} api_method={} message=\"Unknown exception while making updateCustomer API call\" userGuid={} exception=\"{}\" ",
//					DOMAIN, "deactivateCustomer", userGuid, ExceptionUtils.getFullStackTrace(e), e);
//		} finally {
//			log.info(
//					"api_domain={}, api_method={}, complete,  userGuid={},  deactivationReason={}, apiURI={}, success={}",
//					DOMAIN, "deactivateCustomer", userGuid, deactivationReason, customerApiUrl, successFlag);
//		}
//		return successFlag;
//
//	}

	private Map<String, Object> buildUserDeactivationRequest(String deactivationReason) {
		Map<String, Object> deactivationRequest = new HashMap<>();
		Map<String, Object> customerMap = new HashMap<>();
		customerMap.put("status", "INACTIVE");
		customerMap.put("deactivationReason", deactivationReason);
		deactivationRequest.put("customer", customerMap);
		return deactivationRequest;
	}

	public com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse getCustomer(String userGuid) {

		String customerApiUrl = null;
		boolean successFlag = false;
		try {
			log.info("api_domain={}, api_method={}, calling,  userGuid={},  ", DOMAIN, "getCustomer", userGuid);
			customerApiUrl = getProperty(CUSTOMER_V2_GET_API_URL, CUSTOMER_V2_GET_API_URL_DEFAULT);
			customerApiUrl = customerApiUrl.replace("{customerGUID}", userGuid);

			log.info("api_domain={}, api_method={}, message=\"Calling getCustomer API\", apiURI={}, userGuid={},  ",
					DOMAIN, "getCustomer", customerApiUrl, userGuid);
			SHMonitor mon = SHMonitorFactory.getMonitor();
			ResponseEntity<String> customerResponse = null;
			try {
				mon.start();
				customerResponse = callAPI(null, String.class, customerApiUrl, HttpMethod.GET, "getCustomer");
			} catch (Exception e) {
				log.error(
						"api_domain={} api_method={} message=\"Exception while making getCustomer API call\" userGuid={} exception=\"{}\" ",
						DOMAIN, "getCustomer", userGuid, ExceptionUtils.getFullStackTrace(e), e);
			} finally {
				mon.stop();
				log.info(SHMonitoringContext.get()
						+ " api_domain={} api_method={} _message=\"Called getCustomer api\", customerApiUrl={}, userGuid={},   _respTime={}",
						DOMAIN, "getCustomer", customerApiUrl, userGuid, mon.getTime());
			}
			if (customerResponse != null) {
				if (HttpStatus.OK.equals(customerResponse.getStatusCode())) {
					log.info(
							"api_domain={} api_method={} message=\" getCustomer api call successful for \" userGuid={}  respCode={}",
							DOMAIN, "getCustomer", userGuid, customerResponse.getStatusCode());
					ObjectMapper rObjectMapper = new ObjectMapper();
					rObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					rObjectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);

					rObjectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
					com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse response = rObjectMapper
							.readValue(customerResponse.getBody(),
									com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse.class);
					successFlag = true;
					return response;
				} else {
					log.error(
							"api_domain={} api_method={} message=\" getCustomer api call failed \" userGuid={}  respCode={} response={}",
							DOMAIN, "getCustomer", userGuid, customerResponse.getStatusCode(),
							customerResponse.getBody());
				}
			} else {
				log.error(
						"api_domain={} api_method={} message=\"System error occured while calling getCustomer api\" userGuid={} ",
						DOMAIN, "getCustomer", userGuid);
			}
		} catch (Exception e) {
			log.error(
					"api_domain={} api_method={} message=\"Unknown exception while making getCustomer API call\" userGuid={}",
					DOMAIN, "getCustomer", userGuid, e.getMessage());
		} finally {
			log.info("api_domain={}, api_method={}, complete,  userGuid={},  success={} ", DOMAIN, "getCustomer",
					userGuid, successFlag);
		}
		return null;

	}

}