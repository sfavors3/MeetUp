package com.pete.meetup.location;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "locations")
public class LocationList {

	@ElementList(inline = true, required = false)
	private List<PersonLocation> locations;
	
	public List<PersonLocation> getLocations() {
		return locations;
	}
}
