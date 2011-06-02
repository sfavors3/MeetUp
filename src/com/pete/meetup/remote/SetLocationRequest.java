package com.pete.meetup.remote;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicResponseHandler;

import com.pete.meetup.location.PersonLocation;

import android.net.Uri;

public class SetLocationRequest implements RemoteRequest {

	private PersonLocation location;
	
	public SetLocationRequest(PersonLocation location) {
		this.location = location;
	}
	
	public void process(HttpClient client) throws ClientProtocolException, IOException {
		Uri uri = Uri.parse(BASE_URI + LOCATION_PATH);
		uri = uri.buildUpon().appendQueryParameter(PARAM_SESSION, location.getSession()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_PERSON, location.getPerson()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_NAME, location.getName()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_LATITUDE, location.getLatitude()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_LONGITUDE, location.getLongitude()).build();
		
		HttpPut request = new HttpPut(uri.toString());
		
		ResponseHandler<String> handler = new BasicResponseHandler(); 

		client.execute(request, handler);
	}

}
