package com.htbest2000.staticipswitcher2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigActivity extends Activity {
	private static final String TAG = "=ht=";
	private static final boolean DEBUG = true;
	
	public static final int DEVAULT_UPDATE_PERIOD = 0;
	
	// to notify widget to use new period
	public static final String ACTION_UPDATE_PERIOD = "com.htbest2000.staticipswitcher2.updateperiod";
	
	public static final String KEY_INTERVAL = "interval";
	
	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.config_activity);
		
		// shared preference
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		Button btn_ok = (Button)findViewById(R.id.config_btn_ok);
		btn_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// initialize ui
		EditText ui_time_edit = (EditText)findViewById(R.id.config_edit_minutes);
		ui_time_edit.setText( "" + mPrefs.getInt(ConfigActivity.KEY_INTERVAL, DEVAULT_UPDATE_PERIOD) );
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// get pref value
		int pref_time = mPrefs.getInt(KEY_INTERVAL, DEVAULT_UPDATE_PERIOD);
		
		// get ui value
		EditText ui_time_edit = (EditText)findViewById(R.id.config_edit_minutes);
		int ui_time = Integer.parseInt( ui_time_edit.getText().toString() );
		if ( ui_time < 0 || ui_time > 60*24*7) {
			Toast.makeText(ConfigActivity.this, R.string.period_limit_range, Toast.LENGTH_SHORT).show();
			return;
		}

		if (DEBUG) Log.i(TAG, "PREF val: " + pref_time + ", UI val: " + ui_time);

		// save new update period
		mPrefs.edit().putInt(KEY_INTERVAL, ui_time).commit();
		
		// notify widget to update new period.
		ConfigActivity.this.sendBroadcast(new Intent(ACTION_UPDATE_PERIOD).putExtra("config", true));
		Log.i(TAG, "sent ACTION_UPDATE_PERIOD");
		finish();
	}
	
}
