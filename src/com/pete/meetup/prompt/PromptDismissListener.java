package com.pete.meetup.prompt;

public interface PromptDismissListener {
	public void onDismiss(int id, AbstractPrompt prompt, ReturnCode code);
}
