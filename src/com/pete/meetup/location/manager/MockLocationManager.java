package com.pete.meetup.location.manager;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

public class MockLocationManager extends AsyncTask<Void, Void, Void> /*implements Service*/ {

	private static final int POLLING_PERIOD_MS = 1000;
	
	private MockLocation location = new MockLocation(51.39656007, -2.35173451);
	
	private MyLocationListener 	listener;
	private boolean looping = false;
	
	private class MockLocation extends Location {

		private double latitude;
		private double longitude;
		
		public MockLocation(double latitude, double longitude) {
			super(LocationManager.GPS_PROVIDER);
			this.latitude = latitude;
			this.longitude = longitude;
		}
		
		@Override
		public double getLatitude() {
			return latitude;
		}
		
		@Override
		public double getLongitude() {
			return longitude;
		}
		
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
	}
	
	public MockLocationManager(MyLocationListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		looping = true;
		while (looping) {
			// check if task has been cancelled
			if (isCancelled()) {
				looping = false;
			} else {
				
				location.setLatitude(location.getLatitude() + 0.00001);
				location.setLongitude(location.getLongitude() + 0.00001);
			
				publishProgress();
				
				if (looping) {
					try {
						Thread.sleep(POLLING_PERIOD_MS);
					} catch (InterruptedException e) {
						looping = false;
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		if (listener != null) {
			listener.LocationChanged(location);
		}
	}
	
	public void startService() {
		execute();
	}
	
	public void stopService(boolean destroying) {
		looping = false;
		cancel(true);
	}
	
	public boolean isRunning() {
		return looping;
	}
}
