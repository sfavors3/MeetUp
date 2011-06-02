package com.pete.meetup.location.manager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class MyLocationManager /*implements Service*/ {
	
	//private static final int TWO_MINUTES 	= 1000 * 60 * 2;
	private static final int GPS_MIN_TIME 	= 1000;
	private static final int GPS_MIN_DIST 	= 5;
	
	private LocationManager 	locationManager;
	private LocationListener 	locationListener;
	private MyLocationListener 	listener;
	private boolean				running;
	//private Location			bestLocation;

	public MyLocationManager(LocationManager locationManager, MyLocationListener listener) {
		this.locationManager 		= locationManager;
		this.listener				= listener;
		//this.bestLocation			= myLocation.getLocation();
		
		this.locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				setCurrentLocation(location);
			}

			public void onProviderDisabled(String provider) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		};
	}
	
	private synchronized void setCurrentLocation(Location location) {
		//if (isBetterLocation(location, bestLocation)) {
		//	bestLocation = location;
			listener.LocationChanged(location);
		//}
	}
	
	public void startService() {
		if (locationManager != null &&
			locationListener != null) {
			
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						GPS_MIN_TIME, GPS_MIN_DIST, locationListener);
			}
			
			running = true;
			
			/*
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, locationListener);
			}
			*/
		}
	}
	
	public void stopService(boolean destroying) {
		if (locationManager != null &&
			locationListener != null) {
			locationManager.removeUpdates(locationListener);
		}
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	/*
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	    
	    // assume gps is always the most accurate
	    if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
	    	return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;
	    
	    Log.d(MeetUp.LOG_TAG, "current provider: " + currentBestLocation.getProvider() +
	    		" new provider: " + location.getProvider());

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	    	Log.d(MeetUp.LOG_TAG, "significantly newer");
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	    	Log.d(MeetUp.LOG_TAG, "significantly older");
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());
	    		
	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	    	Log.d(MeetUp.LOG_TAG, "more accurate");
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	    	Log.d(MeetUp.LOG_TAG, "newer and ! less accurate");
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	    	Log.d(MeetUp.LOG_TAG, "newer and ! significantly less accurate and same provider");
	        return true;
	    }
	    return false;
	}
	*/

	/** Checks whether two providers are the same */
	/*
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	*/
}
