package com.pete.meetup.map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.pete.meetup.location.PersonLocation;

public class MyOverlay extends PersonOverlay {

	public MyOverlay(LocationTapListener tapListener, MapView mapView,
			Drawable defaultMarker, int overlayId, BalloonManager manager) {
		super(tapListener, mapView, defaultMarker, overlayId, manager, true);
	}
	
	@Override
	public String getName(PersonLocation location) {
		return PersonLocation.NAME_ME;
	}

	@Override
	public String getSnippet(PersonLocation location) {
		return "tap here";
	}
}
