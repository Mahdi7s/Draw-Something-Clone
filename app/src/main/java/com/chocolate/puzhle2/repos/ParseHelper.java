package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.events.LampsIncrementEvent;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

/**
 * Created by choc01ate on 5/10/2015.
 */
public class ParseHelper {
    public static void initialize1(Context context) {
        context = context.getApplicationContext();

//        ParseCrashReporting.enable(context);

        ParseObject.registerSubclass(GameUser.class);
        ParseObject.registerSubclass(UserScore.class);
        ParseObject.registerSubclass(UserPuzzle.class);
        ParseObject.registerSubclass(DrawingWord.class);
        ParseObject.registerSubclass(AppStatistics.class);

        Parse.enableLocalDatastore(context);

        Parse.Configuration.Builder configBuilder = new Parse.Configuration.Builder(context);
        configBuilder.applicationId(context.getString(R.string.parse_app_id))
                .clientKey(context.getString(R.string.parse_client_key))
                .server("http://server.choc01ate.com/puzhle/")
                .enableLocalDataStore();

        Parse.initialize(configBuilder.build());

        GameUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    public static void initializeUser(final String displayName, SaveCallback callback) {
        final GameUser gameUser = GameUser.getGameUser();

        final UserScore userScore = new UserScore();
        userScore.init(displayName);

        userScore.refreshAchievements();

        ParseQuery.getQuery(AppStatistics.class).getFirstInBackground((statistics, e3) -> {
            if (e3 == null) {
                gameUser.setStatistics(statistics);
                gameUser.saveInBackground(e -> {
                    if (e != null) { // check statistics data
                        callback.done(e);
                    } else {
                        userScore.setUserId(gameUser.getObjectId());
                        gameUser.setScore(userScore);
                        gameUser.saveInBackground((e4) -> {
                            if (e4 == null) {
                                ParseObject.pinAllInBackground(Arrays.asList(gameUser, statistics, userScore));
                                ParsePush.subscribeInBackground("Puzzler");

                                EventBus.getDefault().post(new CoinsIncrementEvent(userScore.getCoinsCount()));
                                EventBus.getDefault().post(new LampsIncrementEvent(userScore.getLampsCount()));
                            }
                            callback.done(e4);
                        });

                        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
                        parseInstallation.put("user", gameUser);
                        parseInstallation.saveEventually();
                    }
                });
            } else {
                callback.done(e3);
            }
        });
    }

        public static void initialize2(LocalRepo userRepo, SaveCallback callback) { // todo we need this on each application run?!
        if (userRepo.isFirstRun()) {
            GameUser gameUser = GameUser.getGameUser();
            ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
            parseInstallation.put("user", GameUser.getGameUser());
            ParseQuery.getQuery(AppStatistics.class).getFirstInBackground((statistics, e3) -> {
                if (e3 == null) {
                    gameUser.setStatistics(statistics);
                    parseInstallation.saveInBackground(e -> {
                        if (e != null) { // check statistics data
                            callback.done(e);
                        } else {
                            ParseObject.pinAllInBackground(Arrays.asList(gameUser, statistics));
                            ParsePush.subscribeInBackground("Puzzler", e2 -> {
                                callback.done(e2);
                            });
                        }
                    });
                } else {
                    callback.done(e3);
                }
            });
        } else {
            callback.done(null);
        }
    }
}
