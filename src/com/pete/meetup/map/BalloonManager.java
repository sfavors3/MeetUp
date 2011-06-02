package com.pete.meetup.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.pete.meetup.location.PersonLocation;

public class BalloonManager {
	
	private MapView mapView;
	private Map<String, BalloonOverlayView> views = new HashMap<String, BalloonOverlayView>();
	
	public BalloonManager(MapView mapView) {
		this.mapView = mapView;
	}
	
	public void setLocations(Collection<PersonLocation> locations, int overlayId, LocationTapListener tapListener, boolean top) {
		
		// mark all views as invalid
		for (BalloonOverlayView view : views.values()) {
			if (view.getOverlayId() == overlayId) {
				view.setValid(false);
			}
		}
		
		if (locations != null) {
			for (final PersonLocation location : locations) {
				BalloonOverlayView view = views.get(location.getPerson());
				
				if (view == null) {
					view = createBalloon(location,
							top, tapListener, overlayId);
				}
				
				view.setValid(true);
				
				views.put(location.getPerson(), view);
			}
		}
		
		// remove all invalid views
		for(Iterator<BalloonOverlayView> itr = views.values().iterator(); itr.hasNext();) { 
			BalloonOverlayView view = itr.next(); 
			if (view.getOverlayId() == overlayId &&
				!view.isValid()) {
				mapView.removeView(view);
				itr.remove();
			}
		}
	}
	
	private BalloonOverlayView getView(PersonLocation location) {
		BalloonOverlayView view = null;
		if (location != null) {
			view = views.get(location.getPerson());
		}
		return view;
	}

	private void updatePosition(BalloonOverlayView view, GeoPoint point) {
		int alignment = view.isTop() ? MapView.LayoutParams.BOTTOM_CENTER :
    		MapView.LayoutParams.TOP | MapView.LayoutParams.CENTER_HORIZONTAL;
    	
    	MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
				alignment);
    	
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		view.setVisibility(View.VISIBLE);
		
    	if (!view.isAddedToMap()) {
    		mapView.addView(view, params);
    	} else {
    		view.setLayoutParams(params);
    	}
    	view.setAddedToMap(true);
	}
	
	public void updateDisplay(PersonLocation location, GeoPoint point,
			String title, String snippet) {
		BalloonOverlayView view = getView(location);
		
		if (view != null) {
			view.setLocation(location);
			view.setContent(title, snippet);
			updatePosition(view, point);
			
			// could just hardcode remembered to be upsidedown!
			// check if two view overlapping
			/*
			BalloonOverlayView overlappingView = getOverlappingView(view);
			
			if (overlappingView != null) {
        		
				Log.i(MeetUp.LOG_TAG, "overlapping balloons");
				//still a problem when locations are the same - if have to flip round then problems
        		// need to handle case where locations are the same - in which case northermost
        		BalloonOverlayView southernMost = getSouthernMost(view, overlappingView);
        		BalloonOverlayView northernMost = southernMost.equals(view) ? overlappingView : view;
        		
        		double overlapLatitude = getLatitudeDifference(southernMost, northernMost);
        		
    			// only swap if bubble on top
        		if (southernMost.isTop() &&
        			southernMost.isClosestOverlap(overlapLatitude, northernMost.getLocation().getPerson())) {
        			Log.i(MeetUp.LOG_TAG, "moving " + southernMost.getLocation().getName() +
        					" to bottom");
        			changeOrientation(southernMost, false);
        		}

        		// ensure northernmost bubble is on top
        		if (!northernMost.isTop() &&
        			 northernMost.isClosestOverlap(overlapLatitude, southernMost.getLocation().getPerson())) {
        			Log.i(MeetUp.LOG_TAG, "moving " + northernMost.getLocation().getName() +
					" to top");
        			changeOrientation(northernMost, true);
        		}
        	} else {
        		view.resetClosestOverlap();
        	}
        	*/
		}
	}
	
	/*
	private BalloonOverlayView changeOrientation(BalloonOverlayView view, boolean top) {
		MapView.LayoutParams params = (MapView.LayoutParams)view.getLayoutParams();
		GeoPoint viewPoint = params.point;
		
		mapView.removeView(view);
		view = createBalloon(view.getLocation(), top,
				view.getTapListener(), view.getOverlayId(),
				view.getOverlap(), view.getOverlapPerson());
		views.put(view.getLocation().getPerson(), view);
		
		updatePosition(view, viewPoint);
		return view;
	}
	
	private BalloonOverlayView getSouthernMost(BalloonOverlayView view1, BalloonOverlayView view2) {
		BalloonOverlayView view = null;
		
		PersonLocation loc1 = view1.getLocation();
		PersonLocation loc2 = view2.getLocation();
		
		// return location with least location
		if (loc1 != null &&
			loc2 != null) {
			view = loc1.getLatitudeDouble() < loc2.getLatitudeDouble() ? view1 : view2;
			Log.i(MeetUp.LOG_TAG, view.getLocation().getName() + " is southernmost " +
					view.getLocation().getLatitudeDouble());
		}

		return view;
	}
	
	private BalloonOverlayView getOverlappingView(BalloonOverlayView current) {
		Rect currentRect = new Rect();
		current.getGlobalVisibleRect(currentRect);
		
		double difference = -1;
		BalloonOverlayView closest = null;
    	 
		if (current.isAddedToMap()) {
			if (views != null) {
				for (BalloonOverlayView view : views.values()) {
					if (view != current &&
						view.isAddedToMap()) {
						Rect viewRect = new Rect();
						view.getGlobalVisibleRect(viewRect);
						
						if (currentRect.intersect(viewRect)) {
							double latitude = getLatitudeDifference(current, view);
							if (difference == -1 ||
									latitude < difference) {
								difference = latitude;
								closest = view;
							}
						}
					}
				}
			}
		}
		return closest;
	}
	
	private double getLatitudeDifference(BalloonOverlayView view1, BalloonOverlayView view2) {
		double lat1 = view1.getLocation().getLatitudeDouble();
		double lat2 = view2.getLocation().getLatitudeDouble();
		
		if (lat1 > lat2) {
			return lat1 - lat2;
		} else {
			return lat2 - lat1;
		}
	}
	*/
	
	private BalloonOverlayView createBalloon(final PersonLocation location, boolean top,
			final LocationTapListener tapListener, int overlayId) {
		
		int offset = top ? 20 : 0;
		
		BalloonOverlayView view = new BalloonOverlayView(mapView.getContext(), location,
				offset, top, overlayId, tapListener);
		
		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (tapListener != null) {
					if (v instanceof BalloonOverlayView) {
						BalloonOverlayView view = (BalloonOverlayView)v;
						
						PersonLocation location = view.getLocation();
						
						if (location != null) {
							tapListener.onLocationTapped(location);
						}
					}
				}
			}
		});
		
		return view;
	}
}
