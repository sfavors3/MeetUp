package com.pete.meetup.location;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.pete.meetup.utils.TextFormatter;

@Root(name="personLocation")
public class PersonLocation {

	public static final String NAME_ME = "me";
	
	@Element
	private String		session;
	
	@Element
	private String 		person;
	
	@Element (required = false)
	private String		name;

	@Element
	private String 		latitude;
	
	@Element
	private String 		longitude;
	
	@Element
	private long		secondsSinceLastUpdate;
	
	private boolean		me;
	
	private Location	location;
	
	@Element
	private boolean		loggedIn = true;
	
	private boolean		remembered = false;

	public PersonLocation() {
		// no arg constructor
	}
	
	public PersonLocation(String session, String person, String name) {
		this.session 	= session;
		this.person 	= person;
		this.name		= name;
	}
	
	public PersonLocation(String session, String person, String name,
			String latitude, String longitude) {
		this.session 	= session;
		this.person 	= person;
		this.name		= name;
		this.latitude	= latitude;
		this.longitude	= longitude;
	}
	
	public PersonLocation(String session, String person, String name,
			String latitude, String longitude, boolean remembered) {
		this.session 	= session;
		this.person 	= person;
		this.name		= name;
		this.latitude	= latitude;
		this.longitude	= longitude;
		this.remembered = remembered;
	}
	
	public void setLocation(Location location) {
		if (location != null) {
			this.location = location;
			
			this.latitude = Double.toString(location.getLatitude());
			this.longitude = Double.toString(location.getLongitude());
		}
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public long getSecondsSinceUpdate() {
		return secondsSinceLastUpdate;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public void setPerson(String person) {
		this.person = person;
	}
	
	public String getPerson() {
		return person;
	}
	
	public String getSession() {
		return session;
	}

	public GeoPoint getGeoPoint() {
		GeoPoint point = null;
		if (this.latitude != null &&
			this.longitude != null) {
			try {
				double latitude = getLatitudeDouble();
				double longitude = getLongitudeDouble();
				point = new GeoPoint((int)(latitude*1000000),
						(int)(longitude*1000000));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		return point;
	}
	
	public double getLatitudeDouble() {
		return Location.convert(this.latitude);
	}
	
	public double getLongitudeDouble() {
		return Location.convert(this.longitude);
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isMe() {
		return me;
	}

	public void setMe(boolean me) {
		this.me = me;
	}

	public String getNameForDisplay(TextFormatter formatter) {
		String name = this.name;
		
		if (isMe()) {
			name = NAME_ME;
		}
		
		if (formatter != null) {
			return formatter.formatText(name);
		}
		return "";
	}
	
	public boolean locationSet() {
		if (this.latitude == null ||
			this.longitude == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof PersonLocation)) {
			return false;
		}
		
		PersonLocation pl = (PersonLocation)o;
		
		if (!areEqual(this.getSession(), pl.getSession())) {
			return false;
		}
		
		if (!areEqual(this.getPerson(), pl.getPerson())) {
			return false;
		}
		
		return true;
	}
	
	private boolean areEqual(String aThis, String aThat) {
		return aThis == null ? aThat == null : aThis.equals(aThat);
	}
	
	@Override
	public int hashCode() {
		int result = 23;
		
		if (this.getSession() != null) {
			result += this.getSession().hashCode();
		}
		
		if (this.getPerson() != null) {
			result += this.getPerson().hashCode();
		}
		return result;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public boolean isRemembered() {
		return remembered;
	}

	public void setRemembered(boolean remembered) {
		this.remembered = remembered;
	}
}
