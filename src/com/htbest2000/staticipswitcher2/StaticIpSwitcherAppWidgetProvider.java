/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.htbest2000.staticipswitcher2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class StaticIpSwitcherAppWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "=ht=";
	private static final boolean DEBUG = true;

	private static final String SET_STATIC_IP_STAT  = "com.htbest2000.staticipswitcher2.reset_static_ip_stat";
	private static final String GET_SYSTEM_SETTINGS = "com.htbest2000.staticipswitcher2.get_system_settings";

	PendingIntent mPendingIntentCheckPeriod;

    private static final ComponentName THIS_APPWIDGET =
        new ComponentName("com.htbest2000.staticipswitcher2",
        				  "com.htbest2000.staticipswitcher2.StaticIpSwitcherAppWidgetProvider");

    // access ui data
	private static int mStaticIpStat = -1;
	private static Object mStaticIpStatLock = new Object();
	private static int getUiStaticIpStat() {
		synchronized (mStaticIpStatLock) {
			return mStaticIpStat;
		}
	}
	private static void setUiStaticIpStat( int stat ) {
		synchronized (mStaticIpStatLock) {
			mStaticIpStat = stat;
		}
	}
	// ---------------

	// create a new alarm.
	private void createAlarm(Context ctx, int minutes) {
		final int sec_per_min = 2;
		final int mic_seconds = 1000 * sec_per_min * minutes;
		final long next = SystemClock.elapsedRealtime() + mic_seconds;

		if (null == mPendingIntentCheckPeriod) {
			mPendingIntentCheckPeriod = PendingIntent.getBroadcast(ctx, 0,
					new Intent(GET_SYSTEM_SETTINGS, null, ctx,
							StaticIpSwitcherAppWidgetProvider.class), 0);
		}

		AlarmManager am = (AlarmManager) ctx
				.getSystemService(Context.ALARM_SERVICE);

		Log.i(TAG, "cancel old alarm");
		am.cancel(mPendingIntentCheckPeriod);
		
		// 0 means don't auto update forever
		if (0 == minutes) {
			return;
		}
		
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, next, mic_seconds,
				mPendingIntentCheckPeriod);
		Log.i(TAG, "createAlarm: " + minutes + ", seconds: " + mic_seconds);
	}
	
	private void cancelAlarm(Context ctx) {
		Log.i(TAG, "cancelAlarm()");
		if (null == mPendingIntentCheckPeriod) {
			mPendingIntentCheckPeriod = PendingIntent.getBroadcast(ctx, 0,
					new Intent(GET_SYSTEM_SETTINGS, null, ctx,
							StaticIpSwitcherAppWidgetProvider.class), 0);
		}
		AlarmManager am = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
		am.cancel(mPendingIntentCheckPeriod);
	}

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onUpdate");
        if (-1 == getUiStaticIpStat()) {
        	setUiStaticIpStat( Utils.getIpStat(context) );
        }

        RemoteViews view = updateUi( context );
        bindActions( context, view );
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            appWidgetManager.updateAppWidget(appWidgetIds[i], view);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    	super.onDeleted(context, appWidgetIds);
    	if (DEBUG) Log.i(TAG, "onDeleted");
//        final int N = appWidgetIds.length;
//        for (int i=0; i<N; i++) {
//            int appWidgetId = appWidgetIds[i];
//        }
    }

    @Override
    public void onEnabled(final Context ctx) {
    	if (DEBUG) Log.i(TAG, "onEnabled");
    	
    	// get prefered value
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		final int period = prefs.getInt(ConfigActivity.KEY_INTERVAL, 30); // 30 is default value

		// set alarm
    	createAlarm(ctx, period);
    }

    @Override
    public void onDisabled(Context ctx) {
    	if (DEBUG) Log.i(TAG, "onDisable");
    	cancelAlarm( ctx );
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String act = intent.getAction();
		if (DEBUG) Log.i(TAG, "got broadcast: " + act.toString());
		
		if (act.equals(SET_STATIC_IP_STAT)) {
			int old_stat = getUiStaticIpStat();
			int new_stat = Utils.getNewStat( old_stat );

			if( 0 == new_stat || 1 == new_stat) {
				// write new stat into system settings
				Utils.setIpStat( context,  new_stat );
				setUiStaticIpStat( new_stat );
				
				// map system settings to ui
				RemoteViews rview = updateUi(context);
				final AppWidgetManager awm = AppWidgetManager.getInstance(context);
				awm.updateAppWidget(THIS_APPWIDGET, rview);
				if (DEBUG) Log.i("=ht=", "change ip stat to: " + new_stat + ", from: " + old_stat);
			}
		}
		else if (act.equals(GET_SYSTEM_SETTINGS)) {
			int sys_stat = Utils.getIpStat(context);
			Log.i(TAG, "detect sys stat: " + sys_stat + ", but ui is: " + getUiStaticIpStat());
			
			if (sys_stat != getUiStaticIpStat()) {
				setUiStaticIpStat( sys_stat );
				RemoteViews rview = updateUi(context);
				final AppWidgetManager awm = AppWidgetManager.getInstance(context);
				awm.updateAppWidget(THIS_APPWIDGET, rview);
			}
		}
		else if (act.equals(ConfigActivity.ACTION_UPDATE_PERIOD)) {
				// ConfigActivity make something changed, so need to re-set the
				// alarm.
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);
				int interval = prefs.getInt(ConfigActivity.KEY_INTERVAL, 30);
				createAlarm(context, interval);
		}
	}

	private static void bindActions(Context ctx, RemoteViews views) {
        Intent launchIntent = new Intent(SET_STATIC_IP_STAT);
        launchIntent.setClass(ctx, StaticIpSwitcherAppWidgetProvider.class);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.btn_switch, pi);
	}

	private static RemoteViews updateUi(Context ctx) {
    	String title;
		if (0 == getUiStaticIpStat()) {
			title = "static IP: OFF";
		}
		else {
			title = "static IP: ON";
		}

        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.appwidget_provider);
        views.setTextViewText(R.id.btn_switch, title);
        return views;
	}

}


