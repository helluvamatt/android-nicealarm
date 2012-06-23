package com.schneenet.android.nicealarm.ui;

import android.app.Activity;
import android.os.Bundle;
import com.schneenet.android.nicealarm.R;

public class AlarmEditorActivity extends Activity
{

	private AlarmEditorFragment mFragment;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.simple_container);
		if (savedInstanceState == null)
		{
			mFragment = new AlarmEditorFragment();
			
			getFragmentManager().beginTransaction().add(R.id.content, mFragment).commit();
		}
	}
	
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		mFragment.onSaveInstanceState(outState);
	}
	
	public void onBackPressed()
	{
		mFragment.onBackPressed();
	}
	
}
