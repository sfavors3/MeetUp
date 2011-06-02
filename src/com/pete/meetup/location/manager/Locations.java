package com.pete.meetup.location.manager;

import java.util.ArrayList;
import java.util.List;

import com.pete.meetup.location.PersonLocation;

public class Locations {
	
	private static  PersonLocation			myLocation				= null;
	private static	List<PersonLocation>	otherLocations 			= new ArrayList<PersonLocation>();
	private static	List<PersonLocation>	newLocations 			= new ArrayList<PersonLocation>();
	private static	List<PersonLocation> 	loggedInLocations 		= new ArrayList<PersonLocation>();
	private static	List<PersonLocation> 	loggedOutLocations 		= new ArrayList<PersonLocation>();
	private static	List<PersonLocation> 	loggedInNotifications 	= new ArrayList<PersonLocation>();
	private static	List<PersonLocation> 	loggedOutNotifications 	= new ArrayList<PersonLocation>();
	private static  List<PersonLocation>	removedLocations 		= new ArrayList<PersonLocation>();
	
	public static synchronized PersonLocation getMyLocation() {
		return myLocation;
	}
	
	public static synchronized void setMyLocation(PersonLocation location) {
		myLocation = location;
	}
	
	public static synchronized List<PersonLocation> getOtherLocations() {
		// return a copy as want the underlying list to contain all people
		// including removed and the returned list just the active people
		List<PersonLocation> locations = new ArrayList<PersonLocation>();
		
		if (otherLocations != null &&
			removedLocations != null) {
			
			// ignore removed locations
			for (PersonLocation location : otherLocations) {
				if (!isRemoved(location)) {
					locations.add(location);
				}
			}
		}
		
		return locations;
	}
	
	private static synchronized boolean isRemoved(PersonLocation location) {
		if (removedLocations != null) {
			return removedLocations.contains(location);
		}
		return false;
	}
	
	public static synchronized List<PersonLocation> getAllLocations() {
		List<PersonLocation> allLocations = new ArrayList<PersonLocation>();
		
		if (myLocation != null) {
			allLocations.add(myLocation);
		}
		
		List<PersonLocation> otherLocations = getOtherLocations();
		
		if (otherLocations != null) {
			allLocations.addAll(otherLocations);
		}
		
		return allLocations;
	}
	
	public static synchronized List<PersonLocation> getLoggedInLocations() {
		return loggedInLocations;
	}
	
	public static synchronized List<PersonLocation> getLoggedOutLocations() {
		return loggedOutLocations;
	}
	
	public static synchronized List<PersonLocation> getLoggedInNotifications() {
		return loggedInNotifications;
	}
	
	public static synchronized List<PersonLocation> getLoggedOutNotifications() {
		return loggedOutNotifications;
	}
	
	public static synchronized boolean hasNewLocations() {
		return (newLocations != null && !newLocations.isEmpty());
	}
	
	public static synchronized void addRemovedLocation(PersonLocation location) {
		if (location != null) {
			if (removedLocations == null) {
				removedLocations = new ArrayList<PersonLocation>();
			}
			removedLocations.add(location);
		}
	}
	
	public static synchronized void clearOtherLocations() {
		newLocations 			= new ArrayList<PersonLocation>();
		loggedInLocations		= new ArrayList<PersonLocation>();
		loggedOutLocations  	= new ArrayList<PersonLocation>();
		loggedInNotifications	= new ArrayList<PersonLocation>();
		loggedOutNotifications  = new ArrayList<PersonLocation>();
		otherLocations			= new ArrayList<PersonLocation>();
		removedLocations 		= new ArrayList<PersonLocation>();
	}
	
	public static synchronized void setOtherLocations(List<PersonLocation> locations) {
		newLocations 			= new ArrayList<PersonLocation>();
		loggedInLocations		= new ArrayList<PersonLocation>();
		loggedOutLocations  	= new ArrayList<PersonLocation>();
		loggedInNotifications	= new ArrayList<PersonLocation>();
		loggedOutNotifications  = new ArrayList<PersonLocation>();
		
		if (locations != null) {
			
			for (PersonLocation location : locations) {
				
				PersonLocation cachedLocation = getCachedLocation(location);
				
				// check if this is a newly added location
				if (cachedLocation == null) {
					newLocations.add(location);
				} 
				
				// check for login/logout state changes
				if (location.isLoggedIn()) {
					loggedInLocations.add(location);
					
					if (cachedLocation != null &&
						!cachedLocation.isLoggedIn()) {
						loggedInNotifications.add(location);
					}
				} else {
					// logged out
					loggedOutLocations.add(location);
					
					if (cachedLocation != null &&
						cachedLocation.isLoggedIn()) {
						loggedOutNotifications.add(location);
					}
				}
			}
		}
		
		otherLocations = locations;
	}
	
	
	private static synchronized PersonLocation getCachedLocation(PersonLocation location) {
		int index = otherLocations.indexOf(location);
		
		if (index != -1) {
			return otherLocations.get(index);
		}
		return null;
	}
	
	public static synchronized List<PersonLocation> getNewLocations() {
		return newLocations;
	}
	
	public static synchronized PersonLocation findLocationInList(List<PersonLocation> locations, PersonLocation location) {
		PersonLocation found = null;
		if (locations != null &&
			location != null) {
			int index = locations.indexOf(location);
			
			if (index != -1 ) {
				found = locations.get(index);
			}
		}
		return found;
	}
}
