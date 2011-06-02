package com.pete.meetup.remote;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.net.Uri;
import android.util.Log;

import com.pete.meetup.MeetUp;
import com.pete.meetup.location.LocationList;
import com.pete.meetup.location.PersonLocation;

public class GetLocationRequest implements RemoteRequest {

	private PersonLocation myLocation;
	private List<PersonLocation> locations;
	
	public GetLocationRequest(PersonLocation myLocation) {
		this.myLocation = myLocation;
	}

	public void process(HttpClient client) throws ClientProtocolException,
			IOException {
		Uri uri = Uri.parse(BASE_URI + LOCATION_PATH);
		uri = uri.buildUpon().appendQueryParameter(PARAM_SESSION, myLocation.getSession()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_PERSON, myLocation.getPerson()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_NAME, myLocation.getName()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_LATITUDE, myLocation.getLatitude()).build();
		uri = uri.buildUpon().appendQueryParameter(PARAM_LONGITUDE, myLocation.getLongitude()).build();
		
		HttpGet request = new HttpGet(uri.toString());
		
		ResponseHandler<String> handler = new BasicResponseHandler(); 
		
		Log.d(MeetUp.LOG_TAG, "Get location send");
		String response = client.execute(request, handler);
		Log.d(MeetUp.LOG_TAG, "Get location receive");
		
		if (response != null) {
			Serializer serializer = new Persister();
			
			LocationList locationList;
			try {
				Log.d(MeetUp.LOG_TAG, "serialise start");
				locationList = serializer.read(
						LocationList.class, response);
				Log.d(MeetUp.LOG_TAG, "serialise end");
			} catch (Exception e) {
				throw new RuntimeException("Failed to deserialize location list", e);
			}
			
			locations = locationList.getLocations();
		}
	}
	
	public List<PersonLocation> getLocations() {
		return locations;
	}
}
