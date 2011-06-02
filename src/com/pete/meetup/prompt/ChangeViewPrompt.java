package com.pete.meetup.prompt;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.pete.meetup.MeetUp.ViewType;

public class ChangeViewPrompt extends AbstractPrompt {
	
	private ViewType viewType;
	private ViewType currentViewType;
	
	public ChangeViewPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled, ViewType currentViewType) {
		super(id, listener, context, exitIfCancelled);
		this.currentViewType = currentViewType;
	}

	@Override
	public AlertDialog create() {
		List<CharSequence> items = new ArrayList<CharSequence>();
		
		int selectedIndex = -1;
		
		for (int i = 0; i < ViewType.values().length; i++) {
			items.add(ViewType.values()[i].toString().toLowerCase());
			if (ViewType.values()[i].equals(currentViewType)) {
				selectedIndex = i;
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Select map view:").
			setSingleChoiceItems(items.toArray(new CharSequence[]{}),
					selectedIndex,
					new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	viewType = ViewType.values()[item];
			    	if (listener != null) {
						listener.onDismiss(ChangeViewPrompt.this.id, ChangeViewPrompt.this, ReturnCode.POSITIVE);
		        	}
			    }
			}).
			setCancelable(true).
			setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (listener != null) {
						listener.onDismiss(ChangeViewPrompt.this.id, ChangeViewPrompt.this, ReturnCode.CANCEL);
		        	}
				}
			});
		
		return builder.create();
	}

	public ViewType getViewType() {
		return viewType;
	}
}
