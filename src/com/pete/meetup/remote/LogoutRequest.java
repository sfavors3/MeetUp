package com.pete.meetup.remote;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.net.Uri;

import com.pete.meetup.location.PersonLocation;

public class LogoutRequest implements RemoteRequest {

	private PersonLocation myLocation;
	
	public LogoutRequest(PersonLocation myLocation) {
		this.myLocation = myLocation;
	}
	
	public void process(HttpClient client) throws ClientProtocolException,
			IOException {
		Uri uri = Uri.parse(BASE_URI + LOGOUT_PATH);
		uri = uri.buildUpon().appendQueryParameter(PARAM_SESSION, myLocation.getSession()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_PERSON, myLocation.getPerson()).build();
		
		HttpGet request = new HttpGet(uri.toString());
	
		client.execute(request);
	}
}
