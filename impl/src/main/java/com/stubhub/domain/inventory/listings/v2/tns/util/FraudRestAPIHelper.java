package com.stubhub.domain.inventory.listings.v2.tns.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.stubhub.domain.infrastructure.common.exception.derived.SHBizException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.tns.SHRestTemplate;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

public class FraudRestAPIHelper {
	
	private static final String X_SH_SERVICE_CONTEXT = "X-SH-Service-Context";
	private static final String TNS_API_BEARER_KEY = "newapi.accessToken";
	private static final Logger log = LoggerFactory.getLogger(FraudRestAPIHelper.class);
	private static final String DOMAIN = "inventory";
	
	protected RestTemplate restTemplate;
	
	@Autowired
	protected MasterStubhubPropertiesWrapper masterStubhubProperties;
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	private static final List<HttpStatus> HTTP_SUCCESS_CODES = Arrays.asList(HttpStatus.OK,HttpStatus.ACCEPTED,HttpStatus.CREATED);
	
	@PostConstruct
	public void init() {
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		restTemplate = SHRestTemplate.createRestTemplate(3000);
	}
	
	public <O> ResponseEntity<O> callAPI(Object request, Class<O> clazz, String url, HttpMethod httpMethod, String apiName,
			MultiValueMap<String, String> requestHeaders) {
				log.info("api_domain={}, api_method={}, message=\"Starting API Call for : {}\" , apiURI={},  ", DOMAIN,
						"callAPI", apiName, url);
				boolean success = false;
				try {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					ResponseEntity<String> responseString = restTemplate.exchange(url, httpMethod,
							new HttpEntity(request, requestHeaders), String.class);
					if (null == responseString || null == responseString.getStatusCode()) {
						throw new SHBizException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),
								HttpStatus.INTERNAL_SERVER_ERROR.name());
					} else if (!HttpStatus.Series.SUCCESSFUL.equals(responseString.getStatusCode().series())) {
						log.error(
								"api_domain={} api_method={} message=\"Invalid response code for API call : {}\" responseCode=\"{}\" ",
								DOMAIN, "callAPI", apiName, responseString.getStatusCode(),
								responseString.getBody());
					} else {
						success = true;
					}
					ResponseEntity<O> genericResponseEntity = buildResponseEntity(clazz, responseString,apiName);
					return genericResponseEntity;
				} catch (Exception e) {
					log.error(
							"api_domain={} api_method={} message=\"Unknown exception during API call : {}\"  exception=\"{}\" ",
							DOMAIN, "getCustomer", apiName, ExceptionUtils.getFullStackTrace(e), e);
					throw new SHBizException(HttpStatus.INTERNAL_SERVER_ERROR.name(), e.getMessage(), e);
				} finally {
					log.info("api_domain={}, api_method={}, message=\"Finishing API Call for : {}\" , apiURI={},  success={}",
							DOMAIN, "callAPI", apiName, url, success);
				}
			}

	@SuppressWarnings("unchecked")
	private <O> ResponseEntity<O> buildResponseEntity(Class<O> clazz, ResponseEntity<String> responseString, String apiName)
			throws IOException, JsonParseException, JsonMappingException {
		O responceObect = null;
		String responseBody = responseString.getBody();
		log.info(
				"api_domain={} api_method={} message=\"Response for API call : {}\" responseCode=\"{}\" ",
				DOMAIN, "callAPI", apiName, responseString.getStatusCode());
		log.debug(
				"api_domain={} api_method={} message=\"Response body for API call : {}\" responseCode=\"{}\" responseBody=\"{}\" ",
				DOMAIN, "callAPI", apiName, responseString.getStatusCode(),
				responseBody);
		if(HTTP_SUCCESS_CODES.contains(responseString.getStatusCode()) && StringUtils.isNotBlank(responseBody)){
			if(clazz.getCanonicalName().equals(String.class.getCanonicalName())) {
				responceObect = (O)responseBody;
			}else {
				responceObect = objectMapper.readValue(responseString.getBody(), clazz);
			}
		}else {
			log.error(
					"api_domain={} api_method={} message=\"Invalid response code or body for API call : {}\" responseCode=\"{}\" responseBody=\"{}\" ",
					DOMAIN, "callAPI", apiName, responseString.getStatusCode(),
					responseString.getBody());
		}
		ResponseEntity<O> genericResponseEntity = new ResponseEntity<O>(responceObect, responseString.getHeaders(),responseString.getStatusCode());
		return genericResponseEntity;
	}

	public String getProperty(String propertyName, String defaultValue) {
		return masterStubhubProperties.getProperty(propertyName, defaultValue);
	}

	public <O> ResponseEntity<O> callAPI( Object request, Class<O> clazz, String url, HttpMethod httpMethod, String apiName) {
		return callAPI(request, clazz, url, httpMethod, apiName,
				getRequestHeaders("", "", "", "application/json"));
	}

	protected String getAuthHeader() {
		return "Bearer " + getProperty(TNS_API_BEARER_KEY, "");
	}

	String getServiceContextHeader(String proxiedId, String role, String operatorId) {
		Map<String, String> attributeMap = new HashMap<>();
		attributeMap.put(SHServiceContext.ATTR_PROXIED_ID,
				StringUtils.isNotBlank(proxiedId) ? proxiedId : ("fraudEval"));
		attributeMap.put(SHServiceContext.ATTR_OPERATOR_ID,
				StringUtils.isNotBlank(operatorId) ? operatorId : ("fraudEval"));
		attributeMap.put(SHServiceContext.ATTR_ROLE, StringUtils.isNotBlank(role) ? role : ("R3"));
		return attributeMap.toString();
	}

	protected MultiValueMap<String, String> getRequestHeaders(String proxiedId, String role, String operatorId, String contentType) {
		final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.put(X_SH_SERVICE_CONTEXT, Arrays.asList(getServiceContextHeader(proxiedId, role, operatorId)));
		headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(contentType));
		headers.put(HttpHeaders.ACCEPT, Arrays.asList(contentType));
		headers.put(HttpHeaders.AUTHORIZATION, Arrays.asList(getAuthHeader()));
		return headers;
	}

}
