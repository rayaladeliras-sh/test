package com.stubhub.domain.inventory.listings.v2.newflow.task;

public interface Task<T> {
	public T call();
}
