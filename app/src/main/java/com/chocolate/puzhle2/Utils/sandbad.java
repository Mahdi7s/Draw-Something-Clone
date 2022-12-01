package com.chocolate.puzhle2.Utils;

/**
 * Created by hossein on 2/9/16.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.chocolate.puzhle2.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.net.URL;

public class sandbad {
    
    final int API_ID=33;

    private Context context;

    private SharedPreferences pref;

    public static final String PREF_FIRSTCHECK="sd_firstCheck";
    public static final String PREF_LASTCHECK="sd_lastCheck";

    public sandbad(Context c){
        context=c;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void trackInstall(){
        long first=pref.getLong(PREF_FIRSTCHECK,0L);
        long last=pref.getLong(PREF_LASTCHECK,0L);
        long time=System.currentTimeMillis();
        if(first==0L){
            SharedPreferences.Editor edit= pref.edit();
            edit.putLong(PREF_FIRSTCHECK,time);
            edit.commit();
        }
        if(isNetworkAvailable() &&
                (
                    ((time-last)>86400000L) || ((time-first)<(2*3600*24*1000L))
                    )
                ){
            (new getNews()).execute();
        }
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private static String getDeviceID(Context context) {
        String deviceId;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();

        if (deviceId == null) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            deviceId = wifiManager.getConnectionInfo().getMacAddress();

            if (deviceId == null) {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }

        if ("9774d56d682e549c".equals(deviceId) || deviceId == null) {
            deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }

        return (deviceId != null) ? deviceId : "DEVICE_ID_ERROR";
    }
    private class getNews extends AsyncTask<Void,Void,String> {
        String market;
        public getNews(){
            market=context.getResources().getString(R.string.market);
        }
        protected String doInBackground(Void... v) {
            try {
                System.setProperty("http.keepAlive", "false");
                String id= getDeviceID(context);
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                String url = "http://s.boozar.ir/app/ads.php" +
                        "?id&app="+API_ID+"&r="+market+"&i="+
                        id+"&ver="+info.versionCode+
                        "&sdk="+ Build.VERSION.SDK_INT
                        +"&time="+ ("time"+id+market).hashCode();
                URLConnection conn = new URL(url).openConnection();
                conn.setUseCaches(false);
                InputStream in = conn.getInputStream();
                return convertStreamToString(in);
            } catch (Exception e) {
                Log.e("hz", "getNews-E",e);
            }
            return "";
        }
        protected void onPostExecute(String data){
            String[] resp=data.split(";");
            if(resp[0].equals("") && resp.length>1){
                SharedPreferences.Editor e=pref.edit();
                e.putLong(PREF_LASTCHECK,System.currentTimeMillis());
                e.commit();
            }
        }
    }
    public static String convertStreamToString(InputStream is) throws Exception {

        BufferedReader reader = new BufferedReader(new
                InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            Boolean first=true;
            String l;
            while ((line = reader.readLine()) != null) {
                if(first){
                    first=false;
                    l=line;
                }else
                    l="\n"+line;
                sb.append(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public static class Net extends BroadcastReceiver {
        public void onReceive(final Context context, Intent intent) {
            //super.onReceive(context, intent);
            if (intent.getExtras() != null) {
                NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                    (new sandbad(context)).trackInstall();
                }
            }
        }
    }
}


