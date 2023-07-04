package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.domain.inventory.datamodel.entity.Listing;

public class ListingWrapper 
{
	private Listing listing;
	private SeatProductsContext context;
	
	public ListingWrapper (Listing l, SeatProductsContext context)
	{
		this.listing = l;
		this.context = context;
	}
	public Listing getListing() {
		return listing;
	}
	public void setListing(Listing listing) {
		this.listing = listing;
	}
	public SeatProductsContext getContext() {
		return context;
	}
	public void setContext(SeatProductsContext context) {
		this.context = context;
	}

}
