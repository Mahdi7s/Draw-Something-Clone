package com.chocolate.puzhle2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.CustomViews.CTextView;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.MessageUtils;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.Utils.Utility;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;
import com.chocolate.puzhle2.events.LampsIncrementEvent;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.PublishDestination;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.GameUserRepo;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Random;

import de.greenrobot.event.EventBus;
import ir.adad.client.Adad;
import ir.devage.hamrahpay.HamrahPay;
import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;

public class CoinStoreActivity extends BaseActivity implements View.OnClickListener {
    // (arbitrary) request code for the purchase flow
    private static final int RC_REQUEST = 10001;
    private static final String TAG = "STORE";
    private static final String SKU_Coin_200 = "coin_200";
    private static final String SKU_Coin_450 = "coin_450";
    private static final String SKU_Coin_1200 = "coin_1200";
    private static final String SKU_Coin_3000 = "coin_3000";
    private static final String SKU_Coin_9000 = "coin_9000";
    private static final String SKU_Coin_20000 = "coin_20000";

    private static final String SKU_Coin_9000_off = "coin_9000_off";
    private static final String SKU_Coin_20000_off = "coin_20000_off";

    private static final String SKU_Premium = "vip";

    //-------------------------------------------------------------------------------

    private static final String SKU_Coin_450_H = "hp_56d53c40d0f4f608821097";//"hp_56d5db437c7ad895181538";//"hp_56d53c40d0f4f608821097";
    private static final String SKU_Coin_1200_H = "hp_56d53c88435d6235169045";
    private static final String SKU_Coin_3000_H = "hp_56d53c0a35e08890733776";
    private static final String SKU_Coin_9000_H = "hp_56d53be80812a765703041";
    private static final String SKU_Coin_20000_H = "hp_56d53bceb0036267520385";

    private static final String SKU_Coin_9000_off_H = "hp_56d53b6f5b21a402674940";
    private static final String SKU_Coin_20000_off_H = "hp_56d53b1fd1d2a646949839";

    private static final String SKU_Premium_H = "hp_56d53cba568d9023497693";

    private UserScore userScore = null;
    private View storeClickedBtn = null;

    private IabHelper mHelper;

    private MaterialDialog mProgressDialog;

    private DateTime freeGotTime = null;
    private boolean isRating = false, isIntroducing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_store);

        isInitialized = AppStatistics.publishDestination == PublishDestination.HamPay;

        userScore = GameUser.getGameUser().getScore();
        initDiscount();
        updateUi();

        Intent intent = getIntent();
        if (intent.getBooleanExtra("fromPush", false)) {
            GameUserRepo.isDirty = true;
            intent.putExtra("fromPush", false);

            errorableBlock("CoinsStore", cid -> {
                uow.getUserRepo().refreshGameUser((gameUser, e) -> {
                    if (handleError(cid, e, true)) {
                        userScore = gameUser.getScore();

                        initDiscount();
                        updateUi();
                    }
                });
                return null;
            }, dt -> {
            });
        }
        //----------------------------------------------------------
        freeGotTime = uow.getLocalRepo().getLastFreeCoinGot();
        updateFreeCoin();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateFreeCoin();

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void initDiscount() {
        final AppStatistics statistics = GameUser.getGameUser().getStatistics();
        if (statistics != null && statistics.isDiscountOn()) {
            findViewById(R.id.layout_normal_store).setVisibility(View.GONE);
            findViewById(R.id.layout_off_store).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_normal_store).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_off_store).setVisibility(View.GONE);
        }
    }


    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                onPurchaseSuccess(purchase.getSku());
            } else {
                complain("Error while consuming: " + result);
            }
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };

    private void onPurchaseSuccess(String sku) { //**********************************************************************
        String alertMsg = "";
        if (sku.startsWith("coin")) {
            int addedTokens = getCoinsOf(sku);
            userScore.incrementCoinsCount(addedTokens);
            alertMsg = addedTokens + " سکه به سکه هات اضافه شد. برو حالشو ببر :)";
        } else if (sku.equals(SKU_Premium)) {
            userScore.setPremium(); // unlock color pallete & remove Ads
            userScore.pinInBackground((e) -> {
//                updateUi();
                EventBus.getDefault().post(new EventMessage(EventMsgType.ColorPurchased, null, null));
            });
            alertMsg = "از شر تبلیغات خلاص شدی و پالت رنگتم کامل شد :)";
        }
        saveData();
        dialogManager.showPurchased(alertMsg);
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                //complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            switch (purchase.getSku()) {
                case SKU_Coin_200:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_450:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_1200:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_3000:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_9000:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_20000:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_9000_off:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Coin_20000_off:
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    break;
                case SKU_Premium:
                    saveData();
                    setWaitScreen(false);
                    onPurchaseSuccess(SKU_Premium);
                    break;
            }
        }
    };
    private boolean isInitialized = false;
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) {
                setWaitScreen(false);
                return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                setWaitScreen(false);
                complain("Failed to query inventory: " + result);
                return;
            }

            Purchase premiumPurchase = inventory.getPurchase(SKU_Premium); //******************************************************
            if (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase)) {
                userScore.setPremium();
                saveData();
            }

            updateUi();
            setWaitScreen(false);
            isInitialized = true;
            if (storeClickedBtn != null) {
                doPurchase(storeClickedBtn);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (isRating) {
            toastManager.youGotRateGift();
            userScore.put("rated", true);
            userScore.incrementCoinsCount(100);
            userScore.pinInBackground();
            EventBus.getDefault().post(new CoinsIncrementEvent(userScore.getCoinsCount()));
            isRating = false;

            updateUi();
        } else if (isIntroducing) {
            toastManager.youGotRateGift();
            userScore.put("introduce", true);
            userScore.incrementLampsCount(3);
            userScore.incrementCoinsCount(200);
            userScore.pinInBackground();
            EventBus.getDefault().post(new LampsIncrementEvent(userScore.getLampsCount()));
            EventBus.getDefault().post(new CoinsIncrementEvent(userScore.getCoinsCount()));
            isIntroducing = false;

            updateUi();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            final boolean removeAds = getIntent().getBooleanExtra("removeAds", false),
                    lamps = getIntent().getBooleanExtra("lamps", false),
                    rate = getIntent().getBooleanExtra("rate", true);
            getIntent().putExtra("removeAds", false);
            getIntent().putExtra("lamps", false);
            getIntent().putExtra("rate", false);

            if (removeAds) {
                new Handler().post(() -> {
                    ScrollView scrollView = ((ScrollView) findViewById(R.id.coin_store_scroll));
                    scrollView.smoothScrollTo(0, scrollView.getBottom() + scrollView.getMaxScrollAmount());
                    View btnPremium = findViewById(R.id.btn_store_premium);
                    btnPremium.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh));
                });
            } else {
                final UserScore score = GameUser.getGameUser().getScore();
                final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh);
                if (lamps && !score.getBoolean("introduce")) {
                    findViewById(R.id.btn_store_gift2_introduce).startAnimation(animation);
                    findViewById(R.id.btn_storeoff_gift2_introduce).startAnimation(animation);
                }
            }
        }
    }

    private void checkUnconsumables() {
        if (userScore.getBoolean("introduce")) {
            findViewById(R.id.frame_store_gift2_introduce).setVisibility(View.GONE);
            findViewById(R.id.frame_storeoff_gift2_introduce).setVisibility(View.GONE);
        }
        if (userScore.isPremium()) {
            findViewById(R.id.frame_store_premium).setVisibility(View.GONE);
            findViewById(R.id.frame_storeoff_premium).setVisibility(View.GONE);

            Adad.disableBannerAds();
        }
    }

    private void initializePurchase() {
        if (AppStatistics.publishDestination == PublishDestination.HamPay) {
            isInitialized = true;
            return;
        }
        if (isInitialized) return;
        // ---------------------------------------------------------------------------

        String base64EncodedPublicKey = getString(AppStatistics.getPublishPublicKey());

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);

        setWaitScreen(true);
        mHelper.startSetup(result -> {

            if (!result.isSuccess()) {
                setWaitScreen(false);
                complain("Problem setting up in-app billing: " + result);
                return;
            }

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) {
                setWaitScreen(false);
                return;
            }

            // IAB is fully set up. Now, let's get an inventory of stuff we own.
            Log.d(TAG, "Setup successful. Querying inventory.");
            mHelper.queryInventoryAsync(mGotInventoryListener);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    // updates UI to reflect model
    public void updateUi() {
        checkUnconsumables();
        EventBus.getDefault().post(new CoinsIncrementEvent(userScore.getCoinsCount()));
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
        if (AppStatistics.publishDestination == PublishDestination.HamPay) return;

        if (set && (mProgressDialog == null || !mProgressDialog.isShowing())) {
            mProgressDialog = dialogManager.showBazaarProgress();
        } else {
            mProgressDialog.dismiss();
        }
    }

    void complain(String message) {
        if (Utility.isConnectingToInternet(this)) {
            dialogManager.showLoginToBazaar();
        } else {
            dialogManager.showServerReceiveError(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    dialog.dismiss();
                }
            });
        }
    }

    void saveData() {
        userScore.pinInBackground((e) -> {
            EventBus.getDefault().post(new CoinsIncrementEvent(userScore.getCoinsCount()));
            updateUi();
        });
    }

    private int getCoinsOf(String pack) {
        int retval = 0;
        switch (pack) {
            case SKU_Coin_200:
                retval = 2000;
                break;
            case SKU_Coin_450:
            case SKU_Coin_450_H:
                retval = 4500;
                break;
            case SKU_Coin_1200:
            case SKU_Coin_1200_H:
                retval = 12000;
                break;
            case SKU_Coin_3000:
            case SKU_Coin_3000_H:
                retval = 30000;
                break;
            case SKU_Coin_9000:
            case SKU_Coin_9000_H:
                retval = 90000;
                break;
            case SKU_Coin_20000:
            case SKU_Coin_20000_H:
                retval = 200000;
                break;
            case SKU_Coin_9000_off:
            case SKU_Coin_9000_off_H:
                retval = 90000;
                break;
            case SKU_Coin_20000_off:
            case SKU_Coin_20000_off_H:
                retval = 200000;
                break;
        }
        return retval;
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.btn_store_gift2_introduce || v.getId() == R.id.btn_storeoff_gift2_introduce ||
                v.getId() == R.id.btn_store_daily_coin || v.getId() == R.id.btn_storeoff_daily_coin || isInitialized) {
            doPurchase(v);
        } else if (Utility.isMarketInstalled(this)) {
            storeClickedBtn = v;
            initializePurchase();
        }
        sfxPlayer.Play(SfxResource.Button);
    }

    private void doPurchase(View v) {
        switch (v.getId()) {
            case R.id.btn_store_coins450:
            case R.id.btn_storeoff_coins450:
                AnalyzeUtil.track("store_coins450");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_450, SKU_Coin_450_H);
                break;
            case R.id.btn_store_coins1200:
            case R.id.btn_storeoff_coins1200:
                AnalyzeUtil.track("store_coins1200");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_1200, SKU_Coin_1200_H);
                break;
            case R.id.btn_store_coins3000:
            case R.id.btn_storeoff_coins3000:
                AnalyzeUtil.track("store_coins3000");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_3000, SKU_Coin_3000_H);
                break;
            case R.id.btn_store_coins9000: // 9000
                AnalyzeUtil.track("store_coins9000");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_9000, SKU_Coin_9000_H);
                break;
            case R.id.btn_store_coins20000:
                AnalyzeUtil.track("store_coins20000");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_20000, SKU_Coin_20000_H);
                break;
            case R.id.btn_storeoff_coins9000: // 9000
                AnalyzeUtil.track("storeoff_coins9000");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_9000_off, SKU_Coin_9000_off_H);
                break;
            case R.id.btn_storeoff_coins20000:
                AnalyzeUtil.track("storeoff_coins20000");
                setWaitScreen(true);

                launchPurchase(SKU_Coin_20000_off, SKU_Coin_20000_off_H);
                break;
            case R.id.btn_store_premium:
            case R.id.btn_storeoff_premium:
                AnalyzeUtil.track("store_premium");
                setWaitScreen(true);

                launchPurchase(SKU_Premium, SKU_Premium_H);
                break;
            //---------------------------------------------------------------------------------------
            case R.id.btn_store_gift2_introduce:
            case R.id.btn_storeoff_gift2_introduce:
                AnalyzeUtil.track("gift2_introduce");

                isIntroducing = true;
                final String msg = (new MessageUtils()).getIntroduceMessage();
                Intent shareTxt = new Intent(Intent.ACTION_SEND);
                shareTxt.setType("text/plain");
                shareTxt.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(shareTxt);
                break;
            case R.id.btn_store_daily_coin:
            case R.id.btn_storeoff_daily_coin:
                final int gift = (3 + new Random().nextInt(4)) * 10;
                final UserScore score = GameUser.getGameUser().getScore();
                score.incrementCoinsCount(gift);
                score.pinInBackground();
                EventBus.getDefault().post(new CoinsIncrementEvent(score.getCoinsCount()));
                sfxPlayer.Play(SfxResource.BuyCoin);
                toastManager.show(gift + " سکه جایزه گرفتی:)");
                //-------------------------------------------------------------------------
                uow.getLocalRepo().resetLastFreeCoinGot();
                freeGotTime = uow.getLocalRepo().getLastFreeCoinGot();
                break;
        }
        storeClickedBtn = null;
    }

    private void launchPurchase(final String sku, final String hpSku) {
        if (AppStatistics.publishDestination != PublishDestination.HamPay) {
            String payload = "payload";
            mHelper.launchPurchaseFlow(CoinStoreActivity.this, sku, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } else {
            new HamrahPay(this).sku(hpSku)
                    .verificationType(HamrahPay.DEVICE_VERIFICATION)
                    .listener(new HamrahPay.Listener() {
                        @Override
                        public void onErrorOccurred(String s, String s1) {
                            toastManager.show("پرداخت ناموفق :(");
                        }

                        @Override
                        public void onPaymentSucceed(String s) {
                            onPurchaseSuccess(sku);
                        }
                    }).startPayment();
        }
    }

    private void updateFreeCoin() {
        runOnUiThread(() -> {
            final CTextView remianedTime = (CTextView) findViewById(R.id.daily_coin_time);
            final CTextView remianedTimeOff = (CTextView) findViewById(R.id.off_daily_coin_time);
            final Period period = new Period(DateTime.now(), freeGotTime);
            if (period.getMillis() <= 0) { // enable -> you can get gift
                findViewById(R.id.btn_store_daily_coin).setEnabled(true);
                findViewById(R.id.btn_storeoff_daily_coin).setEnabled(true);
                remianedTime.setText("کلیک کن! جایزه بگیر!");
                remianedTimeOff.setText(remianedTime.getText());
            } else {
                findViewById(R.id.btn_store_daily_coin).setEnabled(false);
                findViewById(R.id.btn_storeoff_daily_coin).setEnabled(false);
                remianedTime.setText(String.format("%02d:%02d:%02d", period.getHours(), period.getMinutes(), period.getSeconds()));
                remianedTimeOff.setText(remianedTime.getText());
            }
        });
    }
}
