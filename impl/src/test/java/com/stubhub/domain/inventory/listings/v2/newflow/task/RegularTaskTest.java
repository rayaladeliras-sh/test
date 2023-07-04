package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;

public class RegularTaskTest {
	@Test
	public void testRegularTaskSuccess() {
		ListingDTO listingDTO = new ListingDTO(null);
		listingDTO.setStatus(ListingStatus.HIDDEN);
		RegularTask regularTask = new RegularTask(listingDTO) {

			@Override
			protected void preExecute() {
			}

			@Override
			protected void execute() {
				listingDTO.setStatus(ListingStatus.ACTIVE);
			}

			@Override
			protected void postExecute() {

			}

		};

		ListingDTO postListingDTO = regularTask.call();

		assertFalse(regularTask.equals(null));
		assertEquals(regularTask.hashCode(), regularTask.getClass().getName().hashCode());
		assertEquals(postListingDTO.getStatus(), ListingStatus.ACTIVE);
	}

	@Test
	public void testRegularTaskEqual() {
		UpdatePricingTask updatePricingTask1 = new UpdatePricingTask(null);
		UpdatePricingTask updatePricingTask2 = new UpdatePricingTask(null);

		UpdateSplitTask updateSplitTask = new UpdateSplitTask(null);

		assertTrue(updatePricingTask1.equals(updatePricingTask2));
		assertFalse(updatePricingTask1.equals(updateSplitTask));
	}
}
