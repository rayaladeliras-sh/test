package com.stubhub.domain.inventory.listings.v2.util;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.newplatform.common.util.DateUtil;

public class TicketSeatUtils 
{
	public static String delFromCSVString ( String csvList, String item )
	{
		StringBuilder sb = new StringBuilder (","+csvList+",");
		String delItem = ","+ item + ",";
		int i = sb.indexOf( delItem );
		if ( i >= 0 ) {
			sb.delete(i, i+item.length()+1);
		}
		sb.deleteCharAt(0);
		if ( sb.length() > 0 ) {
			sb.setLength(sb.length()-1);
		}
		return sb.toString();
	}
	
	public static String addToCSVString ( String csvList, String item ) 
	{
		if ( csvList == null || csvList.length()==0 ) {
			return item.trim();
		}
		if (item != null && !item.isEmpty()){
			return csvList.trim() + "," + item.trim();
		}
		else{
			return csvList.trim();
		}
	}
	
	public static String addToCSVStringUnique ( String csvList, String item ) 
	{
		String list = ","+csvList+",";
		
		// if not there add
		if ( list.indexOf(","+item+",") < 0 ) {
			return addToCSVString ( csvList,  item );
		}
		return csvList;
	}
	
	public static TicketSeat findLikeSeat ( List<TicketSeat> ticketSeatsFromDB )
	{
		if ( ticketSeatsFromDB!=null && ticketSeatsFromDB.size()>0 ) {
			for ( TicketSeat seat : ticketSeatsFromDB ) {
				if ( seat.getTixListTypeId() == 1l && seat.getSeatStatusId() == 1l ) {
					return seat;
				}
			}
		}
		return null;
	}
	
	public static TicketSeat makeTicketSeatLike ( Listing currentListing, TicketSeat pLikeSeat, String newSeatNumber )
	{
		//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
		TicketSeat likeSeat = pLikeSeat;
		if ( likeSeat == null ) {
			likeSeat = new TicketSeat();
			likeSeat.setTixListTypeId(1L);	// regular ticket
			likeSeat.setSeatStatusId(1L);
			likeSeat.setSection( currentListing.getSection());
			if ( isGASection (currentListing.getSection()) || currentListing.getEvent().getGaIndicator() ) {
				likeSeat.setSeatNumber(null);	// seat number is null for GA listing
				likeSeat.setRow(CommonConstants.GA_ROW_DESC);
				likeSeat.setGeneralAdmissionInd(true);
			}
		}
		
		TicketSeat seat = new TicketSeat();
		seat.setTicketId(currentListing.getId());
		seat.setSection(likeSeat.getSection());
		seat.setRow(likeSeat.getRow());
		
		if ( StringUtils.isEmpty(newSeatNumber) )
			seat.setSeatNumber(likeSeat.getSeatNumber());
		else 
			seat.setSeatNumber(newSeatNumber);
		
		seat.setSeatDesc(likeSeat.getSeatDesc());
		if (likeSeat.getGeneralAdmissionInd() != null){
			seat.setGeneralAdmissionInd(likeSeat.getGeneralAdmissionInd());
		}
		else{
			seat.setGeneralAdmissionInd(false);
		}
		seat.setTixListTypeId( likeSeat.getTixListTypeId() );
		seat.setSeatStatusId( likeSeat.getSeatStatusId() );
		Calendar utcNow = DateUtil.getNowCalUTC();
		seat.setCreatedDate(utcNow);
		seat.setLastUpdatedDate(utcNow);
		seat.setCreatedBy(CommonConstants.LISTING_API_V2);
		seat.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
		return seat;
	}
	
	public static TicketSeat createParkingTicketSeat ( Listing listing)
	{
		TicketSeat seat = new TicketSeat();
		seat.setSection("Lot");
		seat.setRow("LOT");
		seat.setTicketId(listing.getId());
		seat.setSeatNumber("Parking Pass");
		seat.setSeatDesc("Parking");
		seat.setGeneralAdmissionInd(false);
		seat.setTixListTypeId(2l);
		seat.setSeatStatusId(1l);
		Calendar utcNow = DateUtil.getNowCalUTC();
		seat.setCreatedDate(utcNow);
		seat.setLastUpdatedDate(utcNow);
		seat.setCreatedBy(CommonConstants.LISTING_API_V2);
		seat.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
		return seat;		
	}
	
	public static ListingSeatTrait makeListingSeatTrait ( long listingId, long supplementSeatTraitId, 
			String createdBy, String updatedBy)
	{
		ListingSeatTrait seatTrait = new ListingSeatTrait();
		seatTrait.setActive(true);
		seatTrait.setSellerSpecifiedInd(true);
		seatTrait.setExtSystemSpecifiedInd(false);
		seatTrait.setSupplementSeatTraitId(supplementSeatTraitId);
		seatTrait.setTicketId(listingId);
		Calendar utcNow = DateUtil.getNowCalUTC();
		seatTrait.setCreatedDate(utcNow);
		seatTrait.setLastUpdatedDate(utcNow);
		seatTrait.setCreatedBy(createdBy);
		seatTrait.setLastUpdatedBy(updatedBy);
		seatTrait.setMarkForDelete(false);
		return seatTrait;
	}
	//commenting out based SELLAPI-3674 Sonar fixes - no reference to this method
/*	public static List<String> makeListFromCSVString ( String csvSeats ) 
	{
		if ( !StringUtils.isEmpty(csvSeats) ) {
			String [] seats = csvSeats.split(",");
			
			ArrayList<String> seatList = new ArrayList<String>();
			for ( int i=0; i<seats.length; i++ ) 
				seatList.add( seats[i].trim() );
			
			return seatList;
		}
		return null;
	} */
	
	public static String makeCSVStringFromList ( List seats ) 
	{
		StringBuilder sb = new StringBuilder(200);
		for ( int i=0; i<seats.size(); i++ ) {
			sb.append(seats.get(i)).append(',');
		}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	
	//commenting out based SELLAPI-3674 Sonar fixes - no reference to this method
  /*public static String removeSpacesFromCSVString ( String csvSeats )
	{
		if ( csvSeats != null && csvSeats.indexOf(',')>0 ) {
			String [] parts = csvSeats.split(",");
			StringBuilder sb = new StringBuilder(200);
			for ( int i=0; i<parts.length; i++ ) {
				sb.append( parts[i].trim() ).append(',');
			}
			sb.setLength( sb.length() - 1);
			return sb.toString();
		}
		return csvSeats;
	} */
	
	public static int countItemsInCSVString ( String csvString )
	{
		return StringUtils.countMatches(csvString, ",");
	}
	
	public static boolean isGASection ( String section )
	{
		return ( section != null ) && ( section.equalsIgnoreCase(CommonConstants.GENERAL_ADMISSION) );
	}
}
