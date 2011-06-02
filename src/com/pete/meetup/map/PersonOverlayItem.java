package com.pete.meetup.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.pete.meetup.location.PersonLocation;

public class PersonOverlayItem extends OverlayItem {

	private PersonLocation personLocation;
	
	public PersonOverlayItem(GeoPoint point, String title,
			String snippet, PersonLocation personLocation) {
		super(point, title, snippet);
		this.personLocation = personLocation;
	}

	public PersonLocation getPersonLocation() {
		return personLocation;
	}
}
