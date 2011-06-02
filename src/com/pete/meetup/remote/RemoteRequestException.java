package com.pete.meetup.remote;

public class RemoteRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8307733259059578200L;

	public RemoteRequestException(String message) {
		super(message);
	}
	
	public RemoteRequestException(String message, Throwable t) {
		super(message, t);
	}
}
