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
		Log.e(TAG, "****** AlarmReceiver RECEIVED INTENT *******");
		Log.e(TAG, "Dumping Intent:");
		Log.e(TAG, "    action=" + intent.getAction());
		Log.e(TAG, "    hasAlarm=" + intent.hasExtra(NiceAlarmManager.EXTRA_ALARM));
		Log.e(TAG, "    hasRawExtra=" + intent.hasExtra(NiceAlarmManager.EXTRA_ALARM_RAW));
		Log.e(TAG, "    systemTime=" + System.currentTimeMillis());
		Log.e(TAG, "Building alarm object...");

		Alarm alarm = null;
		// Grab the alarm from the intent. Since the remote AlarmManagerService
		// fills in the Intent to add some extra data, it must unparcel the
		// Alarm object. It throws a ClassNotFoundException when unparcelling.
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
			Log.e(TAG, "Alarm:");
			Log.e(TAG, "    id=" + alarm.id);
			Log.e(TAG, "    enabled=" + alarm.enabled);
			Log.e(TAG, "    label=" + alarm.label);
			Log.e(TAG, "    hour=" + alarm.hour);
			Log.e(TAG, "    minutes=" + alarm.minutes);
			Log.e(TAG, "    repeat=" + alarm.daysOfWeek.toString(context, true));
			Log.e(TAG, "    time=" + alarm.time);
			Log.e(TAG, "    alert=" + (alarm.alert != null ? alarm.alert.toString() : Alarm.ALARM_ALERT_SILENT));
			Log.e(TAG, "    vibrate=" + alarm.vibrate);
			Log.e(TAG, "    niceAlarm_enabled=" + alarm.niceAlarm_enabled);
			Log.e(TAG, "    niceAlarm_leadInSeconds=" + alarm.niceAlarm_leadInSeconds);
			Log.e(TAG, "    niceAlarm_alert=" + (alarm.niceAlarm_alert != null ? alarm.niceAlarm_alert.toString() : Alarm.ALARM_ALERT_SILENT));
		}

	}
}
