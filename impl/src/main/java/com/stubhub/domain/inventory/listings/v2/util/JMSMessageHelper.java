package com.stubhub.domain.inventory.listings.v2.util;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.common.entity.Genre;
import com.stubhub.domain.inventory.common.entity.GenreItem;
import com.stubhub.domain.inventory.common.entity.Geo;
import com.stubhub.domain.inventory.common.entity.GeoItem;
import com.stubhub.domain.inventory.common.entity.LMSEvent;
import com.stubhub.domain.inventory.common.entity.LMSInfo;
import com.stubhub.domain.inventory.common.entity.LMSInfoWrapper;
import com.stubhub.domain.inventory.common.entity.LMSListing;
import com.stubhub.domain.inventory.common.entity.LMSSeller;
import com.stubhub.domain.inventory.common.entity.Seat;
import com.stubhub.domain.inventory.common.entity.XMLStreamMsg;
import com.stubhub.domain.inventory.common.entity.XMLStreamMsgHeader;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.entity.SharedWithFriendMessage;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryRequest;
import com.stubhub.newplatform.common.util.DateUtil;

@Component("jmsMessageHelper")
public class JMSMessageHelper {
  private final static Logger log = LoggerFactory.getLogger(JMSMessageHelper.class);

  @Autowired
  @Qualifier(value = "unlockBarcodeMessageTemplate")
  private JmsTemplate unlockBarcodeMessageTemplate;

  @Autowired
  @Qualifier(value = "lockBarcodeMessageTemplate")
  private JmsTemplate lockBarcodeMessageTemplate;

  @Autowired
  @Qualifier(value = "lmsFormMessageTemplate")
  private JmsTemplate lmsFormMessageTemplate;

  @Autowired
  @Qualifier(value = "lmsLookupFormMessageTemplate")
  private JmsTemplate lmsLookupFormMessageTemplate;
  
  @Autowired
  @Qualifier(value = "shareWithFriendsTemplate")
  private JmsTemplate shareWithFriendsTemplate;

  @Autowired
  @Qualifier(value = "lockInventoryRequestProducer")
  private JmsTemplate lockInventoryRequestProducer;

  @Autowired
  @Qualifier(value = "unlockInventoryRequestProducer")
  private JmsTemplate unlockInventoryRequestProducer;

  @Autowired
  @Qualifier(value = "partnerLockInventoryRequestProducer")
  private JmsTemplate partnerLockInventoryRequestProducer;

  @Autowired
  @Qualifier(value = "partnerUnlockInventoryRequestProducer")
  private JmsTemplate partnerUnlockInventoryRequestProducer;

  @Autowired
  private EventHelper eventHelper;

  @Autowired
  private UserHelper userHelper;

  public void sendUnlockBarcodeMessage(long listingId) {
    StringBuilder unlockBarcodeMessage = new StringBuilder();
    unlockBarcodeMessage.append(
        "<msg type=\"event\" name=\"listingMsg\"><header><producer>ListingMessageProducer</producer></header>");
    unlockBarcodeMessage.append("<data>");
    unlockBarcodeMessage.append("<listingId>").append(listingId).append("</listingId>");
    unlockBarcodeMessage.append(
        "<listingSourceId>10</listingSourceId><sendEmails>true</sendEmails><serviceInvoked>false</serviceInvoked>");
    unlockBarcodeMessage.append("</data></msg>");

    final String msgContent = unlockBarcodeMessage.toString();

    try {
      log.info("Sending unlock barcode message listingId=" + listingId);

      unlockBarcodeMessageTemplate.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          Message message = session.createTextMessage(msgContent);
          return message;
        }
      });

      log.info("sent unlock barcode message listingId=" + listingId);
    } catch (Throwable t) {
      log.error("Error while sending unlock barcode message listingId=" + listingId, t);
    }
  }

  public void sendShareWithFriendsMessage(Long orderId,
      List<Map<String, String>> orderItemToSeatMap, String toEmailId, String toCustomerGUID,
      String listingId, String sellerPaymentTypeId) {

    final SharedWithFriendMessage sharedWithFriendMessage = new SharedWithFriendMessage();
    sharedWithFriendMessage.setOrderId(orderId);
    sharedWithFriendMessage.setListingId(listingId);
    sharedWithFriendMessage.setToCustomerGUID(toCustomerGUID);
    sharedWithFriendMessage.setToEmailId(toEmailId);
    sharedWithFriendMessage.setOrderItemToSeatMap(orderItemToSeatMap);
    sharedWithFriendMessage.setPaymentType("1");
    if (sellerPaymentTypeId != null)
      sharedWithFriendMessage.setPaymentType(sellerPaymentTypeId);
    final ObjectMapper mapperObj = new ObjectMapper();
    try {
      final String msgContent = mapperObj.writeValueAsString(sharedWithFriendMessage);
      log.info(
          "_message=\"Sending Share With Friends Messagege \" orderId={} toEmailId={} toCustomerGUID={} listingId={} msgContent={}",
          orderId, toEmailId, toCustomerGUID, listingId, msgContent);
      shareWithFriendsTemplate.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          Message message = session.createTextMessage(msgContent);
          return message;
        }
      });

      log.info("_message=\" sent share with friends message listingId={}", listingId);
    } catch (Throwable t) {
      // TODO throw an error and delete the listing
      log.error("Error while sending share with friends message listingId=" + listingId, t);
    }

  }

  public void sendLockMessage(long listingId) {
    sendLockMessage(listingId, 10);
  }

  public void sendLockMessage(long listingId, int listingSourceId) {
    StringBuilder barcodelockMessage = new StringBuilder();
    barcodelockMessage.append(
        "<msg type=\"event\" name=\"listingMsg\"><header><producer>ListingMessageProducer</producer></header>");
    barcodelockMessage.append("<data>");
    barcodelockMessage.append("<listingId>").append(listingId).append("</listingId>");
    barcodelockMessage.append("<listingSourceId>").append(listingSourceId).append(
        "</listingSourceId><sendEmails>true</sendEmails><serviceInvoked>false</serviceInvoked>");
    barcodelockMessage.append("</data></msg>");

    final String msgContent = barcodelockMessage.toString();

    try {
      log.info("Sending lock barcode message listingId=" + listingId);

      lockBarcodeMessageTemplate.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          Message message = session.createTextMessage(msgContent);
          return message;
        }
      });

      log.info("sent lock barcode message listingId=" + listingId);
    } catch (Throwable t) {
      log.error("Error while sending lock barcode message listingId=" + listingId, t);
    }
  }

  public void sendCreateLMSListingMessage(Listing lmsListing) {
		final Long listingId=lmsListing.getId();
    try {
      StringWriter writer = new StringWriter();
      JAXBContext context = JAXBContext.newInstance(XMLStreamMsg.class);
      Marshaller m = context.createMarshaller();
      XMLStreamMsg msg = new XMLStreamMsg();

      XMLStreamMsgHeader header = new XMLStreamMsgHeader();
      header.setProducer("LMSFormMessageProducer");
      header.setLastUpdatedBy("LMS Form");

      LMSSeller seller = new LMSSeller();
      seller.setSellerID(lmsListing.getSellerId().toString());
      com.stubhub.domain.inventory.listings.v2.entity.UserContact userContact =
          userHelper.getDefaultUserContact(lmsListing.getSellerId());

      if (userContact != null) {
        seller.setFirstName(userContact.getFirstName());
        seller.setLastName(userContact.getLastName());
        seller.setSellerEmail(userContact.getEmail());
      }
      // SELLAPI-1238 08/23/15 added null(locale), false(getTraits) because seatTraits are not used
      // here
      Event event = eventHelper.getEventById(lmsListing.getEventId(), null, null, false);

      LMSEvent lmsEvent = new LMSEvent();
      lmsEvent.setEventID(lmsListing.getEventId().toString());
      lmsEvent.seteTicket(true);

      SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
      Calendar eventDate = (GregorianCalendar) DateUtil
          .convertCalendarToNewTimeZone(event.getEventDate(), TimeZone.getTimeZone("UTC"));
      lmsEvent.setEventDate(dateTimeFormatter.format(eventDate.getTime()));

      SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      lmsEvent.setEventDateLocal(dtf.format(event.getEventDate().getTime()));

      lmsEvent.setEventName(event.getDescription());
      lmsEvent.setTicketingMedium("E-Ticket");


      lmsEvent.setVenue(event.getVenueDesc());
      Geo geo = new Geo();
      geo.setItem(new ArrayList<GeoItem>());
      if (event.getGeoPath() != null) {
        String[] nodes = event.getGeoPath().split("/");
        for (int i = 0; i < nodes.length; i++) {
          GeoItem item = new GeoItem();
          item.setGeoID(nodes[i]);
          geo.getItem().add(item);
        }
        lmsEvent.setGeo(geo);
      }

      Genre genre = new Genre();
      genre.setItem(new ArrayList<GenreItem>());
      if (event.getGenrePath() != null) {
        String[] nodes = event.getGenrePath().split("/");
        for (int i = 0; i < nodes.length; i++) {
          GenreItem gItem = new GenreItem();
          gItem.setGenreID(nodes[i]);
          genre.getItem().add(gItem);
        }
        lmsEvent.setGenre(genre);
      }

      LMSListing listing = new LMSListing();
      listing.seteTicket(false);
      listing.setTicketID(lmsListing.getId().toString());
      listing.setTicketingMedium("Paper");
      listing.setPricePerTicket(lmsListing.getListPrice().getAmount().toPlainString());
      listing.setQuantity(lmsListing.getQuantity().toString());
      listing.setSection(lmsListing.getSection());
      listing.setRow(lmsListing.getRow());
      Seat seat = new Seat();
      seat.setSeatItem(new ArrayList<String>());
      String[] seats = lmsListing.getSeats().split(",");
      for (int i = 0; i < seats.length; i++)
        seat.getItem().add(seats[i]);

      listing.setSeats(seat);

      LMSInfo info = new LMSInfo();
      info.setLmsSeller(seller);
      info.setLmsEvent(lmsEvent);
      info.setLmsListing(listing);


      LMSInfoWrapper wrapper = new LMSInfoWrapper();
      wrapper.setLmsInfo(info);

      msg.setName("DOS Start Process UCA");
      msg.setType("event");
      msg.setHeader(header);
      msg.setData(wrapper);
      m.marshal(msg, writer);

      String messageContent = writer.toString();
      messageContent = messageContent
          .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
      final String content = messageContent;

      log.info("Sending LMS listing message listingId=" + lmsListing.getId());
      log.debug("LMS message=" + content);
      lmsLookupFormMessageTemplate.send(new MessageCreator() {
          public Message createMessage(javax.jms.Session session) throws JMSException {
        	  MapMessage message = session.createMapMessage();
        	  message.setLong("listingId", listingId);
        	  message.setString("lmsContentXml", content);
          return message;
        }
      });

      log.info("Sent LMS listing message listingId=" + lmsListing.getId());
    } catch (Throwable t) {
      log.error("Error while sending LMS listing  message listingId=" + lmsListing.getId(), t);
    }

  }

  /**
   * Helper method to send lock inventory request message as part of listing creation or predelivery
   * process.
   * 
   * @param listingId - Stubhub ListingId
   */
  public void sendLockInventoryMessage(final Long listingId) {
    sendLockInventoryMessage(listingId, null);
  }
  
  public void sendLockInventoryMessage(final Long listingId, final String memberIds) {
    try {
      log.info(
          "_message=\"Sending LockInventoryRequest message during listing creation or predelivery process...\" listingId={}",
          listingId);
      lockInventoryRequestProducer.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          MapMessage message = session.createMapMessage();
          message.setLong("listingId", listingId);
          if(memberIds != null) {
            message.setString("memberIds", memberIds);
          }
          return message;
        }
      });
      log.info(
          "_message=\"Successfully sent LockInventoryRequest message during listing creation or predelivery process...\" listingId={}",
          listingId);
    } catch (Throwable t) {
      log.error(
          "_message=\"Failed to send LockInventoryRequest message during listing creation or predelivery process...\" listingId={}",
          listingId, t);
    }
  }

  /**
   * Helper method to construct seatMap as part of transfer process and push the message to lock
   * inventory request queue.
   * 
   * @param listingId - Stubhub ListingId
   * @param transfer - true/false indicating if its a transfer operation
   * @param transferredSeatMap - seatMap
   */
  public void sendLockTransferMessage(final String listingId, final Boolean transfer,
      List<Map<String, String>> transferredSeatMap) {

    final StringBuilder sb = new StringBuilder();
    for (Map<String, String> maps : transferredSeatMap) {
      sb.append(maps.get("itemId")).append("-").append(maps.get("seatId")).append(",");
    }
    try {
      log.info(
          "_message=\"Sending LockInventoryRequest message during transfer process...\" listingId={}",
          listingId);
      lockInventoryRequestProducer.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          MapMessage message = session.createMapMessage();
          message.setLong("listingId", Long.parseLong(listingId));
          message.setBoolean("transfer", transfer);
          message.setString("seatMap", sb.toString().substring(0, sb.toString().length() - 1));
          return message;
        }

      });
      log.info(
          "_message=\"Successfully sent LockInventoryRequest message during transfer process...\" listingId={}",
          listingId);
    } catch (Throwable t) {
      log.error(
          "_message=\"Failed to send LockInventoryRequest message during transfer process...\" listingId={}",
          listingId, t);
    }
  }

  /**
   * Helper method to send unlock inventory request message as part of listing deletion process.
   * 
   * @param listingId - Stubhub ListingId
   */
  public void sendUnlockInventoryMessage(final Long listingId) {
    try {
      log.info(
          "_message=\"Sending UnlockInventoryRequest message during listing delete process...\" listingId={}",
          listingId);
      unlockInventoryRequestProducer.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          MapMessage message = session.createMapMessage();
          message.setLong("listingId", listingId);
          return message;
        }
      });
      log.info(
          "_message=\"Successfully sent UnlockInventoryRequest message during listing delete process...\" listingId={}",
          listingId);
    } catch (Throwable t) {
      log.error(
          "_message=\"Failed to send UnlockInventoryRequest message during listing delete process...\" listingId={}",
          listingId, t);
    }
  }

  /**
   * Helper method to send partner lock inventory request message during predelivery processing.
   * 
   * @param lockInventoryRequest - {@link LockInventoryRequest}
   * @param isCallback - boolean flag to indicate callback workflow
   * @parma memberIds - String list of memberIds
   * @param callbackPayload
   */
  public void sendPartnerLockInventoryMessage(final LockInventoryRequest lockInventoryRequest,
      final Boolean isCallback, final String callbackPayload, final Boolean forceLock) {
	  sendPartnerLockInventoryMessage(lockInventoryRequest, isCallback, callbackPayload,forceLock, null);
  }
	  
  public void sendPartnerLockInventoryMessage(final LockInventoryRequest lockInventoryRequest,
	      final Boolean isCallback, final String callbackPayload, final Boolean forceLock, final String memberIds) {
    try {
      log.info(
          "_message=\"Sending PartnerLockInventoryRequest message during predelivery processing...\" {}",
          lockInventoryRequest);
      final ObjectMapper mapper = new ObjectMapper();
      final String request = mapper.writeValueAsString(lockInventoryRequest);

      partnerLockInventoryRequestProducer.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          MapMessage message = session.createMapMessage();
          message.setLong("listingId", lockInventoryRequest.getListing().getId());
          message.setString("payload", request);
          if (isCallback) {
            message.setBoolean("isCallback", isCallback);
            message.setString("callbackPayload", callbackPayload);
          }
          if(StringUtils.isNotBlank(memberIds)) {
        	  	message.setString("memberIds", memberIds);
          }
          if (forceLock) {
            message.setBoolean("forceLock", forceLock);
          }
          return message;
        }
      });
      log.info(
          "_message=\"Successfully sent PartnerLockInventoryRequest message during predelivery processing...\" request={}",
          request);
    } catch (Throwable t) {
      log.error(
          "_message=\"Failed to send PartnerLockInventoryRequest message during predelivery processing...\" {}",
          lockInventoryRequest, t);
    }
  }

  /**
   * Helper method to send partner unlock inventory request message during delete or expiry of a
   * predelivered listing.
   * 
   * @param unlockInventoryRequest - {@link UnlockInventoryRequest}
   */
  public void sendPartnerUnlockInventoryMessage(final UnlockInventoryRequest unlockInventoryRequest,
      final boolean forceUnlock) {
    try {
      log.info(
          "_message=\"Sending PartnerUnlockInventoryRequest message \" {}",
          unlockInventoryRequest);
      final ObjectMapper mapper = new ObjectMapper();
      final String request = mapper.writeValueAsString(unlockInventoryRequest);

      partnerUnlockInventoryRequestProducer.send(new MessageCreator() {
        public Message createMessage(javax.jms.Session session) throws JMSException {
          MapMessage message = session.createMapMessage();
          message.setString("payload", request);
          message.setLong("listingId", unlockInventoryRequest.getListing().getId());
          if (forceUnlock) {
            message.setBoolean("forceUnlock", forceUnlock);
          }
          return message;
        }
      });
      log.info(
          "_message=\"Successfully sent PartnerUnlockInventoryRequest message\" request={}",
          request);
    } catch (Throwable t) {
      log.error(
          "_message=\"Failed to send PartnerUnlockInventoryRequest message during listing delete processing...\" {}",
          unlockInventoryRequest, t);
    }
  }

}
