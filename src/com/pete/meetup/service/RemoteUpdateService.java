package com.pete.meetup.service;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.pete.meetup.MeetUp;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.location.manager.Locations;
import com.pete.meetup.location.manager.PersonLocationListener;
import com.pete.meetup.location.manager.PersonLocationManager;

public class RemoteUpdateService extends Service implements PersonLocationListener {

	private PersonLocationManager manager;

	@Override
	public void onCreate() {
		super.onCreate();

		manager = new PersonLocationManager(this);
	}

	@Override
	public void onDestroy() {
		manager.stopService(true);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (manager != null &&
			!manager.isRunning()) {
			manager.startService();
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void LocationsChanged(List<PersonLocation> locations) {
		if (locations != null) {
			Locations.setOtherLocations(locations);
			
			Intent intent = new Intent(MeetUp.ACTION_LOCATIONS_UPDATE);
			
			sendBroadcast(intent, null);
		}
	}
}
