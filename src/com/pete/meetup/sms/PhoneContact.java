package com.pete.meetup.sms;

public class PhoneContact {

	private String number;
	private String name;
	
	public PhoneContact(String number, String name) {
		this.number = number;
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}
}
