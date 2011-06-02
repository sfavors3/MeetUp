package com.pete.meetup.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.pete.meetup.MeetUp.ViewType;
import com.pete.meetup.location.PersonLocation;

public class MapZoomController {
	
	/*
	private int minLat = Integer.MAX_VALUE;
	private int maxLat = Integer.MIN_VALUE;
	private int minLon = Integer.MAX_VALUE;
	private int maxLon = Integer.MIN_VALUE;
	*/
	
	private boolean hasLocation;
	private List<GeoPoint> points = new ArrayList<GeoPoint>();
	
	private MapView view;
	private MapController controller;
	
	public MapZoomController(MapView view) {
		this.view = view;
		this.controller = view.getController();
	}
	
	public void zoomToMiddle(PersonLocation location, ViewType type) {
		if (location != null) {
			processLocation(location);
			
			zoom(type);
		}
	}
	
	public void zoomToMiddle(List<PersonLocation> locations, ViewType type) {
		if (locations != null) 
		{
			for (PersonLocation location : locations) 
			{ 
				processLocation(location);
			}
		}
		
		zoom(type);
	}
	
	public void centre(PersonLocation location) {
		if (location != null &&
			location.getGeoPoint() != null) {
			controller.setCenter(location.getGeoPoint());
		}
	}
	
	private void zoom(ViewType type) {
		
		if (hasLocation) {
			/*
			int latitude = Math.abs(maxLat - minLat);// * ZOOM_FUDGE_FACTOR_Y);
			int longitude = Math.abs(maxLon - minLon);// * ZOOM_FUDGE_FACTOR_X);
			
			// TODO if satellite then can't zoom in the full way - goes black
			
			controller.zoomToSpan(latitude,
					longitude);
			controller.animateTo(new GeoPoint( (maxLat + minLat)/2, 
					(maxLon + minLon)/2 ));
			*/
			
			GeoCalc.zoomToPointsSpan(view, points.toArray(new GeoPoint[0]),
					type == ViewType.SATELLITE);
			
			view.invalidate();
		}
	}
	
	private void processLocation(PersonLocation location) {
		if (location != null) {
			hasLocation = true;
			
			/*
			int lat = location.getGeoPoint().getLatitudeE6();
			int lon = location.getGeoPoint().getLongitudeE6();
	
			maxLat = Math.max(lat, maxLat);
			minLat = Math.min(lat, minLat);
			maxLon = Math.max(lon, maxLon);
			minLon = Math.min(lon, minLon);
			*/
			
			points.add(location.getGeoPoint());
		}
	}
}
