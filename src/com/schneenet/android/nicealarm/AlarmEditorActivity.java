package com.schneenet.android.nicealarm;

import android.app.Activity;
import android.os.Bundle;

public class AlarmEditorActivity extends Activity
{

	// This Action is for editing alarms
	public static final String ACTION_EDIT_ALARM = "com.schneenet.android.nicealarm.ACTION_EDIT_ALARM";
	
	// This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";


	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_editor);

		// TODO Nice Alarm editor

	}

}
