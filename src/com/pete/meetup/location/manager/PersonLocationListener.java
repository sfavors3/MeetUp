package com.pete.meetup.location.manager;

import java.util.List;

import com.pete.meetup.location.PersonLocation;

public interface PersonLocationListener {
	public void LocationsChanged(List<PersonLocation> locations);
}
