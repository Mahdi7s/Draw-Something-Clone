package com.chocolate.puzhle2.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.repos.UnitOfWork;

/**
 * Created by mahdi on 10/1/15.
 */
public class AlarmAutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//            AlarmReceiver.scheduleLikeNotification(context);
            ToastManager toastManager = new ToastManager(context);
            UnitOfWork unitOfWork = new UnitOfWork(context, new DialogManager(context, toastManager), toastManager);
            AlarmReceiver.scheduleCreateAndSolveNotification(context, unitOfWork.getLocalRepo(), unitOfWork.getDrawingWordRepo());
        }
    }
}
