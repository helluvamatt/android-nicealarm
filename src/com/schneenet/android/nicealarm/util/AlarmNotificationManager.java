package com.schneenet.android.nicealarm.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.schneenet.android.nicealarm.R;
import com.schneenet.android.nicealarm.data.Alarm;
import com.schneenet.android.nicealarm.data.NiceAlarmManager;
import com.schneenet.android.nicealarm.service.NiceAlarmService;

public class AlarmNotificationManager
{
	// TODO Notifications
	// 1a. Alarm Sounding
	// 1b. Nice Alarm Sounding
	// 2. Snoozed Alarm
	// 3. Alarm Timed Out (Silenced after a period of time)

	// XXX Notification icons
	public static final int ICON = 0;

	public static void sendAlertNotification(Context context, Alarm alarm)
	{
		// Actual Alert time
		long alert = NiceAlarmManager.calculateAlarmAlert(alarm);

		// PendingIntent for bringing the FullScreen Alarm UI to the front (so
		// it can be dismissed, snoozed, etc.)
		Intent intent = NiceAlarmService.getLaunchIntent(alarm, System.currentTimeMillis() < alert);
		PendingIntent contentIntent = PendingIntent.getActivity(context, alarm.id, intent, 0);

		// Get basic notification
		Notification n = new Notification(ICON, context.getText(R.string.alert_title), alert);
		n.setLatestEventInfo(context, context.getText(R.string.alert_title), alarm.getLabelOrDefault(context), contentIntent);

		// This can vibrate
		if (alarm.vibrate)
		{
			n.defaults |= Notification.DEFAULT_VIBRATE;
		}

		// We are ongoing until the timeout, or user interference
		n.flags |= Notification.FLAG_ONGOING_EVENT;

		// Send it
		getNotificationManager(context).notify(alarm.id, n);

	}

	public static void sendSilencedNotification(Context context, Alarm alarm)
	{
		// Build notification
		Notification n = new Notification(ICON, context.getText(R.string.alert_title_silenced), alarm.time);
		n.setLatestEventInfo(context, context.getText(R.string.alert_title_silenced), alarm.getLabelOrDefault(context), null);
		n.flags |= Notification.FLAG_AUTO_CANCEL;

		// Resend as non-ongoing
		NotificationManager nm = getNotificationManager(context);
		nm.cancel(alarm.id);
		nm.notify(alarm.id, n);
	}

	public static void sendSnoozingNotification(Context context, Alarm alarm)
	{
		// Actual Alert time
		long alert = NiceAlarmManager.calculateAlarmAlert(alarm);

		// PendingIntent for bringing the FullScreen Alarm UI ot the front so it
		// can be dismissed, etc.)
		Intent intent = NiceAlarmService.getLaunchIntent(alarm, System.currentTimeMillis() < alert);
		PendingIntent contentIntent = PendingIntent.getActivity(context, alarm.id, intent, 0);

		// Get basic notification
		Notification n = new Notification(ICON, context.getText(R.string.alert_title_snooze), alert);
		n.setLatestEventInfo(context, context.getText(R.string.alert_title_snooze), alarm.getLabelOrDefault(context), contentIntent);

		// Send it
		getNotificationManager(context).notify(alarm.id, n);
	}

	public static NotificationManager getNotificationManager(Context context)
	{
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

}
