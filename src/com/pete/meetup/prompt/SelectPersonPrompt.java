package com.pete.meetup.prompt;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.pete.meetup.MeetUp.CentreMode;
import com.pete.meetup.location.PersonLocation;
import com.pete.meetup.utils.TextFormatter;

public class SelectPersonPrompt extends AbstractPrompt {
	
	private static final String MAP_CENTRE = "show everyone";
	private static final String MAP_DISABLE = "disable automatic centre/zoom";

	private List<PersonLocation> locations;
	private PersonLocation selectedPerson;
	private TextFormatter nameFormatter;
	private CentreMode centreMode;
	
	public SelectPersonPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled, List<PersonLocation> locations, TextFormatter nameFormatter) {
		super(id, listener, context, exitIfCancelled);
		this.locations = locations;
		this.nameFormatter = nameFormatter;
	}

	@Override
	public AlertDialog create() {
		List<CharSequence> items = new ArrayList<CharSequence>();
		
		if (locations != null &&
			locations.size() > 0) {
			
			for (PersonLocation location : locations) {
				items.add(location.getNameForDisplay(nameFormatter));
			}
		}
		
		items.add(MAP_CENTRE);
		final int indexMapCentreEveryone = items.size() - 1;
		items.add(MAP_DISABLE);
		final int indexMapCentreDisble = items.size() - 1;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Select person to zoom/centre on:").
			setItems(items.toArray(new CharSequence[]{}), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	if (item == indexMapCentreEveryone) {
			    		centreMode = CentreMode.EVERYONE;
			    	} else if (item == indexMapCentreDisble) {
			    		centreMode = CentreMode.DISABLED;
			    	} else if (locations != null) {
			    		selectedPerson = locations.get(item);
			    		centreMode = CentreMode.SINGLE_PERSON;
			    	}
			    	if (listener != null) {
						listener.onDismiss(SelectPersonPrompt.this.id, SelectPersonPrompt.this, ReturnCode.POSITIVE);
		        	}
			    }
			}).
			setCancelable(true).
			setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (listener != null) {
						listener.onDismiss(SelectPersonPrompt.this.id, SelectPersonPrompt.this, ReturnCode.CANCEL);
		        	}
				}
			});
		
		return builder.create();
	}

	public PersonLocation getSelectedPerson() {
		return selectedPerson;
	}
	
	public CentreMode getCentreMode() {
		return centreMode;
	}
}
