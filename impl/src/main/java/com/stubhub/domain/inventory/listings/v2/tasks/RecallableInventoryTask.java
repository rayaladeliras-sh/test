package com.stubhub.domain.inventory.listings.v2.tasks;

public interface RecallableInventoryTask<T> extends CallableInventoryTask<T>
{
	/**
	 * Interface of tasks that can be called again with previously cached values retrieved from external services 
	 * @param listing
	 * @param currentList
	 */
	void callAgain ( T listing, T currentList );
}
 

