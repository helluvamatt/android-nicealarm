package com.schneenet.android.nicealarm.data;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.text.format.DateFormat;
import android.util.Log;
import com.schneenet.android.nicealarm.R;
import com.schneenet.android.nicealarm.data.Alarm.DaysOfWeek;

public class NiceAlarmManager
{

	// Debug Tag
	private static final String TAG = "NiceAlarmManager";

	// This Action is for editing alarms
	public static final String ACTION_EDIT_ALARM = "com.schneenet.android.nicealarm.ACTION_EDIT_ALARM";

	// This Action is for an Alarm going off
	public static final String ACTION_ALARM_ALERT = "com.schneenet.android.nicealarm.ACTION_ALARM_ALERT";

	// This Action is for an Alarm's NiceAlarm starting
	public static final String ACTION_ALARM_NICEALARM = "com.schneenet.android.nicealarm.ACTION_ALARM_NICEALARM";

	// This Action is broadcast when the alarm is silenced either by the user or
	// after a timeout of 30 minutes
	public static final String ACTION_ALARM_SILENCE = "com.schneenet.android.nicealarm.ACTION_ALARM_SILENCE";

	// This Action is broadcast when the alarm should be snoozed
	public static final String ACTION_ALARM_SNOOZE = "com.schneenet.android.nicealarm.ACTION_ALARM_SNOOZE";

	// This Action is for launching the (Nice) Alarm UI
	public static final String ACTION_ALARM_UI = "com.schneenet.android.nicealarm.ACTION_ALARM_UI";

	// This Broadcast Action is for shutting down the UI
	public static final String ACTION_ALARMUI_FINISH = "com.schneenet.android.nicealarm.ACTION_ALARMUI_FINISH";

	// This string is used when passing an Alarm object through an intent.
	public static final String EXTRA_ALARM = "intent.extra.alarm";

	// This extra is the raw Alarm object data. It is used in the
	// AlarmManagerService to avoid a ClassNotFoundException when filling in
	// the Intent extras.
	public static final String EXTRA_ALARM_RAW = "intent.extra.alarm_raw";

	// This string is used when passing a URI referring to an alarm through an
	// Intent
	public static final String EXTRA_ALARM_URI = "intent.extra.alarm_uri";

	// This extra is whether the Alarm UI (AlarmActivity) should display the
	// Nice Alarm UI
	public static final String EXTRA_UI_NICEALARM = "intent.extra.ui_nicealarm";

	// Format strings
	final static String M12 = "h:mm aa";
	final static String M24 = "kk:mm";

	final static int INVALID_ALARM_ID = -1;

	// //////////////////////////////////////////////////////////////////////////////
	// Time calculation
	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * Given an Alarm object, return a time suitable for setting in
	 * AlarmManager. Exclude Nice Alarm, because this is for setting the actual
	 * alarm alert.
	 * 
	 * @param alarm
	 *            Alarm object
	 * @return time in millis since epoch
	 */
	public static long calculateAlarmAlert(Alarm alarm)
	{
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis();
	}

	/**
	 * Given an Alarm object, return a time suitable for setting in
	 * AlarmManager, accounting for Nice alarm
	 * 
	 * @param alarm
	 *            object
	 * @return time in millis since epoch
	 */
	public static long calculateNiceAlarm(Alarm alarm)
	{
		long alarmTime = calculateAlarmAlert(alarm);
		if (alarm.niceAlarm_enabled && alarm.niceAlarm_leadInSeconds > 0)
		{
			return alarmTime - alarm.niceAlarm_leadInSeconds * 1000;
		}
		else
		{
			return alarmTime;
		}
	}

	/**
	 * Given an alarm in hours and minutes, return a time suitable for
	 * setting in AlarmManager. DOES NOT HANDLE NICE ALARM
	 */
	public static Calendar calculateAlarm(int hour, int minute, DaysOfWeek daysOfWeek)
	{

		// start with now
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);

		// if alarm is behind current time, advance one day
		if (hour < nowHour || hour == nowHour && minute <= nowMinute)
		{
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		int addDays = daysOfWeek.getNextAlarm(c);
		if (addDays > 0)
			c.add(Calendar.DAY_OF_WEEK, addDays);
		return c;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Date/Time Text Formatting Methods
	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * Given hour and minute, format the time according to user locale
	 * 
	 * @param context
	 *            Context to decide the format
	 * @param hour
	 *            Hour of day (0-23)
	 * @param minute
	 *            Minute of hour (0-59)
	 * @param daysOfWeek
	 *            Not used
	 * @return Formatted String
	 */
	public static String formatTime(final Context context, int hour, int minute, DaysOfWeek daysOfWeek)
	{
		Calendar c = calculateAlarm(hour, minute, daysOfWeek);
		return formatTime(context, c);
	}

	/**
	 * Given a Calendar object representing a moment in time, format the time
	 * according to user locale
	 * 
	 * @param context
	 *            Context to decide format
	 * @param c
	 *            Calendar object
	 * @return Formatted String
	 */
	public static String formatTime(final Context context, final Calendar c)
	{
		String format = DateFormat.is24HourFormat(context) ? M24 : M12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}

	/**
	 * format "Alarm set for 2 days 7 hours and 53 minutes from
	 * now"
	 */
	public static String formatToast(Context context, long timeInMillis)
	{
		long delta = timeInMillis - System.currentTimeMillis();
		long hours = delta / (1000 * 60 * 60);
		long minutes = delta / (1000 * 60) % 60;
		long days = hours / 24;
		hours = hours % 24;

		String daySeq = (days == 0) ? "" : (days == 1) ? context.getString(R.string.day) : context.getString(R.string.days, Long.toString(days));

		String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context.getString(R.string.minute) : context.getString(R.string.minutes, Long.toString(minutes));

		String hourSeq = (hours == 0) ? "" : (hours == 1) ? context.getString(R.string.hour) : context.getString(R.string.hours, Long.toString(hours));

		boolean dispDays = days > 0;
		boolean dispHour = hours > 0;
		boolean dispMinute = minutes > 0;

		int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0) | (dispMinute ? 4 : 0);

		String[] formats = context.getResources().getStringArray(R.array.alarm_set);
		String formatted = String.format(formats[index], daySeq, hourSeq, minSeq);

		return formatted;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Database Interface Methods
	// //////////////////////////////////////////////////////////////////////////////

	// Build a ContentValues object from an Alarm object
	private static ContentValues createContentValues(Alarm alarm)
	{
		ContentValues values = new ContentValues(8);
		// Set the alarm_time value if this alarm does not repeat. This will be
		// used later to disable expire alarms.
		long time = 0;
		if (!alarm.daysOfWeek.isRepeatSet())
		{
			time = calculateNiceAlarm(alarm);
		}

		values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
		values.put(Alarm.Columns.HOUR, alarm.hour);
		values.put(Alarm.Columns.MINUTES, alarm.minutes);
		values.put(Alarm.Columns.ALARM_TIME, time);
		values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
		values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
		values.put(Alarm.Columns.MESSAGE, alarm.label);
		values.put(Alarm.Columns.NICEALARM_ENABLED, alarm.niceAlarm_enabled ? 1 : 0);
		values.put(Alarm.Columns.NICEALARM_LEADIN, alarm.niceAlarm_leadInSeconds);

		// A null alert Uri indicates a silent alarm.
		values.put(Alarm.Columns.ALERT, alarm.alert == null ? Alarm.ALARM_ALERT_SILENT : alarm.alert.toString());
		values.put(Alarm.Columns.NICEALARM_ALERT, alarm.niceAlarm_alert == null ? Alarm.ALARM_ALERT_SILENT : alarm.niceAlarm_alert.toString());

		return values;
	}

	/**
	 * Insert a new alarm into the database, modifies Alarm object with the
	 * newly inserted ID
	 * 
	 * @param context
	 *            Context object for getting the ContentResolver
	 * @param alarm
	 *            Alarm object to insert
	 * @return time in millis since epoch of new alarm
	 */
	public static long insertAlarm(Context context, Alarm alarm)
	{
		Log.e(TAG, "insertAlarm() called...");
		// Add alarm to database, be sure to change the passed alarm to
		// reflect it's new id
		ContentValues values = createContentValues(alarm);
		Uri uri = context.getContentResolver().insert(Alarm.Columns.CONTENT_URI, values);
		alarm.id = (int) ContentUris.parseId(uri);
		long timeInMillis = calculateNiceAlarm(alarm);
		setNextAlarm(context);
		Log.e(TAG, "alarm (id = " + alarm.id + ") fires at " + timeInMillis + " (systemTime=" + System.currentTimeMillis() + ")");
		return timeInMillis;
	}

	/**
	 * Update an existing alarm in the database
	 * 
	 * @param context
	 *            Context object for getting the ContentResolver
	 * @param alarm
	 *            Alarm object to update (should have a valid ID)
	 * @return time in millis since epoch of updated alarm
	 */
	public static long updateAlarm(Context context, Alarm alarm)
	{
		// Update the passed alarm in the database
		ContentValues values = createContentValues(alarm);
		ContentResolver resolver = context.getContentResolver();
		resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values, null, null);
		long timeInMillis = calculateNiceAlarm(alarm);
		setNextAlarm(context);
		return timeInMillis;
	}

	/**
	 * Delete an existing alarm in the database
	 * 
	 * @param context
	 *            Context object for getting the ContentResolver
	 * @param alarmId
	 *            Id of alarm in database
	 */
	public static void deleteAlarm(Context context, int alarmId)
	{
		// Delete an alarm from the database
		if (alarmId == INVALID_ALARM_ID)
			return;
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
		contentResolver.delete(uri, "", null);
		setNextAlarm(context);
	}

	/**
	 * Return an Alarm object representing the alarm id in the database.
	 * 
	 * @param contentResolver
	 *            ContentResolver object to query
	 * @param alarmId
	 *            Id number of Alarm
	 * @return null if no alarm exists.
	 */
	public static Alarm getAlarm(ContentResolver contentResolver, int alarmId)
	{
		Cursor cursor = contentResolver.query(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId), Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
		Alarm alarm = null;
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				alarm = new Alarm(cursor);
			}
			cursor.close();
		}
		return alarm;
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Simple enabling/disabling alarms
	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * A convenience method to enable or disable an alarm.
	 * 
	 * @param id
	 *            corresponds to the _id column
	 * @param enabled
	 *            corresponds to the ENABLED column
	 */
	public static void enableAlarm(final Context context, final int id, boolean enabled)
	{
		enableAlarmInternal(context, id, enabled);
		setNextAlarm(context);
	}

	// Enable or disable an alarm specified by 'id'
	private static void enableAlarmInternal(final Context context, final int id, boolean enabled)
	{
		enableAlarmInternal(context, getAlarm(context.getContentResolver(), id), enabled);
	}

	// Enable or disable 'alarm'
	private static void enableAlarmInternal(final Context context, final Alarm alarm, boolean enabled)
	{
		if (alarm == null)
		{
			return;
		}
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues(2);
		values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

		// If we are enabling the alarm, calculate alarm time since the time
		// value in Alarm may be old.
		if (enabled)
		{
			long time = 0;
			if (!alarm.daysOfWeek.isRepeatSet())
			{
				time = calculateNiceAlarm(alarm);
			}
			values.put(Alarm.Columns.ALARM_TIME, time);
		}

		resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values, null, null);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Alarm/Alert Handling Methods (Handles determining when alarms fire, etc.)
	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * Find the next enabled alarm set to go off
	 * 
	 * @param context
	 *            Context object
	 * @return Alarm object representing the next enabled Alarm, null if no
	 *         alarms are set
	 */
	private static Alarm calculateNextAlarm(Context context)
	{

		// Query the ContentResolver
		Cursor cursor = getEnabledAlarms(context.getContentResolver());

		// Iterate over the cursor, find the earliest alarm
		long earliest = Long.MAX_VALUE;
		long now = System.currentTimeMillis();
		Alarm alarm = null, eAlarm = null;
		while (cursor.moveToNext())
		{
			// Get an Alarm object
			alarm = new Alarm(cursor);

			// If time is not set, calculate it (for repeating alarms)
			if (alarm.time == 0)
			{
				alarm.time = calculateNiceAlarm(alarm);
			}

			// Check if expired
			if (alarm.time < now)
			{
				// Alarm is expired
				if (!alarm.daysOfWeek.isRepeatSet())
				{
					// Disable if it does not repeat
					enableAlarmInternal(context, alarm, false);
				}
			}

			// Check if earlier than current earliest
			else if (alarm.time < earliest)
			{
				// New earliest alarm
				earliest = alarm.time;
				eAlarm = alarm;
			}
		}

		return eAlarm;
	}

	/**
	 * Called at system startup, on time/timezone change, and whenever
	 * the user changes alarm settings. Activates snooze if set,
	 * otherwise loads all alarms, activates next alert.
	 * 
	 * @param context
	 *            Context object for getting a ContentResolver and accessing the
	 *            system AlarmManager service
	 */
	public static void setNextAlarm(final Context context)
	{
		final Alarm alarm = calculateNextAlarm(context);
		if (alarm != null)
		{
			enableAlert(context, alarm, alarm.time);
		}
		else
		{
			disableAlert(context);
		}
	}

	/**
	 * Called when a NiceAlarm is triggered, this will schedule a broadcast to
	 * be sent to inform the service that it is time to trigger the actual alarm
	 * alert
	 * 
	 * @param context
	 *            Context reference
	 * @param alarm
	 *            Alarm object
	 */
	public static void scheduleAlarmAlert(Context context, final Alarm alarm)
	{
		enableAlert(context, alarm, calculateAlarmAlert(alarm));
	}

	/**
	 * Called when the user wants to snooze an alarm, this will schedule a
	 * broadcast to be sent to relaunch the alarm after the snooze period is up
	 * 
	 * @param context
	 *            Context reference
	 * @param alarm
	 *            Alarm object
	 * @param snoozeDelta
	 *            This number is added to calculateAlarmAlert(alarm) to get the
	 *            time that the alarm should go off again
	 */
	public static void scheduleSnoozedAlarm(Context context, final Alarm alarm, long snoozeDelta)
	{
		long alarmTime = calculateAlarmAlert(alarm) + snoozeDelta;
		enableAlert(context, alarm, alarmTime);
	}

	/**
	 * Sets alert in AlarmManger and StatusBar. This is what will
	 * actually launch the alert when the alarm triggers.
	 * 
	 * @param alarm
	 *            Alarm.
	 * @param atTimeInMillis
	 *            milliseconds since epoch
	 */
	private static void enableAlert(Context context, final Alarm alarm, final long atTimeInMillis)
	{
		AlarmManager am = getAlarmManager(context);
		Intent intent = new Intent(ACTION_ALARM_ALERT);

		// XXX: This is a slight hack to avoid an exception in the remote
		// AlarmManagerService process. The AlarmManager adds extra data to
		// this Intent which causes it to inflate. Since the remote process
		// does not know about the Alarm class, it throws a
		// ClassNotFoundException.
		//
		// To avoid this, we marshall the data ourselves and then parcel a plain
		// byte[] array. The AlarmReceiver class knows to build the Alarm
		// object from the byte[] array.
		Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(EXTRA_ALARM_RAW, out.marshall());
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

	}

	/**
	 * Disables alert in AlarmManger and StatusBar.
	 * 
	 * @param id
	 *            Alarm ID.
	 */
	private static void disableAlert(Context context)
	{
		AlarmManager am = getAlarmManager(context);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_ALARM_ALERT), PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
	}

	/**
	 * Get the list of enabled Alarms
	 * 
	 * @param contentResolver
	 *            ContentResolver object to query
	 * @return Cursor object representing the results of the query
	 */
	private static Cursor getEnabledAlarms(ContentResolver contentResolver)
	{
		return contentResolver.query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.ENABLED + "=1", null, Alarm.Columns.DEFAULT_SORT_ORDER);
	}

	/**
	 * Get the AlarmManager system service
	 * 
	 * @param context
	 *            Context to query system services
	 * @return AlarmManager reference
	 */
	public static AlarmManager getAlarmManager(Context context)
	{
		return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

}
