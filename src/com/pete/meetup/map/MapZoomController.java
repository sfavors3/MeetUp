package com.pete.meetup.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.pete.meetup.MeetUp.ViewType;
import com.pete.meetup.location.PersonLocation;

public class MapZoomController {
	
	private static GeoPoint currentCentrePoint 	= null;
	private static Integer	currentSpanLon		= null;
	private static Integer	currentSpanLat		= null;
	private static int		currentOrientation	= -1;
	
	public static void zoomToMiddle(MapView view, PersonLocation location, ViewType type, int orientation) {
		if (location != null) {
			List<PersonLocation> locations = new ArrayList<PersonLocation>();
			locations.add(location);
			zoomToMiddle(view, locations, type, orientation);
		}
	}
	
	public static void zoomToMiddle(MapView view, List<PersonLocation> locations, ViewType type, int orientation) {
		if (locations != null) {
			List<GeoPoint> points = getPoints(locations);
			zoom(view, type, points, orientation);
		}
	}
	
	private static void zoom(MapView view, ViewType type, List<GeoPoint> points,  int orientation) {
		
		if (points != null &&
			!points.isEmpty()) {
			
			//GeoCalc.zoomToPointsSpan(view, points.toArray(new GeoPoint[0]),
			//		type == ViewType.SATELLITE);
			
			GeoPoint[] pointArr = points.toArray(new GeoPoint[0]);
			
			boolean redraw = false;
			boolean orientationChange = (orientation != currentOrientation);
			
			// only change centre point if different to current centre point
			if (setCentre(view, pointArr, orientationChange)) {
				redraw = true;
			}
			
			if (setSpan(view, type, pointArr, orientationChange)) {
				redraw = true;
			}
			
			if (orientationChange) {
				currentOrientation = orientation;
				redraw = true;
			}
			
			if (redraw) {
				view.invalidate();
			}
		}
	}
	
	private static boolean setCentre(MapView view, GeoPoint[] points, boolean orientationChange) {
		GeoPoint centrePoint = GeoCalc.avgPoint(points);
		
		// only change centre point if different to current centre point
		if (orientationChange ||
			(centrePoint != null &&
			!centrePoint.equals(currentCentrePoint))) {
			view.getController().setCenter(centrePoint);
			currentCentrePoint = centrePoint;
			return true;
		}
		
		return false;
	}
	
	private static boolean setSpan(MapView view, ViewType type, GeoPoint[] points, boolean orientationChange) {
		
		int latSpan = GeoCalc.MIN_LAT_SPAN;
        int lonSpan = GeoCalc.MIN_LON_SPAN;

        // comfortably show all points
        latSpan = GeoCalc.getLatSpanMicro(points) * GeoCalc.SPAN_MULTIPLE;
        lonSpan = GeoCalc.getLonSpanMicro(points) * GeoCalc.SPAN_MULTIPLE;
        
        if (type == ViewType.SATELLITE) {
        	if (latSpan < 1000) {
        		latSpan = 1000;
        	}
        	if (lonSpan < 1000) {
        		lonSpan = 1000;
        	}
        }
        
        if (orientationChange ||
        	!sameSpan(latSpan, lonSpan)) {
        	currentSpanLat = latSpan;
        	currentSpanLon = lonSpan;
        	view.getController().zoomToSpan(latSpan, lonSpan);
        	return true;
        }
        
		return false;
	}
	
	private static boolean sameSpan(int latSpan, int lonSpan) {
		if (currentSpanLat == null ||
			currentSpanLon == null) {
			return false;
		}
		
		if (currentSpanLat == latSpan &&
			currentSpanLon == lonSpan) {
			return true;
		}
		
		return false;
	}
	
	private static List<GeoPoint> getPoints(List<PersonLocation> locations) {
		List<GeoPoint> points = new ArrayList<GeoPoint>();
		if (locations != null) {
			for (PersonLocation location : locations) 
			{
				points.add(location.getGeoPoint());
			}
		}
		return points;
	}
}
