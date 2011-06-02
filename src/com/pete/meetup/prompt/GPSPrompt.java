package com.pete.meetup.prompt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class GPSPrompt extends AbstractPrompt {
	
	public GPSPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled) {
		super(id, listener, context, exitIfCancelled);
	}

	@Override
	public AlertDialog create() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setMessage("Show location settings")
    	   //.setTitle("Please enable GPS and/or Network location settings.")
    	   .setTitle("Please enable GPS location settings.")
	       .setCancelable(true)
	       .setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (listener != null) {
						listener.onDismiss(GPSPrompt.this.id, GPSPrompt.this, ReturnCode.CANCEL);
					}
				}
		   })
	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   Intent intent = new Intent(
	       				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        	   
	        	   GPSPrompt.this.context.startActivity(intent);
	        	   
	        	   if (listener != null) {
						listener.onDismiss(GPSPrompt.this.id, GPSPrompt.this, ReturnCode.POSITIVE);
	        	   }
	           }
	       });
    	
    	return builder.create();
	}
}
