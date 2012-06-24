package com.schneenet.android.nicealarm.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.schneenet.android.nicealarm.data.Alarm;
import com.schneenet.android.nicealarm.data.NiceAlarmManager;

public class AlarmActivity extends Activity
{

	protected Alarm mAlarm;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mAlarm = getIntent().getParcelableExtra(NiceAlarmManager.EXTRA_ALARM);

		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		
		updateLayout();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(NiceAlarmManager.ACTION_ALARMUI_FINISH);
		registerReceiver(mController, filter);
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(mController);
	}
	
	public void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		mAlarm = intent.getParcelableExtra(NiceAlarmManager.EXTRA_ALARM);
		updateLayout();
	}
	
	private void updateLayout()
	{
		// TODO Layout for Alarm alert activity
	}

	private BroadcastReceiver mController = new BroadcastReceiver()
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			Log.v("AlarmActivityController", "Received intent: " + action);
			if (NiceAlarmManager.ACTION_ALARMUI_FINISH.equals(action))
			{
				finish();
			}
		}

	};

}
