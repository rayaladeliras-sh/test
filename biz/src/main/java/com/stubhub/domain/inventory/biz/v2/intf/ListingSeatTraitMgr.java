package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.SupplementSeatTrait;



public interface ListingSeatTraitMgr {

	public List<Long> getSeatTraitsFromComments(long eventId,String structuredComments);
	
	public List<ListingSeatTrait> findSeatTraits(long ticketId);

	public void addSeatTrait(ListingSeatTrait seatTrait);

	public void deleteListingSeatTrait(ListingSeatTrait seatTrait);
	
	public SupplementSeatTrait getSupplementSeatTrait (Long supplementSeatTraitId);
	
	public List<SupplementSeatTrait> getSupplementSeatTraitsForListing (Long listingId);

	public List<SupplementSeatTrait> getSupplementSeatTraitsBySeatTraitIds(List<Long> supplementSeatTraitIds);
	
	/**
	 * This method will check if parking pass is supported or not for an event
	 * @param eventId
	 * @return
	 */
	public boolean isParkingSupportedForEvent(Long eventId);
	
	public List<Long> parseComments(Long eventId, String comments);
	
}
