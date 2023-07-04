package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;

@Component("kafkaMessageConstructor")
public class KafkaMessageConstructor {

	  private static final String EVENTNAME = "eventName";
	  private static final String EVENTDATE = "eventDate";
	  private static final String VENUENAME = "venueName";
	  private static final String CREATEDBY = "createdBy";
	  private static final String TYPE = "type";
	  private static final String NAME = "name";
	  private static final String VENUE = "venue";
	  private static final String EVENT = "event";

	  @Autowired
	  @Qualifier("kafkaProducer")
	  private KafkaProducer kafkaProducer;
	  
	 public void constructMessage(EventInfo eventInfo, String sellerId, String typeValue) {
		 
		    Map<String, String> kafkaMsg = new HashMap<>();
		    kafkaMsg.put(EVENTNAME, eventInfo.getName());
		    if(StringUtils.trimToNull(eventInfo.getEventLocalDate()) != null) {
		      	kafkaMsg.put(EVENTDATE, eventInfo.getEventLocalDate().substring(0, 16));
		      } else {
		      	kafkaMsg.put(EVENTDATE, eventInfo.getDate().substring(0, 16));
		    }
		    kafkaMsg.put(VENUENAME, eventInfo.getVenue());
		    kafkaMsg.put(CREATEDBY, sellerId);
		    kafkaMsg.put(TYPE, typeValue);
		    if(typeValue.equalsIgnoreCase(VENUE)){
                kafkaMsg.put(NAME, eventInfo.getVenue());
            }
            if (typeValue.equalsIgnoreCase(EVENT)){
               kafkaMsg.put(NAME, eventInfo.getName());
            }
		    Gson gson = new Gson();
		    String message = gson.toJson(kafkaMsg);
		    kafkaProducer.sendMessage(message);
		    
		  }
}
