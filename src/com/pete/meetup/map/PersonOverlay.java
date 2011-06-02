package com.pete.meetup.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.pete.meetup.location.PersonLocation;

public abstract class PersonOverlay extends ItemizedOverlay<OverlayItem> {
	
	private LocationTapListener tapListener;
	private MapView mapView;
	private int overlayId;
	private BalloonManager manager;
	private boolean bubbleOnTop;
	
	private List<PersonOverlayItem> overlays = new ArrayList<PersonOverlayItem>();
	
	public PersonOverlay(LocationTapListener tapListener, MapView mapView,
			Drawable defaultMarker, int overlayId, BalloonManager manager, boolean bubbleOnTop) {
		super(boundCenterBottom(defaultMarker));
		this.tapListener = tapListener;	
		this.mapView = mapView;
		this.overlayId = overlayId;
		this.manager = manager;
		this.bubbleOnTop = bubbleOnTop;
	
		populate();
	}

	public void setLocations(Collection<PersonLocation> locations) {
		manager.setLocations(locations, overlayId, tapListener, bubbleOnTop);
		
		this.overlays = new ArrayList<PersonOverlayItem>();

		if (locations != null) {
			for (PersonLocation location : locations) {
				if (location.locationSet()) {
					if (location.getGeoPoint() != null) {
						overlays.add(new PersonOverlayItem(location.getGeoPoint(), "", "", location));
					}
				}
			}
		}
		
	    populate();
	    
	    mapView.invalidate();
	}
	
	public void setLocation(PersonLocation location) {
		Collection<PersonLocation> locations = new ArrayList<PersonLocation>();
		if (location != null) {
			locations.add(location);
			setLocations(locations);
		}
	}
	
	public void clear() {
		manager.setLocations(null, overlayId, tapListener, bubbleOnTop);
		
		this.overlays = new ArrayList<PersonOverlayItem>();
		
		populate();
	    
	    mapView.invalidate();
	}

	@Override
	protected OverlayItem createItem(int index) {
		return overlays.get(index);
	}

	@Override
	public int size() {
		return overlays.size();
	}

	@Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow)
    {
        super.draw(canvas, mapView, shadow);

        if (shadow == false)
        {
        	//can we tell if we're overlapping another view and change orientation?
    		//looks shit when remember a location then it's immediately hidden
	    	for (PersonOverlayItem item : overlays) {
	            GeoPoint point = item.getPoint();
	         
	            manager.updateDisplay(item.getPersonLocation(),
	            		point, getName(item.getPersonLocation()),
	            		getSnippet(item.getPersonLocation()));       
	        }
        }
    }
	
	public abstract String getName(PersonLocation location);

	public abstract String getSnippet(PersonLocation location);
}
