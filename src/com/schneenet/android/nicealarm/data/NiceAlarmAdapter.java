package com.schneenet.android.nicealarm.data;

import java.util.Calendar;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.schneenet.android.nicealarm.R;
import com.schneenet.android.nicealarm.ui.views.DigitalClock;

public class NiceAlarmAdapter extends CursorAdapter
{

	private AlarmStateToggledInterface tListener;
	
	public NiceAlarmAdapter(Context ctxt, Cursor c, AlarmStateToggledInterface l)
	{
		super(ctxt, c, 0);
		tListener = l;
	}

	@Override
	public void bindView(View view, Context ctxt, Cursor cursor)
	{
		final Alarm alarm = new Alarm(cursor);

		View indicator = view.findViewById(R.id.indicator);

		// Set the initial state of the clock "checkbox"
		final CheckBox clockOnOff = (CheckBox) indicator.findViewById(R.id.clock_onoff);
		clockOnOff.setChecked(alarm.enabled);

		// Clicking outside the "checkbox" should also change the state.
		indicator.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				clockOnOff.toggle();
				tListener.onAlarmStateToggled(alarm, clockOnOff.isChecked());
			}
		});

		DigitalClock digitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);

		// set the alarm text
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, alarm.hour);
		c.set(Calendar.MINUTE, alarm.minutes);
		digitalClock.updateTime(c);

		// Set the repeat text or leave it blank if it does not repeat.
		TextView daysOfWeekView = (TextView) digitalClock.findViewById(R.id.daysOfWeek);
		final String daysOfWeekStr = alarm.getDaysOfWeekString(ctxt, false);
		if (daysOfWeekStr != null && daysOfWeekStr.length() != 0)
		{
			daysOfWeekView.setText(daysOfWeekStr);
			daysOfWeekView.setVisibility(View.VISIBLE);
		}
		else
		{
			daysOfWeekView.setVisibility(View.GONE);
		}

		// Display the label
		TextView labelView = (TextView) view.findViewById(R.id.label);
		if (alarm.label != null && alarm.label.length() != 0)
		{
			labelView.setText(alarm.label);
			labelView.setVisibility(View.VISIBLE);
		}
		else
		{
			labelView.setVisibility(View.GONE);
		}
	}

	@Override
	public View newView(Context ctxt, Cursor c, ViewGroup parent)
	{
		View v = LayoutInflater.from(ctxt).inflate(R.layout.alarm_list_item, parent, false);
		DigitalClock clock = (DigitalClock) v.findViewById(R.id.digitalClock);
		clock.setLive(false);
		return v;
	}
	
	public interface AlarmStateToggledInterface
	{
		public void onAlarmStateToggled(Alarm alarm, boolean state);
	}

}
