package com.schneenet.android.nicealarm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NiceAlarmService extends Service
{

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
		// if (action is ACTION_ALARM_NICEALARM and nice alarm is enabled)
		//     get a full wake lock
		//     schedule broadcast of actual alarm for later
		//     start playing nice alarm tone
		//     send notification to system
		//     launch nice alarm ui
		// else if (action is ACTION_ALARM_ALERT)
		//     start playing alarm tone
		//     send notification to system
		//     launch alarm ui
		//     if (alarm is non-repeating)
		//         disable alarm
		// else if (action is ACTION_ALARM_SILENCE)
		//     stop playing tone
		//     clear notification
		//     broadcast to ui to shutdown
		//     release ALL wake locks
		//     stop service (self)
		// else if (action is ACTION_ALARM_SNOOZE)
		//     stop playing alarm tone
		//     update notification to snooze
		//     reschedule alarm for later:
		//          snooze alarm: later = now + TIMEOUT
		//          snooze nice alarm: later = actual alarm
		//     release ALL wake locks
		//     broadcast to ui to shutdown
		
		return Service.START_NOT_STICKY;
	}
	
}
