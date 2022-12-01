package com.chocolate.puzhle2.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.chocolate.puzhle2.MainActivity;
import com.chocolate.puzhle2.R;

/**
 * Created by mahdi on 9/29/15.
 */
public class NotificationUtils {

    public static void showNotificationMessage(Context context, String title, String message, String intentAction, String intentPackage, String url, String activity) {
        final Intent intent = new Intent();
        if(!TextUtils.isEmpty(intentAction)) {
            intent.setAction(intentAction);
        }
        if(!TextUtils.isEmpty(intentPackage)){
            intent.setPackage(intentPackage);
//            intent.setClass(context, MainActivity.class);
            intent.putExtra("startAct", true);
        }
        if(!TextUtils.isEmpty(url)){
            intent.setData(Uri.parse(url));
        }
        if(!TextUtils.isEmpty(activity)){
            intent.putExtra("activity", activity);
            intent.setClass(context, MainActivity.class);
        }

        showNotificationMessage(context, title, message, intent);
    }

    public static void showNotificationMessage(Context mContext, String title, String message, Intent intent) {
        // Check for empty push message
        if (TextUtils.isEmpty(title))
            return;

        // notification icon
        int icon = R.drawable.icon;
        int smallIcon = R.drawable.icon_notification;

        int mNotificationId = 0;

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        Notification notification = mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL).setSmallIcon(smallIcon)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(resultPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, notification);
    }
}
