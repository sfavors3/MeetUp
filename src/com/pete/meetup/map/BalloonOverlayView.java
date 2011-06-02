/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.pete.meetup.map;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pete.meetup.R;
import com.pete.meetup.location.PersonLocation;

public class BalloonOverlayView extends FrameLayout {

	private LinearLayout layout;
	private TextView titleView;
	private TextView snippetView;
	private boolean addedToMap;
	private boolean top;
	private PersonLocation location;
	private int overlayId;
	private LocationTapListener tapListener;
	//private double overlapLatitude = -1;
	//private String overlapPerson = null;
	private boolean valid = true;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(Context context,
			PersonLocation location,
			int balloonBottomOffset, boolean top,
			int overlayId, LocationTapListener tapListener) {
		super(context);
		
		this.top = top;
		this.location = location;
		this.overlayId = overlayId;
		this.tapListener = tapListener;
		//this.overlapLatitude = overlap;
		//this.overlapPerson = overlapPerson;

		setPadding(0, 0, 0, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		int resource = R.layout.balloon_overlay_rotated;
		
		if (top) {
			resource = R.layout.balloon_overlay;
		}
		
		View v = inflater.inflate(resource, layout);
		titleView = (TextView) v.findViewById(R.id.balloon_item_title);
		snippetView = (TextView) v.findViewById(R.id.balloon_item_snippet);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);
	}
	
	/*
	public String getOverlapPerson() {
		return overlapPerson;
	}
	*/

	public boolean isTop() {
		return top;
	}
	
	public void setLocation(PersonLocation location) {
		this.location = location;
	}
	
	public PersonLocation getLocation() {
		return this.location;
	}
	
	public int getOverlayId() {
		return overlayId;
	}
	
	public LocationTapListener getTapListener() {
		return tapListener;
	}
	
	/*
	public boolean isClosestOverlap(double overlap, String person) {
		if (person != null) {
			if (this.overlapLatitude == -1 ||
				this.overlapPerson == null) {
				storeOverlap(overlap, person);
				return true;
			} else {
				if (person.equals(this.overlapPerson)) {
					storeOverlap(overlap, person);
					return true;
				} else {
					if (overlap < this.overlapLatitude) {
						storeOverlap(overlap, person);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void resetClosestOverlap() {
		this.overlapLatitude = -1;
		this.overlapPerson = null;
	}
	
	private void storeOverlap(double overlap, String person) {
		this.overlapLatitude = overlap;
		this.overlapPerson = person;
	}
	
	public double getOverlap() {
		return this.overlapLatitude;
	}
	*/
	
	public void setContent(String title, String snippet) {
		if (title != null) {
			titleView.setVisibility(VISIBLE);
			titleView.setText(title);
		} else {
			titleView.setVisibility(GONE);
		}
		if (snippet != null) {
			snippetView.setVisibility(VISIBLE);
			snippetView.setText(snippet);
		} else {
			snippetView.setVisibility(GONE);
		}
	}
	
	public boolean isAddedToMap() {
		return addedToMap;
	}

	public void setAddedToMap(boolean addedToMap) {
		this.addedToMap = addedToMap;
	}
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof BalloonOverlayView)) {
			return false;
		}
		
		BalloonOverlayView pl = (BalloonOverlayView)o;
		
		if (!this.getLocation().equals(pl.getLocation())) {
			return false;
		}
		return true;
	}

	
	@Override
	public int hashCode() {
		int result = 23;
		
		if (this.getLocation() != null) {
			result += this.getLocation().hashCode();
		}
	
		return result;
	}
}
