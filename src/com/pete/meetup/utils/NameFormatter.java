package com.pete.meetup.utils;

public class NameFormatter implements TextFormatter {

	private int maxLength;
	
	public NameFormatter(int maxLength) {
		this.maxLength = maxLength;
	}
	public String formatText(String input) {
		if (input == null) {
			return "";
		}
		
		int length = input.length() < maxLength ? input.length() : maxLength; 
		return input.toLowerCase().substring(0, length);
	}
}
