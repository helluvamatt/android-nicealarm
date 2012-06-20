package com.schneenet.android.nicealarm;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NiceAlarmAdapter extends BaseAdapter
{

	private ArrayList<Alarm> mAlarmList;
	private Context mContext;
	
	public NiceAlarmAdapter(Context ctxt)
	{
		mContext = ctxt;
		mAlarmList = new ArrayList<Alarm>();
	}
	
	@Override
	public int getCount()
	{
		return mAlarmList.size();
	}

	public Alarm getAlarmItem(int pos)
	{
		return mAlarmList.get(pos);
	}
	
	@Override
	public Object getItem(int pos)
	{
		return getAlarmItem(pos);
	}

	@Override
	public long getItemId(int pos)
	{
		return getAlarmItem(pos).id;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.alarm_list_item, parent, false);
		}
		
		// TODO Populate the view
		
		return convertView;
	}

}
