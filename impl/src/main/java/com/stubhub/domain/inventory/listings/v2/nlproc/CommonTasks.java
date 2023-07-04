package com.stubhub.domain.inventory.listings.v2.nlproc;

import java.util.ArrayList;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.tasks.RecallableInventoryTask;

public class CommonTasks 
{
	private ArrayList<RecallableInventoryTask<Listing>> tasks = null;
	
	public void addRecalllableTask ( RecallableInventoryTask<Listing> task )
	{
		if ( tasks == null ) {
			tasks = new ArrayList<RecallableInventoryTask<Listing>>();
		}
		tasks.add( task );
	}
	
	public void setListingValues ( Listing listing, Listing currentListing )
	{
		if ( tasks != null && tasks.size() > 0 ) {
			
			for ( RecallableInventoryTask<Listing> t: tasks ) {
				t.callAgain(listing, currentListing);
			}
		}
	}
}
