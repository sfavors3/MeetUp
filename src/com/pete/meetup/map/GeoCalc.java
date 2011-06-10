package com.pete.meetup.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class GeoCalc {
    public static int MIN_LAT_SPAN = 15000;
    public static int MIN_LON_SPAN = 90000;

    public static int SPAN_MULTIPLE = 2;
    private static final int LON_MICRO_MAX = microDegrees(360);
    private static final int LON_MICRO_MID = microDegrees(180);

    /**
     * Zoom a mapView so that all given points are visible.
     *
     * @param mapView
     * @param pts
     */
    public static void zoomToPointsSpan(MapView mapView, GeoPoint[] pts,
    		boolean satellite, GeoPoint currentCentre) {
        if (!mapView.isShown()) {
            return;
        }

        GeoPoint centerPoint = GeoCalc.avgPoint(pts);
        MapController mapController = mapView.getController();
        mapController.setCenter(centerPoint);

        int latSpan = MIN_LAT_SPAN;
        int lonSpan = MIN_LON_SPAN;

        //if (!allSame(pts)) {
            // comfortably show all points
            latSpan = GeoCalc.getLatSpanMicro(pts) * SPAN_MULTIPLE;
            lonSpan = GeoCalc.getLonSpanMicro(pts) * SPAN_MULTIPLE;
        //}
        
        if (satellite) {
        	if (latSpan < 1000) {
        		latSpan = 1000;
        	}
        	if (lonSpan < 1000) {
        		lonSpan = 1000;
        	}
        }
        mapController.zoomToSpan(latSpan, lonSpan);
    }

    /*
    private static boolean allSame(GeoPoint[] pts) {
        boolean different = false;
        if (pts.length == 0) {
            return true;
        }

        GeoPoint first = pts[0];

        for (GeoPoint current : pts) {
            if (!compare(first, current)) {
                different = true;
                break;
            }
        }
        return !different;
    }
    
    private static boolean compare(GeoPoint first, GeoPoint second) {
    	if (first.getLatitudeE6() != second.getLatitudeE6()) {
    		return false;
    	}
    	
    	if (first.getLongitudeE6() != second.getLongitudeE6()) {
    		return false;
    	}
    	return true;
    }
    */

    public static GeoPoint avgPoint(GeoPoint[] pts) {
        int totalLat = 0;
        int totalPosLon = 0;
        for (GeoPoint pt : pts) {
            totalLat += pt.getLatitudeE6();
            totalPosLon += posLon(pt.getLongitudeE6());
        }
        int avgLat = totalLat / pts.length;
        int avgLon = normLon(totalPosLon / pts.length);
        return new GeoPoint(avgLat, avgLon);
    }

    public static int getLatSpanMicro(GeoPoint[] pts) {
        int maxLatSpan = 0;
        for (GeoPoint pt1 : pts) {
            for (GeoPoint pt2 : pts) {
                int currentLatSpan = getLatSpanMicro(pt1, pt2);
                if (currentLatSpan > maxLatSpan) {
                    maxLatSpan = currentLatSpan;
                }
            }
        }
        return maxLatSpan;
    }

    private static int getLatSpanMicro(GeoPoint pt1, GeoPoint pt2) {
        return getAngleSpanMicro(pt1.getLatitudeE6(), pt2.getLatitudeE6());
    }

    public static int getLonSpanMicro(GeoPoint[] pts) {
        int maxLonSpan = 0;
        for (GeoPoint pt1 : pts) {
            for (GeoPoint pt2 : pts) {
                int currentLonSpan = getLonSpanMicro(pt1, pt2);
                if (currentLonSpan > maxLonSpan) {
                    maxLonSpan = currentLonSpan;
                }
            }
        }
        return maxLonSpan;
    }

    public static int getLonSpanMicro(GeoPoint gp1, GeoPoint gp2) {
        int lon1 = posLon(gp1.getLongitudeE6());
        int lon2 = posLon(gp2.getLongitudeE6());
        return getAngleSpanMicro(lon1, lon2);
    }

    private static int getAngleSpanMicro(int a1, int a2) {
        return Math.abs(a1 - a2);
    }

    private static int posLon(int lon) {
        return (lon < 0) ? lon + LON_MICRO_MAX : lon;
    }

    private static int normLon(int lon) {
        return (lon > LON_MICRO_MID) ? lon - LON_MICRO_MAX : lon;
    }

    public static int microDegrees(double coord) {
        return (int)(coord * 1E6);
    }
}
