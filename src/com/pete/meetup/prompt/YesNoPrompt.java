package com.pete.meetup.prompt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class YesNoPrompt extends AbstractPrompt {

	private String title;
	private String message;
	
	public YesNoPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled, String title, String message) {
		super(id, listener, context, exitIfCancelled);
		this.title = title;
		this.message = message;
	}

	@Override
	public AlertDialog create() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).
		setMessage(message).
			setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   if (listener != null) {
						listener.onDismiss(YesNoPrompt.this.id, YesNoPrompt.this, ReturnCode.POSITIVE);
	        	   }
	           }
			}).
			setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   if (listener != null) {
							listener.onDismiss(YesNoPrompt.this.id, YesNoPrompt.this, ReturnCode.NEGATIVE);
		        	   }
		           }
			}).
			setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (listener != null) {
						listener.onDismiss(YesNoPrompt.this.id, YesNoPrompt.this, ReturnCode.CANCEL);
	        	   }
				}
			}).
			setCancelable(true);
		
		return builder.create();
	}
}
