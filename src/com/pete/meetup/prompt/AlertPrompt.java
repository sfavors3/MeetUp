package com.pete.meetup.prompt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertPrompt extends AbstractPrompt {
	
	private String title;
	private String message;
	
	public AlertPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled, String title, String message) {
		super(id, listener, context, exitIfCancelled);
		this.title = title;
		this.message = message;
	}

	@Override
	public AlertDialog create() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setMessage(message)
    	   //.setTitle("Please enable GPS and/or Network location settings.")
    	   .setTitle(title)
	       .setCancelable(true)
	       .setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (listener != null) {
						listener.onDismiss(AlertPrompt.this.id, AlertPrompt.this, ReturnCode.CANCEL);
					}
				}
		   })
	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   if (listener != null) {
						listener.onDismiss(AlertPrompt.this.id, AlertPrompt.this, ReturnCode.POSITIVE);
	        	   }
	           }
	       });
    	
    	return builder.create();
	}
}
