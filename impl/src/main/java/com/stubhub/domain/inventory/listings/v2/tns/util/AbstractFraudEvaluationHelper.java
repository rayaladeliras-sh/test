package com.stubhub.domain.inventory.listings.v2.tns.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.listings.v2.impl.MessagePublishAPIHelper;
import com.stubhub.domain.user.services.customers.v2.intf.CustomerContactInfo;
import com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse;
import com.stubhub.newplatform.common.cache.store.CacheStore;

public class AbstractFraudEvaluationHelper {

	private static final String TES_FRAUD_LISTINGS_DEACTIVATED_SMS = "TES_FRAUD_LISTINGS_DEACTIVATED_SMS";
	private static final Logger log = LoggerFactory.getLogger(AbstractFraudEvaluationHelper.class);
//	private static final String FRAUD_REJECT_EMAIL_URL = "sendmail.httpServiceUrl";
//	private static final String FRAUD_REJECT_EMAIL_URL_DEFAULT = "http://gen3.jobs.stubprod.com/strongmail/sendmail";
	protected static final String DOMAIN = "inventory";
	private static final Locale US_LOCALE = Locale.US;
	private final DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

	@Autowired
	protected FraudAPIHelper fraudAPIHelper;

	@Autowired
	@Qualifier("entityCBCacheStore")
	protected CacheStore cacheStore;

	@Autowired
	protected MessagePublishAPIHelper messagePublishAPIHelper;

	public AbstractFraudEvaluationHelper() {
		super();
	}

	protected Locale getUserLocale(String locale) {
		if (!StringUtils.isBlank(locale)) {
			log.info("api_domain={}, api_method={}, complete, user.getPreferredLocale={}, ", DOMAIN, "getUserLocale",
					locale);
			log.info("user.getPreferredLocale() ={}", locale);
			String[] preferredLocale = locale.split("_");
			return new Locale(preferredLocale[0], preferredLocale[1]);
		} else {
			return US_LOCALE;
		}
	}

	protected String getFormattedDate(Calendar calendar) {
		if (calendar != null) {
			return dateFormatter.format(calendar.getTime());
		}
		return "";

	}

	public void sendListingRejectSMS(Long listingId, Long sellerId) {
		log.info("api_domain={}, api_method={}, calling,  listingId={}, sellerId={},  ", DOMAIN,
				"sendListingRejectEmail", listingId, sellerId);
		SHMonitor mon = SHMonitorFactory.getMonitor();
		try {
			mon.start();
			String userGUID = fraudAPIHelper.getUserGuidFromUid(sellerId);
			GetCustomerResponse userInfo = fraudAPIHelper.getCustomer(userGUID);
			log.info("sendListingRejectEmail userGUID: {}, userInfo: {}", userGUID, userInfo);
			Locale locale = getUserLocale(userInfo);
			Long storeID = userInfo.getPreferredStoreID();
			CustomerContactInfo defaultContact = userInfo.getDefaultContact();
			log.info(
					"sendListingRejectEmail sellerId: {}, userGUID: {}, userInfo: {}, locale: {}, contact: {},storeId: {}",
					sellerId, userGUID, userInfo, locale, defaultContact, storeID);
			messagePublishAPIHelper.sendSMSWithMessageAPI(TES_FRAUD_LISTINGS_DEACTIVATED_SMS, defaultContact, locale, storeID, "" + sellerId);

		} catch (Exception e) {
			log.error(
					"api_domain={} api_method={} message=\"Exception while making Listing reject email API call\" listingId={}, sellerId={}, exception=\"{}\" ",
					DOMAIN, "sendListingRejectEmail", listingId, sellerId, ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			mon.stop();
			log.info(SHMonitoringContext.get()
					+ "api_domain={} api_method={} _message=\"Called listing reject email api\", listingId={}, sellerId={},   _respTime={}",
					DOMAIN, "sendListingRejectEmail", listingId, sellerId, mon.getTime());
		}

	}

	protected synchronized boolean addSellerIdToCache(Long sellerId, String key) {
		boolean flag = false;
		Serializable inCache = cacheStore.get(key);
		if (null == inCache) {
			cacheStore.put(key, sellerId.toString(), this.getCacheExpiryTime());
			flag = true;
		}
		return flag;
	}

	/**
	 * Cache expiry time in seconds
	 * 
	 * @return
	 */
	protected int getCacheExpiryTime() {
		String expiry = fraudAPIHelper.getProperty("tns.couchbase.seller.cache.expiry", "301");
		log.debug("api_domain={}, api_method={}, message=\"getCacheExpiryTime:{}\" ", DOMAIN, "call", expiry);
		return Integer.parseInt(expiry);
	}

	protected String getKey(String key) {
		return "FraudEvaluationListingUpdateListener:" + key;
	}

	public Locale getUserLocale(GetCustomerResponse user) {
		if (!StringUtils.isBlank(user.getPreferredLocale())) {
			String[] preferredLocale = user.getPreferredLocale().split("_");
			return new Locale(preferredLocale[0], preferredLocale[1]);
		} else {
			return US_LOCALE;
		}
	}
	

}