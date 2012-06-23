/*
 * Copyright (C) 2007 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.schneenet.android.nicealarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import com.schneenet.android.nicealarm.data.Alarm;
import com.schneenet.android.nicealarm.data.NiceAlarmManager;
import com.schneenet.android.nicealarm.util.AlarmAlertWakeLock;
import com.schneenet.android.nicealarm.util.AsyncHandler;

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity. Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver
{

	private static final String TAG = "AlarmReceiver";

	@Override
	public void onReceive(final Context context, final Intent intent)
	{
		final PendingResult result = goAsync();
		final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
		wl.acquire();
		AsyncHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				handleIntent(context, intent);
				result.finish();
				wl.release();
			}
		});
	}

	private void handleIntent(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.e(TAG, "****** AlarmReceiver RECEIVED INTENT *******");
		Log.e(TAG, "Dumping Intent:");
		Log.e(TAG, "    action=" + action);
		Log.e(TAG, "    hasAlarm=" + intent.hasExtra(NiceAlarmManager.EXTRA_ALARM));
		Log.e(TAG, "    hasRawExtra=" + intent.hasExtra(NiceAlarmManager.EXTRA_ALARM_RAW));
		Log.e(TAG, "    systemTime=" + System.currentTimeMillis());

		
		if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action))
		{
			NiceAlarmManager.setNextAlarm(context);
		}
		else if (NiceAlarmManager.ACTION_ALARM_ALERT.equals(action) || NiceAlarmManager.ACTION_ALARM_NICEALARM.equals(action))
		{
			Log.e(TAG, "Building alarm object...");
			Alarm alarm = null;
			// Grab the alarm from the intent. Since the remote
			// AlarmManagerService
			// fills in the Intent to add some extra data, it must unparcel the
			// Alarm object. It throws a ClassNotFoundException when
			// unparcelling.
			// To avoid this, do the marshalling ourselves.
			final byte[] data = intent.getByteArrayExtra(NiceAlarmManager.EXTRA_ALARM_RAW);
			if (data != null)
			{
				Parcel in = Parcel.obtain();
				in.unmarshall(data, 0, data.length);
				in.setDataPosition(0);
				alarm = Alarm.CREATOR.createFromParcel(in);
			}
			if (alarm == null)
			{
				Log.e(TAG, "Failed to parse the alarm from the intent");
			}
			else
			{
				Intent serviceIntent = new Intent(context, NiceAlarmService.class);
				serviceIntent.setAction(action);
				serviceIntent.putExtra(NiceAlarmManager.EXTRA_ALARM, alarm);
				context.startService(serviceIntent);
			}
		}
		else
		{
			Log.e(TAG, "Invalid Action: " + action);
		}
	}
}
