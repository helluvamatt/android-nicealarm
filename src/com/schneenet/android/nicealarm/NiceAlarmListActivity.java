package com.schneenet.android.nicealarm;

import android.app.ListActivity;
import android.os.Bundle;

public class NiceAlarmListActivity extends ListActivity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_list);
		// TODO Load the alarms from the database and build a NiceAlarmAdapter
	}
}