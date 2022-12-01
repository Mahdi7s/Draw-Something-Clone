package com.chocolate.puzhle2.events;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chocolate.puzhle2.Utils.NotificationUtils;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mahdi on 9/29/15.
 */
public class CPushReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = CPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    private Intent parseIntent;

    public CPushReceiver() {
        super();
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);

        if (intent == null)
            return;

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            Log.e(TAG, "Push received: " + json);

            parseIntent = intent;

            parsePushJson(context, json);

        } catch (JSONException e) {
            Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    /**
     * Parses the push notification json
     *
     * @param context
     * @param json
     */
    private void parsePushJson(Context context, JSONObject json) {
        try {
            JSONObject data = json.getJSONObject("data");
            String title = data.getString("title");
            String message = data.getString("message");
            String url = data.has("url") ? data.getString("url") : null;
            String packageName = data.has("package") ? data.getString("package") : null;
            String action = data.has("action") ? data.getString("action") : null;
            String activityClass = data.has("activity") ? data.getString("activity") : null;

            NotificationUtils.showNotificationMessage(context, title, message, action, packageName, url, activityClass);
        } catch (JSONException e) {
            Log.e(TAG, "Push message json exception: " + e.getMessage());
        }
    }
}
