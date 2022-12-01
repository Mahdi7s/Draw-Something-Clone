package com.chocolate.puzhle2;

import com.chocolate.puzhle2.repos.ParseHelper;

import net.danlew.android.joda.JodaTimeAndroid;

//import ir.sls.android.slspush.IFinishedInit;
//import ir.sls.android.slspush.SLS;
//import ir.sls.android.slspush.SlsApplication;

/**
 * Created by choc01ate on 5/11/2015.
 */
public class Application extends android.app.Application//SlsApplication implements IFinishedInit
{
	@Override
	public void onCreate ()
	{
		super.onCreate();

		JodaTimeAndroid.init(this);

		ParseHelper.initialize1(this);
		// init push
		//SLS.init(this, "16a8e68d-9db5-40c5-8556-4cb99349e009", this);
	}

//	@Override
//	public void finishedInit(int i) {
//		switch (i){
//			case SLS.RESULT_OK:
//				Log.i("baas", "RESULT_OK");
//				break;
//			case SLS.RESULT_FAILED:
//				Log.i("baas", "RESULT_FAILED");
//				break;
//		}
//	}
}
