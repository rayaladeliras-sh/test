package com.stubhub.test.inventory;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomNumberGenerator {

	private static final String ALL_CHAR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String randomNumbers() {
		return RandomStringUtils.randomNumeric(8);
	}

	public static String generateRandomBarcodeForTDC() {
		String tdcBarcode = null;
		Random random = new Random();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 12; i++) {
			if (i == 4) {
				sb.append("-");
			}
			sb.append(ALL_CHAR.charAt(random.nextInt(ALL_CHAR.length())));
		}

		tdcBarcode = sb.toString();
		return tdcBarcode;
	}

	public static String generateRandomBarcodeForAEG() {
		return RandomStringUtils.randomNumeric(14);
	}

	public static String generateRandomBarcodeForTM() {
		String tmBarcode = null;
		Random random = new Random();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 12; i++) {
			sb.append(random.nextInt(10));
		}
		tmBarcode = sb.toString();
		return tmBarcode;
	}

	public static String generateRandomBarcodeForPAC() {
		String pacBarcode = null;

		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 14; i++) {
			sb.append(random.nextInt(10));
		}

		pacBarcode = sb.toString();
		return pacBarcode;
	}

	public static String generateExternalListingId() {
		String pacBarcode = null;

		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb.append("EXT-").append(random.nextInt(10));
		}

		pacBarcode = sb.toString();
		return pacBarcode;
	}

	public static String generateRandomBarcodeForTMDirect() {
		String tmdBarcode = null;

		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 13; i++) {
			sb.append(random.nextInt(10));
		}

		tmdBarcode = sb.toString() + "4";
		return tmdBarcode;
	}
	
	public static Seats generateRandomSeats() {
		Random random = new Random();
		
		final int min = 1;
		final int max = 100;
		int rand = random.nextInt((max - min) + 1 ) + min;
		return new Seats(String.valueOf(rand), String.valueOf(rand + 1));
	}

}
