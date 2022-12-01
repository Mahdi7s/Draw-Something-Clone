package com.chocolate.puzhle2.events;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.chocolate.puzhle2.CoinStoreActivity;
import com.chocolate.puzhle2.CreateTypeActivity;
import com.chocolate.puzhle2.MyPuzzlesActivity;
import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.SolveType;
import com.chocolate.puzhle2.Utils.NotificationUtils;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.repos.DrawingWordRepo;
import com.chocolate.puzhle2.repos.LocalRepo;

import org.joda.time.DateTime;

import java.util.Random;

/**
 * Created by mahdi on 9/30/15.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    public static final int RepeatAfter6H = 1000 * 360 * 60 * 6;
    public static final int RepeatAfter12H = 1000 * 360 * 60 * 12;
    public static final int RepeatAfter24H = 1000 * 360 * 60 * 24;

    public static void scheduleLikeNotification(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("LikeAction");
        intent.putExtra("title", "چندتا لایک؟ چندتا؟!");
        intent.putExtra("message", "برای دیدن تعداد لایک ها و حل های پازلت اینجا کلیک کن");
        intent.putExtra("activity", MyPuzzlesActivity.class.getName());

        scheduleIntent(context, 3, intent, DateTime.now().plusHours(3).getMillis(), -1);
    }

    // ---------------------------------------------------------------------------------

    public static void scheduleCreateAndSolveNotification(Context context, LocalRepo localRepo, DrawingWordRepo drawingWordRepo) {
        final int creations = localRepo.getCanCreateCount(),
                solves = localRepo.getCanSolveCount();

        final GameUser gameUser = GameUser.getGameUser();

        BiFunction3<String, String, String, Object> scheduleFunc = (title, msg, activity) -> {
            boolean isCreate = activity.equals(CreateTypeActivity.class.getName());
            Intent intent = new Intent(context, AlarmReceiver.class/*isCreate ? CreateTypeActivity.class : SolveType.class*/);
            intent.setAction(isCreate ? "CreateAction" : "SolveAction");
            intent.putExtra("title", title);
            intent.putExtra("message", msg);
            intent.putExtra("activity", activity);

            long morning = getTomarrowMillis(8, 30), // 8:30 AM
                    launch = getTomarrowMillis(17, 30); // 5:30 PM
            boolean create = creations > solves;

            scheduleIntent(context, isCreate ? 1 : 2, intent, isCreate ? (create ? morning : launch) : (create ? morning : launch), AlarmManager.INTERVAL_DAY);
            return null;
        };

//        if (creations >= solves) {
        // schedule create
        drawingWordRepo.get3Words((words, e) -> {
            String actvity = CreateTypeActivity.class.getName();
            String title = "هوش دوستات رو به چالش بکش",
                    message = "";

            if (e == null && words.size() == 3) {
                Random random = new Random();
                int rnd = random.nextInt(2);
                DrawingWord drawingWord = words.get(rnd);
                switch (rnd) {
                    case 0:
                        message = String.format("با کلمه <%s> پازل بساز و %d تا سکه جایزه بگیر", drawingWord.getWord(), (drawingWord.getMode() + 1) * 10);
                        break;
                    case 1:
                        message = String.format("می تونی با کلمه <%s> پازل بسازی؟! (جایزه:%d)", drawingWord.getWord(), (drawingWord.getMode() + 1) * 10);
                        break;
                    case 2:
                        message = String.format("%s ! دوست داری یدونه پازل با <%s> بسازی؟!", gameUser.getScore().getDisplayName(), drawingWord.getWord());
                        break;
                }
            } else {
                title = "پاژل تو را می طلبد";
                if (gameUser.getScore().getLeague() == 0) {
                    message = "دوست داری وارد لیگ آی کیو بشی؟!";
                } else {
                    int league = gameUser.getScore().getLeague(),
                            leagueRes = -1;
                    String leagueName = "";

                    switch (league) {
                        case 0:
                            leagueRes = R.drawable.league1;
                            leagueName = "آکبند";
                            break;
                        case 1:
                            leagueRes = R.drawable.league2;
                            leagueName = "آی کیو";
                            break;
                        case 2:
                            leagueRes = R.drawable.league3;
                            leagueName = "عقل کل";
                            break;
                        case 3:
                            leagueRes = R.drawable.league4;
                            leagueName = "دانشمند";
                            break;
                        case 4:
                            leagueRes = R.drawable.league5;
                            leagueName = "نابغه";
                            break;
                        case 5:
                            leagueRes = R.drawable.league6;
                            leagueName = "اعجوبه";
                            break;
                    }

                    message = String.format("ای %s:) بلندشو یه پازل بساز...", leagueName);
                }
            }

            scheduleFunc.run(title, message, actvity);
        });
//        } else {
        // schedule solve

        String actvity = SolveType.class.getName();
        String title = "به دوستات نشون بده چقدر باهوشی",
                message = "";

        if (gameUser.getScore().getLeague() == 0) {
            message = "بیا پازل حل کن تا به لیگ آی کیو بری";
        } else {
            message = "پازل های جدید انتظارت رو میکشن";
        }

        scheduleFunc.run(title, message, actvity);
//        }
    }

    // ---------------------------------------------------------------------------------

    public static void scheduleFreeCoin(Context context, long timeMillis) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("FreeCoinAction");
        intent.putExtra("title", "سکه مجانی!");
        intent.putExtra("message", "کلیک کن تا سکه مجانی بگیری:)");
        intent.putExtra("activity", CoinStoreActivity.class.getName());

        scheduleIntent(context, 4, intent, timeMillis, -1);
    }

    // ---------------------------------------------------------------------------------

    private static void scheduleIntent(/*String name, LocalRepo localRepo, */Context context, int requestCode, Intent intent, long timeMillis, long interval) {
        context = context.getApplicationContext();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//        Intent lastIntent = localRepo.getIntent(name);
//        if (lastIntent != null) {
//            alarmManager.cancel(PendingIntent.getBroadcast(context, 0, lastIntent, PendingIntent.FLAG_CANCEL_CURRENT));
//        }

        if (interval < 0) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeMillis, interval, pendingIntent);
        }
    }

    private static long getTomarrowMillis(int hour, int minute) {
        DateTime firingTime = DateTime.now().withHourOfDay(hour).withMinuteOfHour(minute);
        if (firingTime.isBeforeNow()) {
            firingTime = firingTime.plusDays(1);
        }
        return firingTime.getMillis();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationUtils.showNotificationMessage(context, intent.getStringExtra("title"),
                intent.getStringExtra("message"), null, null, null, intent.getStringExtra("activity"));
    }
}
