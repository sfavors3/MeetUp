package com.pete.meetup.prompt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class EnterTextPrompt extends AbstractPrompt {

	private EditText 	input;
	private String 		text;
	private String 		message;
	private int 		maxChars;
	private AlertDialog dialog;
	
	public EnterTextPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled, String message, String text, int maxChars) {
		super(id, listener, context, exitIfCancelled);
		this.message 	= message;
		this.text 		= text;
		this.maxChars	= maxChars;
	}

	@Override
	public AlertDialog create() {
		input = new EditText(context);
		input.setText(text);
		
		input.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1,
					int paramInt2, int paramInt3) {
			}
			
			public void beforeTextChanged(CharSequence paramCharSequence,
					int paramInt1, int paramInt2, int paramInt3) {
			}
			
			public void afterTextChanged(Editable paramEditable) {
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(input.length() > 0);
			}
		});
		
		StringBuilder messageText = new StringBuilder();
		messageText.append(message);
		
		if (maxChars > 0) {
			messageText.append(" (max " + maxChars + " characters)");
			// limit text as we need to display it on the map overlay
			InputFilter[] filters = new InputFilter[] {new InputFilter.LengthFilter(maxChars)};
			input.setFilters(filters);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setMessage(messageText.toString())
	       .setCancelable(true)
	       .setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					hideKeyboard();
					if (listener != null) {
						listener.onDismiss(EnterTextPrompt.this.id, EnterTextPrompt.this, ReturnCode.CANCEL);
		        	}
				}
	       })
	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   text = input.getText().toString();
	        	   hideKeyboard();
	        	   if (listener != null) {
	        		   listener.onDismiss(EnterTextPrompt.this.id, EnterTextPrompt.this, ReturnCode.POSITIVE);
	        	   }
	           }
	       });
    	
    	builder.setView(input);
    	dialog = builder.create();
    	
    	dialog.show();
    	
    	dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(input.length() > 0);
    	
    	return dialog;
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		if (imm != null) {
			imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		}
	}
	
	public String getText() {
		return text;
	}
}
