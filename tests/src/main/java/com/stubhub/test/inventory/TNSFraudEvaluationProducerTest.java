package com.stubhub.test.inventory;

import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.xml.bind.DatatypeConverter;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.util.Calendar;
import com.stubhub.global.domain.utils.base.database.DBHelper;
import com.stubhub.global.domain.utils.base.database.DBRequest;
import com.stubhub.global.domain.utils.base.database.ResultTable;
import com.stubhub.global.domain.utils.base.database.TableRow;
import com.stubhub.global.domain.utils.base.exception.EnvironmentException;
import com.stubhub.global.domain.utils.base.jmx.JmxMBean;
import com.stubhub.global.domain.utils.base.jmx.JmxUtil;
import com.stubhub.global.domain.utils.base.log.Log;
import com.stubhub.global.domain.utils.base.registry.GlobalRegistry;
import com.stubhub.test.platform.Run;
import com.stubhub.test.platform.Setter;

public class TNSFraudEvaluationProducerTest implements Setter {
	private Run run = null;
	private ActiveMQConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Queue queue = null;
	private QueueBrowser browser = null;
	private static final Boolean TRANSACTIONAL = false;
	private static final String FRAUD_LISTING_EVALUATION_QUEUE = "tns.listing.fraud.evaluation.incoming.queue";
	private final static String role = "slx";
	private static final String PACKAGE_NAME = "SHConfigFPLV1";
	private static final String PACKAGE_DOMAIN = "fpl";
	private static final Logger logger = LoggerFactory.getLogger(TNSFraudEvaluationProducerTest.class);
	private static final String RELEASE_ENV = "slce";
	private static final String BLADE_001 = "001";
	private static final String BLADE_002 = "002";

	private void init(String env) throws JMSException {
		// Creating Factory for connection
		this.run.trace(String.format(
				"tcp://%smqm001.%s.com:61616",
				env,env));
		factory = new ActiveMQConnectionFactory(String.format(
				"tcp://%smqm001.%s.com:61616",
				env,env));
		this.run.trace(factory.getBrokerURL());
		connection = 
				factory.createConnection("admin","admin");
			connection.start();
		this.run.trace(connection.getClientID());
		session = connection.createSession(TRANSACTIONAL, Session.AUTO_ACKNOWLEDGE);
		queue = session.createQueue(FRAUD_LISTING_EVALUATION_QUEUE);
		browser = session.createBrowser(queue);

	}
	
	public String getRandomNumber() {
		Calendar.getInstance().getTime().getTime();
		Date d= new Date();
		return d.getTime()+"";
	}
	
	public void scrubUsersTableForDuplicateEmail(String email) {
		DBRequest dbRequest = DBRequest.getDBRequest();
		String sql = "select ID from STUB.USERS where DEFAULT_EMAIL='"+email+"' ";
		String updateStatement = " update stub.users set DEFAULT_EMAIL=CONCAT('"+email+"',DBMS_RANDOM.VALUE) where ID= ";
		 try {
			ResultTable result = dbRequest.query(sql);
			if (null == result.getTBody()) return;
			if (null == result.getTBody().getTRs()) return;
			for( TableRow row: result.getTBody().getTRs()) {
				if(null==row) {
					this.run.trace("Row Null");
					continue;
				}else {
					if(row.getTD(1)==null) {
						this.run.trace("Column Null");
						continue;
					}else {
						String id = row.getTD(1).getValue();
						this.run.trace("Updating Email for userId : "+id);
						dbRequest.update(updateStatement+id);
					}
				}
			}
		} catch (SQLException e) {
			this.run.trace("Cause : " + e.getCause());
			this.run.trace("Error : " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	public void browseQueue(String env, String listingId, String ticketStatus,boolean validateForExistence) throws JMSException {
		boolean isSuccess = false;
		this.run.trace("env:"+env + "  listingId:" + listingId + "  ticketStatus:" + ticketStatus +" validateForExistence:"+validateForExistence);
		try {
			init(env);
			System.out.println("INIT");
			@SuppressWarnings("rawtypes")
			Enumeration e = browser.getEnumeration();
			while (e.hasMoreElements()) {
				MapMessage message = (MapMessage) e.nextElement();
				this.run.trace("Retrieved  From Queue - {listingId:" + message.getString("listingId") + ",listingStatus"
						+ message.getString("listingStatus") + "}");
				if (listingId.equals(message.getString("listingId"))) {
					isSuccess = true;
					break;
				}
			}
			this.run.trace("Done -  env:"+env + "  listingId:" + listingId + "  ticketStatus:" + ticketStatus +" validateForExistence:"+validateForExistence +" isSuccess:"+isSuccess);
		} catch (Exception e) {
			this.run.trace("Cause : " + e.getCause());
			this.run.trace("Error : " + e.getMessage());
			e.printStackTrace();
		} finally {
			boolean result = false;
			if(validateForExistence) {
				if(!isSuccess) {
					result = isEntityFraudCheckExists(listingId);
				}
				this.run.trace("Final result -   listingId:" + listingId + "  ticketStatus:" + ticketStatus +" validateForExistence:"+validateForExistence +" isSuccess:"+isSuccess +" isEntityFraudCheckExists:"+result);
				Assert.assertTrue((result || isSuccess));
			}else {
				result = isEntityFraudCheckExists(listingId);
				this.run.trace("Final result -   listingId:" + listingId + "  ticketStatus:" + ticketStatus +" validateForExistence:"+validateForExistence +" isSuccess:"+isSuccess +" isEntityFraudCheckExists:"+result);
				Assert.assertFalse(isSuccess && result);
			}
			this.run.trace("TNSFraudEvaluationProducerTest isSuccess : " + isSuccess+"  validateForExistence : "+validateForExistence);
			browser.close();
			connection.close();
		}
	}
	
	    public String getConnectionString() throws Exception {
	        String conString = "jdbc:oracle:thin:{%username%}/{%password%}@(DESCRIPTION=(SDU=32676)(ADDRESS_LIST = (LOAD_BALANCE=ON) (FAILOVER=ON) (ADDRESS=(PROTOCOL=TCP)(HOST={%host%})(PORT={%port%})(SEND_BUF_SIZE=125000)(RECV_BUF_SIZE=125000)) (ADDRESS=(PROTOCOL=TCP)(HOST={%host%})(PORT={%port%})(SEND_BUF_SIZE=125000)(RECV_BUF_SIZE=125000)) (ADDRESS=(PROTOCOL=TCP)(HOST={%host%})(PORT={%port%})(SEND_BUF_SIZE=125000)(RECV_BUF_SIZE=125000))) (CONNECT_DATA =(SERVER = DEDICATED) (SERVICE_NAME = {%service%})(FAILOVER_MODE = (TYPE=SELECT)(METHOD=BASIC)(RETRIES=2)(DELAY=5))))";

	        /* fetch connection string */
	        conString = conString.replace("{%username%}",
	                "STUB_TNS_APP").replace("{%password%}", "dstub_tns_app");
	        String configuration = DBHelper.getDBConfig();
	        if(configuration.isEmpty()) {
	            throw new EnvironmentException("Can't find db configuration from 'http://slcd000dvo015.stubcorp.com/env-db-map'");
	        }
	        String service = configuration.split(":")[1];
	        String host = configuration.split(":")[2];
	        String port = configuration.split(":")[3];
	        conString = conString.replace("{%service%}", service).replace("{%host%}", host).replace("{%port%}", port);
	        return conString.trim();
	    }

	@Override
	public void setRun(Run arg0) {
		this.run = arg0;

	}
	
	public boolean isEntityFraudCheckExists(String listingId) {
		String sql = "select ENTITY_ID from  STUB_TNS_USER.ENTITY_FRAUD_CHECK where ENTITY_TYPE='LISTING' and ENTITY_ID='"+listingId+"'";
		ResultTable result = sqlSelect(sql);
		if (null == result.getTBody()) return false;
        if (null == result.getTBody().getTR(1)) return false;
        return StringUtils.isNotBlank(result.getTBody().getTR(1).getTD(1).getValue());
	}
	
    public ResultTable sqlSelect(String sql) {
        ResultTable result = null;
//        DBRequest dbRequest = DBRequest.getDBRequest();
        DBRequest dbRequest = DBRequest.getDBRequest(new StubDbConnector());
        try {
            Log.info("sql : " + sql);
            result = dbRequest.query(sql);
        } catch (SQLException e) {
            Log.info("[sqlSelect-]" + sql + " SQLException Details:" + e.getMessage());
            Assert.fail("Test Failed Due to sqlSelect : " + sql);
        }
        return result;
    }

	public void changePropertyInEnv(String env, String propertyName, String value) {
		changeProperty(BLADE_001, propertyName, value);
		if (env.toLowerCase().contains(RELEASE_ENV)) {
			changeProperty(BLADE_002, propertyName, value);
		}
	}

	@SuppressWarnings("finally")
	private String changeProperty(String blade, String property, String propertyValue) {
		String value = null;
		try {
			JmxMBean jmxMBean = connectToSlxJMX(blade, PACKAGE_DOMAIN, PACKAGE_NAME);

			if (jmxMBean != null) {
				value = jmxMBean.setValue(property, propertyValue);
			}
		} catch (Exception e) {
			logger.error("Error getting value for broker", e);
		} finally {
			return value;
		}
	}

	@SuppressWarnings("finally")
	private JmxMBean connectToSlxJMX(String blade, String packageDomain, String packageName) {
		JmxMBean jmxBean = null;
		@SuppressWarnings("rawtypes")
		Map keyValue = getCredentials();
		try {
			jmxBean = JmxUtil.console(role, blade, packageDomain, packageName, keyValue.get("key").toString(),
					keyValue.get("value").toString());
		} catch (Exception e) {
			logger.error("Error connection to JMX ", e);
			throw e;
		} finally {
			return jmxBean;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, String> getCredentials() {
		String credent = new String(DatatypeConverter.parseBase64Binary(
				GlobalRegistry.byCommonConfig("account").getValue("stubcorp.user.credential").get()));
		StringTokenizer tokenizer = new StringTokenizer(credent, ":");
		Map keyValue = new HashMap();
		keyValue.put("key", tokenizer.nextToken());
		keyValue.put("value", tokenizer.nextToken());
		return keyValue;
	}

}
