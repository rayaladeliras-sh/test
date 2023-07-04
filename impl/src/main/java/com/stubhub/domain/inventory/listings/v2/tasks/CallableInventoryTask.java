package com.stubhub.domain.inventory.listings.v2.tasks;

import java.util.concurrent.Callable;
import com.stubhub.domain.inventory.datamodel.entity.Listing;

public interface CallableInventoryTask<T> extends Callable<T>
{
	/**
	 * After initializing the task object, you can call this method to determine if the Task Object 
	 * is needed to run
	 * 
	 * @return true if task is needed to run
	 */
	boolean ifNeedToRunTask ();
}
 

