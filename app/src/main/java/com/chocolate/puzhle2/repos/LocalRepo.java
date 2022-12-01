package com.chocolate.puzhle2.repos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.chocolate.puzhle2.events.AlarmReceiver;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mahdi on 5/23/15.
 */
public class LocalRepo {
    private Context context;
    private SharedPreferences sharedPreferences;

    private static Map<String, String> dataMap = new HashMap<>();

    public LocalRepo(Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = this.context.getSharedPreferences("app_data", Context.MODE_PRIVATE);
    }

    public int getLastPackageRecieved() {
        int lastPackageRecieved = 0;
        if (getData("LastPackageRecieved") != null) {
            lastPackageRecieved = Integer.parseInt(getData("LastPackageRecieved"));
        }
        return lastPackageRecieved;
    }

    public void setLastPackageRecieved(int lastPackageRecieved) {
        setData("LastPackageRecieved", lastPackageRecieved + "");
    }

    public void setSharePuzzleText(boolean share) {
        setData("SharePuzzleText", Boolean.toString(share));
    }

    public boolean getSharePuzzleText() {
        String retval = getData("SharePuzzleText");
        if (TextUtils.isEmpty(retval)) {
            setSharePuzzleText(true);
            return true;
        }
        return Boolean.parseBoolean(retval);
    }

    public void setMute(boolean mute) {
        setData("Mute", Boolean.toString(mute));
    }

    public boolean getMute() {
        String retval = getData("Mute");
        return Boolean.parseBoolean(retval);
    }

    public int getCanCreateCount() {
        String str = getData("CanCreateCount");
        if (!TextUtils.isEmpty(str)) {
            return Integer.parseInt(str);
        } else {
            setCanCreateCount(3);
        }
        return 3;
    }

    public void setCanCreateCount(int count) {
        setData("CanCreateCount", count + "");
    }

    public int getCanSolveCount() {
        String str = getData("CanSolveCount");
        if (!TextUtils.isEmpty(str)) {
            return Integer.parseInt(str);
        } else {
            setCanSolveCount(3);
        }
        return 3;
    }

    public void setCanSolveCount(int count) {
        setData("CanSolveCount", count + "");
    }

    public void saveIntent(String name, Intent intent) {
        if (intent == null) {
            setData(name, "");
        } else {
            setData(name, intent.getClass().getName() + "|" + intent.getAction() + "|" + intent.getData().getPath() + "|" + intent.getType());
        }
    }

    public Intent getIntent(String name) {
        String intentStr = getData(name);
        if (TextUtils.isEmpty(intentStr)) return null;

        String[] params = intentStr.split("|");
        try {
            Intent intent = new Intent(context.getApplicationContext(), Class.forName(params[0]));
            if (!TextUtils.isEmpty(params[1])) {
                intent.setAction(params[1]);
            }
            if (!TextUtils.isEmpty(params[2])) {
                intent.setData(Uri.parse(params[2]));
            }
            if (!TextUtils.isEmpty(params[3])) {
                intent.setType(params[3]);
            }

            return intent;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setData(String prop, String val) {
        SharedPreferences.Editor spEdit = sharedPreferences.edit();
        spEdit.putString(prop, val);
        commitOrApply(spEdit);
        dataMap.put(prop, val);
    }

    private String getData(String prop) {
        if (dataMap.containsKey(prop)) {
            return dataMap.get(prop);
        }

        String retval = sharedPreferences.getString(prop, null);
        if (retval != null) {
            retval = retval;
        }
        return retval;
    }

    private void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public boolean isActFirstSeen(Activity activity) {
        final String prop = "ActSeen" + activity.getClass().getSimpleName();
        return isFirstSeen(prop);
    }

    public boolean isFirstSeen(String prop) {
        final String email = ParseUser.getCurrentUser().getEmail();
        boolean isLoggedIn = !ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser()) && !TextUtils.isEmpty(email);
        if (isLoggedIn) {
            return false;
        }

        String seen = getData(prop);
        setData(prop, Boolean.toString(true));

        return TextUtils.isEmpty(seen) || seen.equals(Boolean.toString(false));
    }

    public void setSolveRandomCount(int solveRandomCount) {
        setData("SolveRandomCount", Math.max(0, Math.min(solveRandomCount, 4)) + "");
    }

    public int getSolveRandomCount() {
        try {
            String retStr = getData("SolveRandomCount");
            return Integer.parseInt(retStr);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setSolvedFeaturedPuzzles(int solveCount) {
        setData("SolvedFeaturedPuzzles", solveCount + "");
    }

    public int getSolvedFeaturedPuzzles() {
        try {
            String retStr = getData("SolvedFeaturedPuzzles");
            return Integer.parseInt(retStr);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public int getFreeSolveCount() {
        try {
            String retStr = getData("FreeSolveCount");
            return Integer.parseInt(retStr);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void incFreeSolveCount() {
        setData("FreeSolveCount", (getFreeSolveCount() + 1) + "");
    }

    public void setRankTab(String rankTab) {
        setData("RankTab", rankTab);
    }

    public String getRankTab() {
        final String ret = getData("RankTab");
        return (ret != null && ret.equals("Mine")) ? "Mine" : "All";
    }

    public DateTime getLastFreeCoinGot() {
        if (TextUtils.isEmpty(getData("LastFreeCoinGot"))) {
            resetLastFreeCoinGot();
        }
        return new DateTime(Long.parseLong(getData("LastFreeCoinGot")));
    }

    public void resetLastFreeCoinGot() {
        final long millis = DateTime.now().plusHours(8).getMillis();
        setData("LastFreeCoinGot", millis + "");
        AlarmReceiver.scheduleFreeCoin(context, millis);
    }

    public boolean canShowAds(String fullAdsPic) {
        String retStr = getData("FullAdsPic");
        if (!TextUtils.isEmpty(fullAdsPic) && (TextUtils.isEmpty(retStr) || !retStr.equals(fullAdsPic))) {
            setData("FullAdsPic", fullAdsPic);
            return true;
        }
        return false;
    }

    public void setIsFirstRun(boolean firstRun) {
        setData("IsFirstRun", Boolean.toString(firstRun));
    }

    public boolean isFirstRun() {
        String retStr = getData("IsFirstRun");
        if (TextUtils.isEmpty(retStr) || retStr.equals(Boolean.toString(true))) {
            return true;
        }
        return false;
    }
}
