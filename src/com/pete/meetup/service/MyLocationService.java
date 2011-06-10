package com.pete.meetup.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import com.pete.meetup.MeetUp;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.location.manager.Locations;
import com.pete.meetup.location.manager.MyLocationListener;
import com.pete.meetup.location.manager.MyLocationManager;

public class MyLocationService extends Service implements MyLocationListener {

	private MyLocationManager locationManager;
	//private MockLocationManager locationManager;

	@Override
	public void onCreate() {
		super.onCreate();

		//locationManager = new MockLocationManager(this);
		locationManager = new MyLocationManager(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this);
	}

	@Override
	public void onDestroy() {
		locationManager.stopService(true);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (locationManager != null &&
			!locationManager.isRunning()) {
			locationManager.startService();
		}
		return START_STICKY;
	}
	
	public boolean isRunning() {
		if (locationManager != null &&
			locationManager.isRunning()) {
			return true;
		}
		return false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void LocationChanged(Location location) {
		
		if (location != null) {
			PersonLocation myLocation = Locations.getMyLocation();
			
			if (myLocation != null) {
				myLocation.setLocation(location);
				
			}
			Intent intent = new Intent(MeetUp.ACTION_MY_LOCATION_UPDATE);
			
			sendBroadcast(intent, null);
		}
	}
}
