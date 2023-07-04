package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryResponse;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;

/**
 * Helper to get information around listing, primarily used by lock/unlock inventory handlers.
 * 
 * @author rkesara
 *
 */
public interface IntegrationManager {

  /**
   * Helper method to pull listing basic details along with seat status. Seat status will be further
   * used to determine if a seat needs to be locked or unlocked on primary.
   * 
   * @param listingId - ListingId
   * @param specifiedTicketSeatIds - only include those asked
   * @return {@link Listing} details
   */
  public Listing getListing(final Long listingId);

  public Listing getListing(final Long listingId, List<Long> specifiedTicketSeatIds);

  /**
   * Helper method to populate basic inventory details into {@link LockInventoryRequest} instance.
   * 
   * @param listing - {@link Listing} data
   * @param sellerContact - {@link CustomerContactV2Details} data
   * @return {@link LockInventoryRequest} instance.
   */
  public LockInventoryRequest createLockInventoryRequest(final Listing listing,
      final CustomerContactV2Details sellerContact);


  /**
   * Helper method to populate basic inventory details into {@link UnlockInventoryRequest} instance.
   * 
   * @param listing - {@link Listing} data
   * @param sellerContact - {@link CustomerContactV2Details} data
   * @return {@link UnlockInventoryRequest} instance.
   */
  public UnlockInventoryRequest createUnlockInventoryRequest(final Listing listing,
      final CustomerContactV2Details sellerContact);

  public UnlockInventoryRequest createUnlockInventoryRequest(final Listing listing,
      final CustomerContactV2Details sellerContact, List<Long> unlockTicketSeatIds);

  /**
   * Helper method to process lock response, update seats with actual info from partner, update
   * delivery option if necessary and also set system status to ACTIVE.
   * 
   * @param lockInventoryResponse - {@link LockInventoryResponse}
   */
  public void updateListingAfterLock(final LockInventoryResponse lockInventoryResponse);

  /**
   * Helper method to process unlock response, update status to 1 in tickets table to indicate
   * completion of unlock.
   * 
   * @param unlockInventoryResponse - {@link UnlockInventoryResponse}
   */
  public void updateListingAfterUnlock(final UnlockInventoryResponse unlockInventoryResponse);

  /**
   * Helper method to invoke cs apis and lookup customer guid by customerId.
   * 
   * @param userId - Stubhub userId
   * @return Stubhub UserGuid
   */
  public String getUserGuid(final Long userId);
  
  public void updateListingAndSeats(Listing listing, List<TicketSeat> ticketSeats);
  
  /**
   * Helper method to unlock the partial removed seats before sending to Lock queue and updating 
   * the Listing/Ticket status to 4. 
   * @param listingId
   * @param s
   */
  public void updateTicketStatus(Long listingId, short s);

}
