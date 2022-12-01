package com.chocolate.puzhle2.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.chocolate.puzhle2.CoinStoreActivity;
import com.chocolate.puzhle2.MainActivity;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;

import org.joda.time.DateTime;
import org.json.JSONObject;

import ir.adad.client.AdListener;
import ir.adad.client.Adad;
import ir.adad.client.Banner;
import ir.adad.client.InterstitialAdListener;

/**
 * Created by mahdi on 10/6/15.
 */
public class AdsUtils {
    private static boolean isPreparing = false;
    private static boolean isReady = false;
    private static DateTime lastInterstitialShow = null;

    public static void initializeAds(Context appContext) {
//        try {
//            JSONObject json = new JSONObject();
//            json.put("PublicRelease", false);
//            Adad.executeCommand(json);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        Adad.initialize(appContext);

        if(!canShowAds()) {
            Adad.disableBannerAds();
            return;
        }
    }

    public static void setListenerForBanner(Activity activity, int bannerId) {
        if(!canShowAds()) return;

        ((Banner)activity.findViewById(bannerId)).setAdListener(bannerListener);
    }

    public static void prepareInterstitialAd(){
        prepareInterstitialAd(null);
    }

    private static Runnable onInterstitialAdLoaded = null;
    // this should be called every time before showing ads
    public static void prepareInterstitialAd(Runnable onAdsLoaded) {
        if(!canShowAds()) return;

        onInterstitialAdLoaded = onAdsLoaded;
        if (!isPreparing && !isReady) {
            Adad.prepareInterstitialAd(interstitialAdListener);
            isPreparing = true;
        }
    }

    public static void showInterstitialAd(Activity activity) {
        if(!canShowAds()) return;

        if(isReady && (lastInterstitialShow == null || lastInterstitialShow.plusMinutes(10).isBeforeNow())) {
            Adad.showInterstitialAd(activity);
            isReady = false;
        }
    }

    private static boolean canShowAds(){
        UserScore score = GameUser.getGameUser().getScore();
        return score == null || !score.isPremium();
    }

    private static void removeAds() {
        AnalyzeUtil.track("removeAds_adad");

        Intent intent = new Intent(MainActivity.instance, CoinStoreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("removeAds", true);
        MainActivity.instance.startActivity(intent);
    }

    private static AdListener bannerListener = new AdListener() {
        @Override
        public void onAdLoaded() {

        }

        @Override
        public void onAdFailedToLoad() {

        }

        @Override
        public void onMessageReceive(JSONObject jsonObject) {

        }

        @Override
        public void onRemoveAdsRequested() {
            removeAds();
        }
    };

    private static InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
        @Override
        public void onInterstitialAdDisplayed() {
            lastInterstitialShow = DateTime.now();
        }

        @Override
        public void onInterstitialClosed() {

        }

        @Override
        public void onAdLoaded() {
            isPreparing = false;
            isReady = true;
            if(onInterstitialAdLoaded != null){
                onInterstitialAdLoaded.run();
            }
        }

        @Override
        public void onAdFailedToLoad() {
            isPreparing = false;
        }

        @Override
        public void onMessageReceive(JSONObject jsonObject) {

        }

        @Override
        public void onRemoveAdsRequested() {
            removeAds();
        }
    };
}
