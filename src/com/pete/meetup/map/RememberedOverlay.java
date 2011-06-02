package com.pete.meetup.map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.utils.TextFormatter;

public class RememberedOverlay extends PersonOverlay {

	private TextFormatter nameFormatter;
	
	public RememberedOverlay(LocationTapListener tapListener, MapView mapView, Drawable defaultMarker,
			TextFormatter nameFormatter, int overlayId, BalloonManager manager) {
		super(tapListener, mapView, defaultMarker, overlayId, manager, false);
		this.nameFormatter = nameFormatter;
	}
	
	@Override
	public String getName(PersonLocation location) {
		return location.getNameForDisplay(nameFormatter);
	}

	@Override
	public String getSnippet(PersonLocation location) {
		return "tap here";
	}
}
