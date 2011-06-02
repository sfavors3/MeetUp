package com.pete.meetup.prompt;

import android.app.AlertDialog;
import android.content.Context;

public abstract class AbstractPrompt {
	protected int id;
	protected PromptDismissListener listener;
	protected Context context;
	protected boolean exitIfCancelled;
	
	public AbstractPrompt(int id, PromptDismissListener listener,
			Context context, boolean exitIfCancelled) {
		this.id = id;
		this.listener = listener;
		this.context = context;
		this.exitIfCancelled = exitIfCancelled;
	}
	
	public boolean isExitIfCancelled() {
		return exitIfCancelled;
	}
	
	public abstract AlertDialog create();
}
