package com.pete.meetup.map;

import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.pete.meetup.location.PersonLocation;

public class LocationAlert {

	private List<PersonLocation> locations;
	private String message;
	private Context context;
	
	public LocationAlert(Context context, List<PersonLocation> locations,
			String message) {
		this.context	= context;
		this.locations 	= locations;
		this.message	= message;
	}
	
	public void display() {
		if (locations != null &&
			!locations.isEmpty()) {

			StringBuilder sb = new StringBuilder();
			
			int count = 0;
	
			for (PersonLocation location : locations) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(location.getName());
				count++;
			}
	
			sb.append((count == 1) ? " has " : " have ");
			sb.append(message);
	
			Toast.makeText(context, sb.toString(),
					Toast.LENGTH_LONG).show();
		}
	}
}
