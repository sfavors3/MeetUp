package com.pete.meetup.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class PackageUtils {

	public static boolean isIntentAvailable(Context context, String action, Uri uri) {
		try {
		    PackageManager packageManager = context.getPackageManager();
		    Intent intent = new Intent(action, uri);
		    List<ResolveInfo> list =
		            packageManager.queryIntentActivities(intent,
		                    PackageManager.MATCH_DEFAULT_ONLY);
		    return list.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}
}
