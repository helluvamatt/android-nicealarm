package com.schneenet.android.nicealarm;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NiceAlarmListActivity extends ListActivity implements OnItemClickListener
{
	
	private ImageButton mAddButton;
	private ListView mAlarmsList;
	private LinearLayout mProgressContainer;
	private FrameLayout mListContainer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_list);
		
		mProgressContainer = (LinearLayout) findViewById(R.id.progressContainer);
		mListContainer = (FrameLayout) findViewById(R.id.listContainer);
		
		mAddButton = (ImageButton) findViewById(R.id.alarm_list_add_button);
		mAddButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				doNewAlarm();
			}
		});
		
		mAlarmsList = (ListView) findViewById(android.R.id.list);
		mAlarmsList.setOnCreateContextMenuListener(this);
		mAlarmsList.setOnItemClickListener(this);
		// TODO Load the alarms from the database and build a NiceAlarmAdapter
	}
	
	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{

		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id)
	{
		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(pos);
		final Alarm alarm = new Alarm(c);
		editAlarm(alarm);
	}
	
	private void editAlarm(Alarm alarm)
	{
		Intent intent = new Intent(AlarmEditorActivity.ACTION_EDIT_ALARM);
		intent.putExtra(AlarmEditorActivity.ALARM_INTENT_EXTRA, alarm);
		startActivity(intent);
	}
	
	private void doNewAlarm()
	{
		startActivity(new Intent(AlarmEditorActivity.ACTION_EDIT_ALARM));
	}

}