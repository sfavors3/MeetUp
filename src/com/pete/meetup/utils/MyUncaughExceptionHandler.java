package com.pete.meetup.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.pete.meetup.MeetUp;

/**
 * @author Pete
 *
 */
public class MyUncaughExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultHandler;
	private Context context;

	public MyUncaughExceptionHandler(Context context) {
		this.context = context;
		this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	/**
	 * @param thread
	 * @param ex
	 */
	public void uncaughtException(Thread thread, Throwable ex) {

		// send event to flurry
		try {
			FlurryAgent.onEvent("UNCAUGHT_EXCEPTION");
		} catch (Exception e) {
			Log.e(MeetUp.LOG_TAG, "Failed to log unacught error to flurry");
		}

		
		try {
			ErrorReporter reporter = new ErrorReporter(context,
					false);//AppConfig.getInstance().isTestPhone());

			reporter.reportError(thread, ex);
		} catch (Exception e) {
			Log.e(MeetUp.LOG_TAG, "Failed to record crash report.");
		}

		// rethrow to cause application to exit - not sure this is possible
		defaultHandler.uncaughtException(thread, ex);	
	}
}
