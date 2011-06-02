package com.pete.meetup.map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.utils.TextFormatter;

public class OtherPeopleOverlay extends PersonOverlay {
	
	private static final long SECONDS_IN_MINUTE = 60;
	private static final long SECONDS_IN_HOUR 	= 60*60;
	private static final long SECONDS_IN_DAY 	= 60*60*24;
	
	private TextFormatter nameFormatter;

	public OtherPeopleOverlay(LocationTapListener tapListener, MapView mapView, Drawable defaultMarker,
			TextFormatter nameFormatter, int overlayId, BalloonManager manager) {
		super(tapListener, mapView, defaultMarker, overlayId, manager, true);
		this.nameFormatter = nameFormatter;
	}
	
	@Override
	public String getName(PersonLocation location) {
		StringBuilder text = new StringBuilder();
		
		// ensure name is lower case + truncate
		text.append(location.getNameForDisplay(nameFormatter));
        
        return text.toString();
	}
	
	@Override
	public String getSnippet(PersonLocation location) {
		return getLastUpdated(location);
	}
	
	private String getLastUpdated(PersonLocation location) {
		StringBuilder text = new StringBuilder();
		
		long seconds = location.getSecondsSinceUpdate();
		
		if (!location.isLoggedIn()) {
	        text.append("offline ");
	    }
    	
    	if (seconds != -1) {
    		if (seconds > SECONDS_IN_DAY) {
    			long days = seconds / SECONDS_IN_DAY;
    			text.append("[");
    			text.append(days);
    			text.append("d ago]");
    		} else if (seconds > SECONDS_IN_HOUR) {
    			long hours = seconds / SECONDS_IN_HOUR;
    			text.append("[");
    			text.append(hours);
    			text.append("hr ago]");
    		} else if (seconds > SECONDS_IN_MINUTE) {
    			long minutes = seconds / SECONDS_IN_MINUTE;
    			text.append("[");
    			text.append(minutes);
    			text.append("m ago]");
    		} else {
    			if (seconds < 10) {
    				if (location.isLoggedIn()) {
    					text.append("now");
    				}
    			} else {
    				if (seconds < 30) {
    					seconds = 10;
    				} else {
    					seconds = 30;
    				}
    				text.append("[");
        			text.append(seconds);
        			text.append("s ago]");
    			}
    			
    		}
    	}
    	
    	return text.toString();
	}
}
