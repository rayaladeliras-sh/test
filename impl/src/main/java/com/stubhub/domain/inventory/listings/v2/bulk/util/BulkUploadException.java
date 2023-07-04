package com.stubhub.domain.inventory.listings.v2.bulk.util;

/**
 * 
 * Exception class for BulkUploadQueue processing
 *
 */
public class BulkUploadException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2668579422551805697L;

	public BulkUploadException() {
	}

	public BulkUploadException(String message) {
		super(message);
	}

	public BulkUploadException(Throwable cause) {
		super(cause);
	}

	public BulkUploadException(String message, Throwable cause) {
		super(message, cause);
	}

}