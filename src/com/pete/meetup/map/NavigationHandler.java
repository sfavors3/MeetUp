package com.pete.meetup.map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.pete.meetup.location.PersonLocation;

public class NavigationHandler {

	private Context context;
	
	public NavigationHandler(Context context) {
		this.context = context;
	}
	
	/**
	 * Launch google navigation
	 * @param latitude
	 * @param longitude
	 * @param walking
	 */
	public void navigateTo(PersonLocation location, boolean walking) {
		if (location != null) {
			// create intent
			double latitude = location.getLatitudeDouble();
			double longitude = location.getLongitudeDouble();
			
			String url = "google.navigation:ll=" + latitude + "," + longitude;
			
			if (walking) {
				url += "&mode=w";
			}
			
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse(url));
			context.startActivity(i);
		}
	}
}
