package com.schneenet.android.nicealarm.util;

import android.os.AsyncTask;

/**
 * AsyncRunner - run abstract jobs on a separate thread, notifying an interface when complete
 * @author Matt Schneeberger
 *
 */
public class AsyncRunner<ResultType> extends AsyncTask<Void, Void, ResultType>
{
	private AsyncRunnerInterface<ResultType> mInterface;
	
	public AsyncRunner(AsyncRunnerInterface<ResultType> i)
	{
		mInterface = i;
	}
	
	@Override
	protected ResultType doInBackground(Void... args)
	{
		return mInterface.onDoWork();
	}
	
	protected void onPostExecute(ResultType result)
	{
		mInterface.onFinished(result);
	}
	
	public interface AsyncRunnerInterface<ResultType>
	{
		public ResultType onDoWork();
		public void onFinished(ResultType result);
	}

}
