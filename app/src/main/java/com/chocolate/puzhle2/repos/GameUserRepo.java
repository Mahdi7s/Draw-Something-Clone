package com.chocolate.puzhle2.repos;

import android.content.Context;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.SettingsActivity;
import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.events.LampsIncrementEvent;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.GetCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by choc01ate on 5/11/2015.
 */
public class GameUserRepo extends BaseRepo<GameUser> {
    public static boolean isDirty = true;
    private static DateTime lastRefreshTime = null;

    public GameUserRepo(Context context, DialogManager dialogManager, ToastManager toastManager) {
        super(context, dialogManager, toastManager, GameUser.class);
    }

//    public boolean isFirstRun() {
//        return GameUser.getGameUser() == null || GameUser.getCurrScore() == null || GameUser.getCurrScore().getDisplayName() == null || GameUser.getCurrScore().getDisplayName().isEmpty();
//    }

    public boolean isUserLoggedIn() {
        final String email = ParseUser.getCurrentUser().getEmail();
        return !ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser()) && !TextUtils.isEmpty(email);
    }

    public static boolean isRefreshed = false;

    public void refreshGameUser(GetCallback<GameUser> callback) {
        refreshGameUser(false, callback);
    }

    public void refreshGameUser(boolean isLogin, GetCallback<GameUser> callback) {
        GameUser gameUser = GameUser.getGameUser();
        if (isDirty || lastRefreshTime == null || lastRefreshTime.plusMinutes(7).isBeforeNow()) {
            if (!isLogin) {
                gameUser.getScore().fetchInBackground((score, e) -> {
                    if (e == null) {
                        isDirty = false;
                        isRefreshed = true;
                        lastRefreshTime = DateTime.now();

                        EventBus.getDefault().post(new CoinsIncrementEvent(((UserScore) score).getCoinsCount()));
                        EventBus.getDefault().post(new LampsIncrementEvent(((UserScore) score).getLampsCount()));

                        score.pinInBackground();//  *****************************************

                        callback.done(gameUser, e);
                    } else {
                        callback.done(null, e);
                    }
                });
            } else {
                ParseQuery.getQuery(GameUser.class).include("Score").include("Statistics").getInBackground(gameUser.getObjectId(), (nGameUser, e) -> {
                    if (e == null) {
                        ParseObject.pinAllInBackground(Arrays.asList(nGameUser, nGameUser.getParseObject("Score"), nGameUser.getParseObject("Statistics")), e2 -> {
                            isDirty = false;
                            isRefreshed = true;
                            lastRefreshTime = DateTime.now();

                            EventBus.getDefault().post(new CoinsIncrementEvent(nGameUser.getScore().getCoinsCount()));
                            EventBus.getDefault().post(new LampsIncrementEvent(nGameUser.getScore().getLampsCount()));
                            callback.done(nGameUser, e2);
                        });
                    } else {
                        callback.done(null, e);
                    }
                });
            }
        } else {
            callback.done(gameUser, null);
        }
    }

    public void attachToGooglePlusMail(final SettingsActivity settingsActivity, final String email, final MaterialDialog progressDlg) {
        HashMap<String, Object> parms = new HashMap<>();
        parms.put("email", email);

        ParseQuery.getQuery(ParseUser.class).whereEqualTo("username", email).getFirstInBackground((gu, e) -> {
            if (e == null && gu != null) { // login
                dialogManager.showLoginDialog(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog dialog) {
                        final String password = email.hashCode() + "";
                        ParseUser.logInInBackground(email, password, (parseUser, e1) -> {
                            dialog.dismiss();
                            if (e1 != null) {
                                e1.printStackTrace();
                                progressDlg.dismiss();
                                settingsActivity.disconnectAccount();
                                settingsActivity.processUIUpdate(false);
                            } else {
                                parseUser.pinInBackground((pe) -> {
                                    isDirty = true;
                                    refreshGameUser(true, (guser, e2) -> {
                                        if (e2 == null) {
                                            toastManager.successfulLogin();
                                            settingsActivity.processUIUpdate(true);
                                        }
                                        progressDlg.dismiss();
                                    });
                                });
                            }
                        });
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        settingsActivity.disconnectAccount();
                        progressDlg.dismiss();
                        dialog.dismiss();
                    }
                });
            } else { // signup
                dialogManager.showSignupDialog(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        final GameUser gameUser = GameUser.getGameUser();
                        gameUser.setUsername(email);
                        gameUser.setPassword(email.hashCode() + "");
                        gameUser.signUpInBackground(e1 -> {
                            progressDlg.dismiss();
                            if (e1 != null) {
                                e1.printStackTrace();
                                settingsActivity.disconnectAccount();
                                settingsActivity.processUIUpdate(false);
                            } else {
                                gameUser.setEmail(email);
                                gameUser.saveEventually();
                                toastManager.successfulLogin();
                                settingsActivity.processUIUpdate(true);
                            }
                        });
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        settingsActivity.disconnectAccount();
                        progressDlg.dismiss();
                        dialog.dismiss();
                    }
                });
            }
        });
    }
}
