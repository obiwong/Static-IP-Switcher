package com.htbest2000.staticipswitcher2;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.provider.Settings.System;
import android.util.Log;

public class Utils {

	public static void setIpStat(Context context, int stat) {
		System.putInt(context.getContentResolver(), System.WIFI_USE_STATIC_IP,
				stat);
	}

	public static int getIpStat(Context context) {
		return System.getInt(context.getContentResolver(),
				System.WIFI_USE_STATIC_IP, 0);
	}
	
	public static boolean isServiceRunning(Context context, Object clazz) {
		ActivityManager mgr = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> N = mgr.getRunningServices(Integer.MAX_VALUE);
		for (Iterator<RunningServiceInfo> it = N.iterator(); it.hasNext();) {
			if (it.getClass().equals(clazz)) {
				Log.i("=ht=", "got running service:" + clazz);
				return true;
			}
		}
		return false;
	}
	
	public static int getNewStat(int old) {
		int new_stat;
		if (0 == old) {
			new_stat = 1;
		}
		else if (1 == old) {
			new_stat = 0;
		}
		else {
			new_stat = -1;
		}
		return new_stat;
	}
 

}
