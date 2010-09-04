package com.htbest2000.staticipswitcher2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			// write prefs to "just_boot=true"
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
			sp.edit().putBoolean("just_boot", true).commit();
			Log.i("=ht=", "I got BOOT COMPLETE");
		}

	}

}
