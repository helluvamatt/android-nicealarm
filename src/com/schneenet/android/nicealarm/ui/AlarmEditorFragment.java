package com.schneenet.android.nicealarm.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.schneenet.android.nicealarm.R;
import com.schneenet.android.nicealarm.data.Alarm;
import com.schneenet.android.nicealarm.data.NiceAlarmManager;
import com.schneenet.android.nicealarm.ui.views.AlarmPreference;
import com.schneenet.android.nicealarm.ui.views.RepeatPreference;
import com.schneenet.android.nicealarm.util.ToastMaster;

public class AlarmEditorFragment extends PreferenceFragment implements OnPreferenceChangeListener, TimePickerDialog.OnTimeSetListener
{
	private static final String KEY_CURRENT_ALARM = "currentAlarm";
	private static final String KEY_ORIGINAL_ALARM = "originalAlarm";
	private static final String KEY_TIME_PICKER_BUNDLE = "timePickerBundle";

	private SwitchPreference mEnabledPref;
	private Preference mTimePref;
	private AlarmPreference mAlarmPref;
	private CheckBoxPreference mVibratePref;
	private RepeatPreference mRepeatPref;
	private EditTextPreference mLabelPref;
	private CheckBoxPreference mNiceEnabledPref;
	private AlarmPreference mNiceAlarmPref;
	private Preference mNiceLeadinPref;
	
	private ListView mListView;

	private int mId;
	private int mHour;
	private int mMinute;
	private int mNiceLeadinSeconds;
	private TimePickerDialog mTimePickerDialog;
	private Alarm mOriginalAlarm;

	public static final String TAG = "AlarmEditorFragment";
	
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		
		Log.e(TAG, "onCreateView() called...");
		
		// Override the default content view.
		return inflater.inflate(R.layout.alarm_editor, parent, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		Log.e(TAG, "onActivityCreated() called...");
		
		// TODO Stop using preferences for this view. Save on done, not after
		// each change.
		addPreferencesFromResource(R.xml.alarm_prefs);

		// Get each preference so we can retrieve the value later.
		mLabelPref = (EditTextPreference) findPreference("key_label");
		mLabelPref.setOnPreferenceChangeListener(this);
		mEnabledPref = (SwitchPreference) findPreference("key_alarm_enabled");
		mEnabledPref.setOnPreferenceChangeListener(this);
		mTimePref = findPreference("key_time");
		mAlarmPref = (AlarmPreference) findPreference("key_alarm");
		mAlarmPref.setOnPreferenceChangeListener(this);
		mVibratePref = (CheckBoxPreference) findPreference("key_vibrate");
		mVibratePref.setOnPreferenceChangeListener(this);
		Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		if (!v.hasVibrator())
		{
			getPreferenceScreen().removePreference(mVibratePref);
		}
		mRepeatPref = (RepeatPreference) findPreference("key_setRepeat");
		mRepeatPref.setOnPreferenceChangeListener(this);
		mNiceEnabledPref = (CheckBoxPreference) findPreference("key_nice_enabled");
		mNiceEnabledPref.setOnPreferenceChangeListener(this);
		mNiceAlarmPref = (AlarmPreference) findPreference("key_nice_alert");
		mNiceAlarmPref.setOnPreferenceChangeListener(this);
		mNiceLeadinPref = findPreference("key_nice_leadin");

		Intent i = getActivity().getIntent();
		Alarm alarm = i.getParcelableExtra(NiceAlarmManager.EXTRA_ALARM);

		if (alarm == null)
		{
			// No alarm means create a new alarm.
			alarm = new Alarm();
		}
		mOriginalAlarm = alarm;

		// Populate the prefs with the original alarm data. updatePrefs also
		// sets mId so it must be called before checking mId below.
		updatePrefs(mOriginalAlarm);

		// We have to do this to get the save/cancel buttons to highlight on
		// their own.
		mListView = (ListView) getView().findViewById(android.R.id.list);
		mListView.setItemsCanFocus(true);

		// Attach actions to each button.
		Button b = (Button) getView().findViewById(R.id.alarm_save);
		b.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				long time = saveAlarm(null);
				if (mEnabledPref.isChecked())
				{
					popAlarmSetToast(getActivity(), time);
				}
				getActivity().finish();
			}
		});
		Button revert = (Button) getView().findViewById(R.id.alarm_revert);
		revert.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				revert();
				getActivity().finish();
			}
		});
		b = (Button) getView().findViewById(R.id.alarm_delete);
		if (mId == -1)
		{
			b.setEnabled(false);
			b.setVisibility(View.GONE);
		}
		else
		{
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					deleteAlarm();
				}
			});
		}
		
		Log.e(TAG, "onActivityCreated() exiting...");
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_ORIGINAL_ALARM, mOriginalAlarm);
		outState.putParcelable(KEY_CURRENT_ALARM, buildAlarmFromUi());
		if (mTimePickerDialog != null)
		{
			if (mTimePickerDialog.isShowing())
			{
				outState.putParcelable(KEY_TIME_PICKER_BUNDLE, mTimePickerDialog.onSaveInstanceState());
				mTimePickerDialog.dismiss();
			}
			mTimePickerDialog = null;
		}
	}

	// Used to post runnables asynchronously.
	private static final Handler sHandler = new Handler();

	public boolean onPreferenceChange(final Preference p, Object newValue)
	{
		
		Log.e(TAG, "Preference Changed: " + p.toString());
		
		// Asynchronously save the alarm since this method is called _before_
		// the value of the preference has changed.
		
		sHandler.post(new Runnable()
		{
			public void run()
			{
				// Editing any preference (except enable) enables the alarm.
				if (p.getKey().equals(mEnabledPref.getKey()))
				{
					mEnabledPref.setChecked(true);
				}
				saveAlarm(null);
			}
		});
		return true;
	}

	private void updatePrefs(Alarm alarm)
	{
		mId = alarm.id;
		mEnabledPref.setChecked(alarm.enabled);
		mLabelPref.setText(alarm.label);
		mHour = alarm.hour;
		mMinute = alarm.minutes;
		mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
		mVibratePref.setChecked(alarm.vibrate);
		// Give the alert uri to the preference.
		mAlarmPref.setAlert(alarm.alert);
		mNiceEnabledPref.setChecked(alarm.niceAlarm_enabled);
		mNiceAlarmPref.setAlert(alarm.niceAlarm_alert);
		mNiceLeadinSeconds = alarm.niceAlarm_leadInSeconds;
		updateTime();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		
		Log.e(TAG, "onPreferenceTreeClick() called...");
		if (preference == mTimePref)
		{
			showTimePicker();
		}
		else if (preference == mNiceLeadinPref)
		{
			showLeadinPicker();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	public void onBackPressed()
	{
		revert();
		getActivity().finish();
	}

	private void showTimePicker()
	{
		if (mTimePickerDialog != null)
		{
			if (mTimePickerDialog.isShowing())
			{
				Log.e(TAG, "mTimePickerDialog is already showing.");
				mTimePickerDialog.dismiss();
			}
			else
			{
				Log.e(TAG, "mTimePickerDialog is not null");
			}
			mTimePickerDialog.dismiss();
		}

		mTimePickerDialog = new TimePickerDialog(getActivity(), this, mHour, mMinute, DateFormat.is24HourFormat(getActivity()));
		mTimePickerDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog)
			{
				mTimePickerDialog = null;
			}
		});
		mTimePickerDialog.show();
	}
	
	private void showLeadinPicker()
	{
		// TODO Show lead in selection dialog
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute)
	{
		// onTimeSet is called when the user clicks "Set"
		mTimePickerDialog = null;
		mHour = hourOfDay;
		mMinute = minute;
		updateTime();
		// If the time has been changed, enable the alarm.
		mEnabledPref.setChecked(true);
	}

	private void updateTime()
	{
		mTimePref.setSummary(NiceAlarmManager.formatTime(getActivity(), mHour, mMinute, mRepeatPref.getDaysOfWeek()));
	}

	private long saveAlarm(Alarm alarm)
	{
		if (alarm == null)
		{
			alarm = buildAlarmFromUi();
		}

		long time;
		if (alarm.id == -1)
		{
			time = NiceAlarmManager.insertAlarm(getActivity(), alarm);
			// addAlarm populates the alarm with the new id. Update mId so that
			// changes to other preferences update the new alarm.
			mId = alarm.id;
		}
		else
		{
			time = NiceAlarmManager.updateAlarm(getActivity(), alarm);
		}
		return time;
	}

	private Alarm buildAlarmFromUi()
	{
		Alarm alarm = new Alarm();
		alarm.id = mId;
		alarm.enabled = mEnabledPref.isChecked();
		alarm.hour = mHour;
		alarm.minutes = mMinute;
		alarm.daysOfWeek = mRepeatPref.getDaysOfWeek();
		alarm.vibrate = mVibratePref.isChecked();
		alarm.label = mLabelPref.getText() != null ? mLabelPref.getText() : "";
		alarm.alert = mAlarmPref.getAlert();
		alarm.niceAlarm_enabled = mNiceEnabledPref.isChecked();
		alarm.niceAlarm_alert = mNiceAlarmPref.getAlert();
		alarm.niceAlarm_leadInSeconds = mNiceLeadinSeconds;
		return alarm;
	}

	private void deleteAlarm()
	{
		new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.delete_alarm)).setMessage(getString(R.string.delete_alarm_confirm)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface d, int w)
			{
				NiceAlarmManager.deleteAlarm(getActivity(), mId);
				getActivity().finish();
			}
		}).setNegativeButton(android.R.string.cancel, null).show();
	}

	private void revert()
	{
		int newId = mId;
		// "Revert" on a newly created alarm should delete it.
		if (mOriginalAlarm.id == -1)
		{
			NiceAlarmManager.deleteAlarm(getActivity(), newId);
		}
		else
		{
			saveAlarm(mOriginalAlarm);
		}
	}

	/**
	 * Display a toast that tells the user how long until the alarm
	 * goes off. This helps prevent "am/pm" mistakes.
	 */
	public static void popAlarmSetToast(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek)
	{
		popAlarmSetToast(context, NiceAlarmManager.calculateAlarm(hour, minute, daysOfWeek).getTimeInMillis());
	}

	static void popAlarmSetToast(Context context, long timeInMillis)
	{
		String toastText = NiceAlarmManager.formatToast(context, timeInMillis);
		Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
		ToastMaster.setToast(toast);
		toast.show();
	}


}