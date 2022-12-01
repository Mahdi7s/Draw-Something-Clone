package com.chocolate.puzhle2.Utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.PublishDestination;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by mahdi on 6/22/15.
 */
public class Utility
{
	public static boolean rate(Context context) {
		try {
			if (AppStatistics.publishDestination == PublishDestination.Bazaar || AppStatistics.publishDestination == PublishDestination.HamPay) {
				Intent intent = new Intent(Intent.ACTION_EDIT);
				intent.setData(Uri.parse("bazaar://details?id=com.chocolate.puzhle2"));
				intent.setPackage(AppStatistics.getPublishPackage());
				context.startActivity(intent);
			} else if(AppStatistics.publishDestination == PublishDestination.IrApps) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://iranapps.ir/app/com.chocolate.puzhle2?a=comment&r=5"));
				intent.setPackage(AppStatistics.getPublishPackage());
				context.startActivity(intent);
			} else if(AppStatistics.publishDestination == PublishDestination.Myket){
				String url = "myket://comment/#Intent;scheme=comment;package=com.chocolate.puzhle2;end";
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				intent.setPackage(AppStatistics.getPublishPackage());
				context.startActivity(intent);
			} else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("cando://leave-review?id=com.chocolate.puzhle2"));
				intent.setPackage(AppStatistics.getPublishPackage());
				context.startActivity(intent);
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public static float dpFromPx (final Context context, final float px)
	{
		return px / context.getResources().getDisplayMetrics().density;
	}

	public static float pxFromDp (final Context context, final float dp)
	{
		return dp * context.getResources().getDisplayMetrics().density;
	}

	public static void canAccessParse (Asyncer.ResultConsumer<Boolean> callback)
	{
		canAccessSite("https://parse.com", callback);
	}

	public static void canAccessBazaar (Asyncer.ResultConsumer<Boolean> callback)
	{
		canAccessSite("http://cafebazaar.ir", callback);
	}

	public static void canAccessSite (String site, Asyncer.ResultConsumer<Boolean> callback)
	{
		Asyncer.runAsync(params -> {
			try {
				InetAddress ipAddr = InetAddress.getByName(site); //You can replace it with your name

				if (ipAddr.equals("")) {
					return false;
				} else {
					return true;
				}

			} catch (Exception e) {
				return false;
			}
		}, result -> {
			callback.postRun(result);
		});
	}

	public static boolean isConnectingToInternet(Context context){
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}

		}
		return false;
	}

	public static boolean isAppIsInBackground(Context context) {
		boolean isInBackground = true;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
			List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
				if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					for (String activeProcess : processInfo.pkgList) {
						if (activeProcess.equals(context.getPackageName())) {
							isInBackground = false;
						}
					}
				}
			}
		} else {
			List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
			ComponentName componentInfo = taskInfo.get(0).topActivity;
			if (componentInfo.getPackageName().equals(context.getPackageName())) {
				isInBackground = false;
			}
		}

		return isInBackground;
	}

	//sample: getResId("icon", context, Drawable.class);
	public static int getResId(String resName, Class<?> c) {
		try {
			Field idField = c.getDeclaredField(resName);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static boolean isMarketInstalled(Context context) {
		if (AppStatistics.publishDestination == PublishDestination.HamPay) return  true;

		//---------------------------------------------------------------------------------

		final boolean exists = isPackageExisted(context, AppStatistics.getPublishPackage());
		if(!exists){
			new ToastManager(context).show(AppStatistics.marketNotInstalledMsg());
		}
		return exists;
	}

	public static boolean isPackageExisted(Context context, String targetPackage) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}
}
