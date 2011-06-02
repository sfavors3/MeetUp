package com.pete.meetup.location.manager;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;

import com.pete.meetup.MeetUp;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.remote.GetLocationRequest;
import com.pete.meetup.remote.LogoutRequest;
import com.pete.meetup.remote.RemoteRequest;

public class PersonLocationManager extends AsyncTask<Void, Void, Void> /*implements Service*/ {

	private static final int HTTP_CONFIG_CONNECTION_TIMEOUT = 500;
	//private static final int HTTP_CONFIG_SOCKET_TIMEOUT 	= 1000;
	private static final int POLLING_PERIOD_MS				= 500;
	
	private PersonLocationListener 	listener;
	private List<PersonLocation> 	locations;
	private boolean 				running 	= false;
	private boolean					logout 		= false;
	
	public PersonLocationManager(PersonLocationListener listener) {
		this.listener	= listener;
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		running = true;
		while (running) {
			// check if task has been cancelled
			if (isCancelled()) {
				running = false;
			} else {
				try {
					
					PersonLocation myLocation = Locations.getMyLocation();
					
					// wait until we have a location fix before updating location
					if (myLocation != null &&
						myLocation.locationSet()) {
						//Log.d(MeetUp.LOG_TAG, "Got location, sending location to server");
						
						locations = getLocations(myLocation);
						
						if (locations != null) {
							publishProgress();
						}
					}
				} catch (Exception e) {
					// swallow for the moment
					Log.e(MeetUp.LOG_TAG, "Failed to get locations from server");
				}
				
				if (running) {
					try {
						Thread.sleep(POLLING_PERIOD_MS);
					} catch (InterruptedException e) {
						running = false;
					}
				}
			}
		}
		
		return null;
	}
	
	public void startService() {
		execute();
	}
	
	public void stopService(boolean destroying) {
		this.logout = destroying;
		running = false;
		cancel(true);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	
	@Override
	protected void onProgressUpdate(Void... values) {
		if (listener != null) {
			listener.LocationsChanged(locations);
		}
	}
	
	@Override
	protected void onCancelled() {
		try {
			if (logout) {
				logout(Locations.getMyLocation());
			}
		} catch (Exception e) {
			// swallow for the moment
			Log.e(MeetUp.LOG_TAG, "Failed to get logout");
		}
	}
	
	private void logout(PersonLocation location) {
		try {
			if (location != null) {
				LogoutRequest request = new LogoutRequest(location);
				
				processRequest(request);
			}
		} catch (Exception e) {
			// swallow
			Log.e(MeetUp.LOG_TAG, "Failed to log out user");
		}
	}
	
	/*
	private void setLocation(PersonLocation location) {
		SetLocationRequest request = new SetLocationRequest(location);
		
		processRequest(request);
	}
	*/
	
	private List<PersonLocation> getLocations(PersonLocation location) {
		GetLocationRequest request = new GetLocationRequest(location);
		
		processRequest(request);
		
		return request.getLocations();
	}
	
	public void processRequest(RemoteRequest request) {
		HttpClient client = null;
		
		try {
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, HTTP_CONFIG_CONNECTION_TIMEOUT);
			// SoTimeout is timeout waiting for data
			//HttpConnectionParams.setSoTimeout(httpParams, HTTP_CONFIG_SOCKET_TIMEOUT);
			client = new DefaultHttpClient(httpParams);
			
			request.process(client);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}
}
