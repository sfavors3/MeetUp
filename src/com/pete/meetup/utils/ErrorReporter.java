package com.pete.meetup.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.pete.meetup.MeetUp;
import com.pete.meetup.mail.MailHandler;

public class ErrorReporter {
	
	private static final String NEW_LINE = "\n";
	
	private Context context;
	private boolean writeToFile;
	
	public ErrorReporter(Context context, boolean writeToFile) {
		this.context = context;
		this.writeToFile = writeToFile;
	}
	
	public void reportError(Thread t, Throwable e) {
		
		Map<String, String> params = generatePlatformParams(context);
		
		// send crash report to email
		try {
			sendEmail(createMessage(params, t, e));
		} catch (Exception ex) {
			Log.e(MeetUp.LOG_TAG, "Failed to write crash report to email", e);
		}
		
		// write to file if flag set
		if (writeToFile) {
			try {
				//writeToFile(createMessage(params, t, e));
			} catch (Exception ex) {
				Log.e(MeetUp.LOG_TAG, "Failed to write crash report to file, e");
			}
		}
	}

	private Map<String, String> generatePlatformParams(Context context)
	{
		Map<String, String> params = new HashMap<String, String>();
	
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			params.put("Version", pi.versionName);
			params.put("Package", pi.packageName);
		} catch (NameNotFoundException e) {
			// swallow
		}
		
		params.put("Phone Model", android.os.Build.MODEL);
		params.put("Android Version", android.os.Build.VERSION.RELEASE);
		params.put("Board", android.os.Build.BOARD);
		params.put("Brand", android.os.Build.BRAND);
		params.put("Device", android.os.Build.DEVICE);
		params.put("Display", android.os.Build.DISPLAY);
		params.put("Finger Print", android.os.Build.FINGERPRINT);
		params.put("Host", android.os.Build.HOST);
		params.put("ID", android.os.Build.ID);
		params.put("Model", android.os.Build.MODEL);
		params.put("Product", android.os.Build.PRODUCT);
		params.put("Tags", android.os.Build.TAGS);
		params.put("Time", Long.toString(android.os.Build.TIME));
		params.put("Type", android.os.Build.TYPE);
		params.put("User", android.os.Build.USER);
		params.put("Total Internal memory", Long.toString(getTotalInternalMemorySize()));
		params.put("Available Internal memory", Long.toString(getAvailableInternalMemorySize()));
	
		return params;
	}

	public String createMessage(Map<String, String> params, Thread t, Throwable e)
	{
		StringBuilder sb = new StringBuilder();
		
		Date now = new Date();
		sb.append("Error Report collected on : " + now.toString());
		sb.append(NEW_LINE);
		
		for (Map.Entry<String, String> param : params.entrySet()) {
			sb.append(param.getKey());
			sb.append(" : ");
			sb.append(param.getValue());
			sb.append(NEW_LINE);
		}

		sb.append(NEW_LINE);
		sb.append("Stack:");
		sb.append(NEW_LINE);
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		sb.append(result.toString());

		sb.append(NEW_LINE);
		sb.append("Cause:");
		sb.append(NEW_LINE);
		
		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable cause = e.getCause();
		
		while (cause != null)
		{
			cause.printStackTrace( printWriter );
			sb.append(result.toString());
			cause = cause.getCause();
		}
		
		return sb.toString();
	}
	
	/*
	private void writeToFile(String content) {
        try {
        	File root = Environment.getExternalStorageDirectory();
        	
           	SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        	String filename = formatter.format(new Date()) + ".stacktrace";
        	
            BufferedWriter bos = new BufferedWriter(new FileWriter(
            		new File(root, filename)));
            bos.write(content);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

	private void sendEmail(String content) throws Exception
	{
		MailHandler handler = new MailHandler("petehendry@hotmail.co.uk",
				"hotmailpompey");
		
		handler.send("petehendry@hotmail.co.uk",
				new String[] {"petehendry@hotmail.co.uk"},
				"MeetUp Crash Report",
				content);
	}
	
	private long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	private long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}
}