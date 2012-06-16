package com.schneenet.android.nicealarm;

import java.util.Date;
import android.net.Uri;

public class NiceAlarm
{
	private long mId;
	private Date mAlarmTime;
	private Uri mAlarmSongUri;
	private int mPreplaySeconds;
	private Uri mPreplaySongUri;
	
	public void setId(long newValue)
	{
		mId = newValue;
	}
	
	public long getId()
	{
		return mId;
	}
	
	public void setAlarmTime(Date newValue)
	{
		mAlarmTime = newValue;
	}
	
	public Date getAlarmTime()
	{
		return mAlarmTime;
	}
	
	public void setAlarmSongUri(Uri newValue)
	{
		mAlarmSongUri = newValue;
	}
	
	public Uri getAlarmSongUri()
	{
		return mAlarmSongUri;
	}
	
	public void setPreplaySeconds(int newValue)
	{
		mPreplaySeconds = newValue;
	}
	
	public int getPreplaySeconds()
	{
		return mPreplaySeconds;
	}
	
	public void setPreplaySongUri(Uri newValue)
	{
		mPreplaySongUri = newValue;
	}
	
	public Uri getPreplaySongUri()
	{
		return mPreplaySongUri;
	}
	
}
