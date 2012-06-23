package com.schneenet.android.nicealarm.service;

import com.schneenet.android.nicealarm.data.Alarm;
import com.schneenet.android.nicealarm.data.NiceAlarmManager;
import com.schneenet.android.nicealarm.util.AlarmAlertWakeLock;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NiceAlarmService extends Service
{
	
	public static final String TAG = "NiceAlarmService";

	// Define Service states, used when the service is started and we receive
	public static final int STATE_DEFAULT = 0;
	public static final int STATE_NICE_PLAYING = 1;
	public static final int STATE_ALARM_PLAYING = 2;
	public static final int STATE_SNOOZING = 3;
	private int mCurrentState = STATE_DEFAULT;
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// We are startup/broadcast based
		return null;
	}
	
	public void onCreate()
	{
		super.onCreate();
		// TODO Setup the service resources
	}
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Handle an alarm start playing
		
		// Intent ACTION will be one of:
		// NiceAlarmManager.
		//     ACTION_ALARM_ALERT		-- Normal alarm alert
		//     ACTION_ALARM_NICEALARM	-- Nice Alarm
		//     ACTION_ALARM_SILENCE		-- Cancel Nice/Normal alarm (user presses "Dismiss", or otherwise interrupted by phone call, etc.)
		//     ACTION_ALARM_SNOOZE		-- Snooze Nice/Normal alarm (user presses "Snooze")
		
		// Follow this basic flow:

		// try to extract alarm from Intent
		Alarm alarm = intent.getParcelableExtra(NiceAlarmManager.EXTRA_ALARM);
		if (alarm == null)
		{
			Log.e(TAG, "Failed to get Alarm from Intent.");
			return Service.START_NOT_STICKY;
		}
		
		// XXX DEBUGGING: Log the alarm 
		Log.e(TAG, "Alarm:");
		Log.e(TAG, "    id=" + alarm.id);
		Log.e(TAG, "    enabled=" + alarm.enabled);
		Log.e(TAG, "    label=" + alarm.label);
		Log.e(TAG, "    hour=" + alarm.hour);
		Log.e(TAG, "    minutes=" + alarm.minutes);
		Log.e(TAG, "    repeat=" + alarm.daysOfWeek.toString(this, true));
		Log.e(TAG, "    time=" + alarm.time);
		Log.e(TAG, "    alert=" + (alarm.alert != null ? alarm.alert.toString() : Alarm.ALARM_ALERT_SILENT));
		Log.e(TAG, "    vibrate=" + alarm.vibrate);
		Log.e(TAG, "    niceAlarm_enabled=" + alarm.niceAlarm_enabled);
		Log.e(TAG, "    niceAlarm_leadInSeconds=" + alarm.niceAlarm_leadInSeconds);
		Log.e(TAG, "    niceAlarm_alert=" + (alarm.niceAlarm_alert != null ? alarm.niceAlarm_alert.toString() : Alarm.ALARM_ALERT_SILENT));

		String action = intent.getAction();

		// if (action is ACTION_ALARM_NICEALARM and nice alarm is enabled)
		if (NiceAlarmManager.ACTION_ALARM_NICEALARM.equals(action) && alarm.niceAlarm_enabled)
		{
			//     get a full wake lock
			AlarmAlertWakeLock.acquireCpuWakeLock(this);
			
			//     schedule broadcast of actual alarm for later
			
			
			//     start playing nice alarm tone
			
			
			//     send notification to system
			
			
			//     launch nice alarm ui
			launchUi(alarm, true);
			
		}
		// else if (action is ACTION_ALARM_ALERT)
		else if (NiceAlarmManager.ACTION_ALARM_ALERT.equals(action))
		{
			//     start playing alarm tone
			//     send notification to system
			
			
			//     launch alarm ui
			launchUi(alarm, false);
			
			//     if (alarm is non-repeating)
			if (!alarm.daysOfWeek.isRepeatSet())
			{
				// disable alarm
				NiceAlarmManager.enableAlarm(this, alarm.id, false);
			}
			
		}
		// else if (action is ACTION_ALARM_SILENCE)
		else if (NiceAlarmManager.ACTION_ALARM_SILENCE.equals(action))
		{
			//     stop playing tone
			//     clear notification
			//     broadcast to ui to shutdown
			//     release ALL wake locks
			//     stop service (self)
		}
		// else if (action is ACTION_ALARM_SNOOZE)
		else if (NiceAlarmManager.ACTION_ALARM_SNOOZE.equals(action))
		{
			//     stop playing alarm tone
			//     update notification to snooze
			//     reschedule alarm for later:
			//          snooze alarm: later = now + TIMEOUT
			//          snooze nice alarm: later = actual alarm
			//     release ALL wake locks
			//     broadcast to ui to shutdown
		}
		else
		{
			Log.e(TAG, "Invalid action: " + action);
		}
		return Service.START_NOT_STICKY;
	}
	
	private void launchUi(Alarm alarm, boolean niceAlarm)
	{
		Intent intent = new Intent(NiceAlarmManager.ACTION_ALARM_UI);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		intent.putExtra(NiceAlarmManager.EXTRA_UI_NICEALARM, niceAlarm);
		startActivity(intent);
	}
	
}
