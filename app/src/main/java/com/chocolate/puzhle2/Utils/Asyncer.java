package com.chocolate.puzhle2.Utils;


import android.os.AsyncTask;

/**
 * Created by mahdi on 5/24/15.
 */
public class Asyncer
{

	public static <TRes> void runAsync (ResultProvider<TRes> provider, ResultConsumer<TRes> consumer)
	{
		runAsync(provider, consumer, null);
	}

	public static <TRes> void runAsync (ResultProvider<TRes> provider, ResultConsumer<TRes> consumer, Object... params)
	{
		new CAsyncTask<TRes>(provider, consumer).execute(params);
	}

	public interface ResultProvider<T>
	{
		T runAsync (Object... params);
	}

	public interface ResultConsumer<T>
	{
		void postRun (T result);
	}

	public static class CAsyncTask<TResult> extends AsyncTask<Object, Integer, TResult>
	{
		private final ResultProvider<TResult> mProvider;
		private final ResultConsumer<TResult> mConsumer;

		public CAsyncTask (ResultProvider<TResult> provider, ResultConsumer<TResult> consumer)
		{
			mProvider = provider;
			mConsumer = consumer;
		}

		@Override
		protected TResult doInBackground (Object... objects)
		{
			return mProvider.runAsync(objects);
		}

		@Override
		protected void onPostExecute (TResult result)
		{
			mConsumer.postRun(result);
		}
	}
}
