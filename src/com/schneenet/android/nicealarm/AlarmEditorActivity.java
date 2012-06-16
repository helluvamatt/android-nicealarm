package com.schneenet.android.nicealarm;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TimePicker;

public class AlarmEditorActivity extends Activity
{

	private TimePicker mTimePicker_alarmTime;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_editor);
		
		mTimePicker_alarmTime = (TimePicker) findViewById(R.id.alarm_editor_time);
		
		//TODO Nice Alarm editor
		
	}
	
}
