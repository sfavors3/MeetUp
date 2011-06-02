package com.pete.meetup.remote;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;

public interface RemoteRequest {

	//public static final String BASE_URI 		= "http://94.8.88.172:8080/MeetUpWebServer/services/";
	//public static final String BASE_URI 		= "http://ec2-50-17-83-214.compute-1.amazonaws.com:8080/MeetUpWebServer/services/";
	public static final String BASE_URI 		= "http://www.meetupapp.co.uk/MeetUpWebServer/services/";
	public static final String LOCATION_PATH 	= "location";
	public static final String LOGOUT_PATH 		= "logout";
	
	public static final String PARAM_SESSION 	= "session";
	public static final String PARAM_PERSON 	= "person";
	public static final String PARAM_NAME 		= "name";
	public static final String PARAM_LATITUDE 	= "latitude";
	public static final String PARAM_LONGITUDE 	= "longitude";
	
	public void process(HttpClient client) throws ClientProtocolException, IOException;
}
