package com.schneenet.android.nicealarm.ui;

import java.util.Calendar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.schneenet.android.nicealarm.R;
import com.schneenet.android.nicealarm.data.Alarm;
import com.schneenet.android.nicealarm.data.NiceAlarmAdapter;
import com.schneenet.android.nicealarm.data.NiceAlarmAdapter.AlarmStateToggledInterface;
import com.schneenet.android.nicealarm.data.NiceAlarmManager;
import com.schneenet.android.nicealarm.util.AsyncRunner;

public class NiceAlarmListActivity extends ListActivity implements OnItemClickListener, AlarmStateToggledInterface
{

	private ImageButton mAddButton;
	private ListView mAlarmsList;
	private NiceAlarmAdapter mAdapter;
	private AlarmBroadcastListener mAlarmListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_list);

		mAddButton = (ImageButton) findViewById(R.id.alarm_list_add_button);
		mAddButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				doNewAlarm();
			}
		});

		mAdapter = new NiceAlarmAdapter(this, null, this);

		mAlarmsList = getListView(); // (ListView)
										// findViewById(android.R.id.list);
		mAlarmsList.setOnCreateContextMenuListener(this);
		mAlarmsList.setOnItemClickListener(this);
		mAlarmsList.setAdapter(mAdapter);
		
		mAlarmListener = new AlarmBroadcastListener();
		IntentFilter filter = new IntentFilter();
		filter.addAction(NiceAlarmManager.ACTION_ALARM_ALERT);
		
		registerReceiver(mAlarmListener, filter);
	}

	public void onResume()
	{
		super.onResume();
		loadAlarms();
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item)
	{
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final int id = (int) info.id;
		// Error check just in case.
		if (id == -1)
		{
			return super.onContextItemSelected(item);
		}
		switch (item.getItemId())
		{
			case R.id.delete_alarm:
			{
				// Confirm that the alarm will be deleted.
				new AlertDialog.Builder(this).setTitle(getString(R.string.delete_alarm)).setMessage(getString(R.string.delete_alarm_confirm)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface d, int w)
					{
						AsyncRunner<Void> runner = new AsyncRunner<Void>(new AsyncRunner.AsyncRunnerInterface<Void>()
						{

							@Override
							public Void onDoWork()
							{
								NiceAlarmManager.deleteAlarm(NiceAlarmListActivity.this, id);
								return null;
							}

							@Override
							public void onFinished(Void result)
							{
								loadAlarms();
							}
						});
						runner.execute();
						
					}
				}).setNegativeButton(android.R.string.cancel, null).show();
				return true;
			}

			case R.id.enable_alarm:
			{
				final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(info.position);
				final Alarm alarm = new Alarm(c);
				onAlarmStateToggled(alarm, !alarm.enabled);
				if (!alarm.enabled)
				{
					AlarmEditorFragment.popAlarmSetToast(this, alarm.hour, alarm.minutes, alarm.daysOfWeek);
				}
				return true;
			}

			case R.id.edit_alarm:
			{
				final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(info.position);
				final Alarm alarm = new Alarm(c);
				editAlarm(alarm);
				return true;
			}

			default:
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		// Inflate the menu from xml.
        getMenuInflater().inflate(R.menu.context_menu, menu);

        // Use the current item to create a custom view for the header.
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        final Cursor c =
                (Cursor) mAlarmsList.getAdapter().getItem(info.position);
        final Alarm alarm = new Alarm(c);

        // Construct the Calendar to compute the time.
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
        cal.set(Calendar.MINUTE, alarm.minutes);
        final String time = NiceAlarmManager.formatTime(this, cal);

        // Inflate the custom view and set each TextView's text.
        final View v = LayoutInflater.from(this).inflate(R.layout.context_menu_header, null);
        TextView textView = (TextView) v.findViewById(R.id.header_time);
        textView.setText(time);
        textView = (TextView) v.findViewById(R.id.header_label);
        textView.setText(alarm.label);

        // Set the custom view on the menu.
        menu.setHeaderView(v);
        // Change the text based on the state of the alarm.
        if (alarm.enabled) {
            menu.findItem(R.id.enable_alarm).setTitle(R.string.disable_alarm);
        }
	}

	@Override
	public void onAlarmStateToggled(Alarm alarm, final boolean state)
	{
		// Async set alarm to state
		final int id = alarm.id;
		AsyncRunner<Void> runner = new AsyncRunner<Void>(new AsyncRunner.AsyncRunnerInterface<Void>()
		{
			@Override
			public Void onDoWork()
			{
				NiceAlarmManager.enableAlarm(NiceAlarmListActivity.this, id, state);
				return null;
			}

			@Override
			public void onFinished(Void result)
			{
				// Re-load alarms
				loadAlarms();
			}
		});
		runner.execute();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id)
	{
		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(pos);
		final Alarm alarm = new Alarm(c);
		editAlarm(alarm);
	}

	private void loadAlarms()
	{
		AsyncRunner<Cursor> runner = new AsyncRunner<Cursor>(new AsyncRunner.AsyncRunnerInterface<Cursor>()
		{

			@Override
			public Cursor onDoWork()
			{
				return getContentResolver().query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, Alarm.Columns.DEFAULT_SORT_ORDER);
			}

			@Override
			public void onFinished(Cursor result)
			{
				mAdapter.swapCursor(result);
			}

		});
		runner.execute();
	}

	private void editAlarm(Alarm alarm)
	{
		Intent intent = new Intent(NiceAlarmManager.ACTION_EDIT_ALARM);
		intent.putExtra(NiceAlarmManager.EXTRA_ALARM, alarm);
		startActivity(intent);
	}

	private void doNewAlarm()
	{
		startActivity(new Intent(NiceAlarmManager.ACTION_EDIT_ALARM));
	}
	
	private class AlarmBroadcastListener extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			
		}
		
	}

}