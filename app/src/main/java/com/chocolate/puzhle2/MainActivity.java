package com.chocolate.puzhle2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.Utils.Utility;
import com.chocolate.puzhle2.Utils.sandbad;
import com.chocolate.puzhle2.events.AlarmReceiver;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.PublishDestination;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.ParseHelper;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import ir.tapsell.tapselldevelopersdk.developer.ConsumeProductInterface;
import ir.tapsell.tapselldevelopersdk.developer.DeveloperCtaInterface;
import ir.tapsell.tapselldevelopersdk.developer.TapsellDeveloperInfo;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static MainActivity instance = null;

    private ImageView imgCreateNot, imgSolveNot;
    public static boolean isRating = false;
    private DateTime lastRefreshTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdsUtils.initializeAds(getApplicationContext());
        setContentView(R.layout.activity_main);
        instance = this;

        setupTapsell();

        imgCreateNot = (ImageView) findViewById(R.id.notificationCreate);
        imgSolveNot = (ImageView) findViewById(R.id.notificationSolve);

        checkAndroid6Permissions();
        if(AppStatistics.publishDestination == PublishDestination.HamPay) {
            (new sandbad(this)).trackInstall();
        }

        if (uow.getLocalRepo().isFirstRun()) {
            dialogManager.showSetDisplayName(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    errorableBlock("Login", cid -> {
                        String displayName = dialog.getInputEditText().getText().toString();
                        ParseHelper.initializeUser(displayName, e -> {
                            if (handleError(cid, e, true)) {
                                uow.getLocalRepo().setIsFirstRun(false);
                                dialog.dismiss();
                                uow.getLocalRepo().resetLastFreeCoinGot();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.runOnUiThread(() -> {
                                            dialogManager.showMainActStart(() -> {
                                                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_create_type_refresh);
                                                animation.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {

                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        findViewById(R.id.button_solve).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_create_type_refresh));
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {

                                                    }
                                                });
                                                findViewById(R.id.button_create).startAnimation(animation);
                                            });
                                        });
                                    }
                                }, 3000);

                                AlarmReceiver.scheduleCreateAndSolveNotification(MainActivity.this, uow.getLocalRepo(), uow.getDrawingWordRepo());
                            }
                        });

                        return dialog;
                    }, dlg -> {
                        if (dlg != null) {
                            ((MaterialDialog) dlg).dismiss();
                        }
                    });
                }
            });
        } else {
            AlarmReceiver.scheduleCreateAndSolveNotification(this, uow.getLocalRepo(), uow.getDrawingWordRepo());
            showChocolateAds();
        }
//        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        // --------------------------------------------------------------------------------------------------
        handleIntent(getIntent());
    }

    private void checkAndroid6Permissions() {
        final String[] permissionsList = {
                "android.permission.GET_ACCOUNTS",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.READ_PHONE_STATE",
                "android.permission.ACCESS_COARSE_LOCATION"
        };
        final ArrayList<String> permissionsNeeded = new ArrayList<>();
        boolean shouldShowRequestPermissionRationale = false;

        for (String perm : permissionsList) {
            if(!TextUtils.isEmpty(perm)) {
                int permCheck = ContextCompat.checkSelfPermission(this, perm);
                if (permCheck != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded.add(perm);

                    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, perm);
                }
            }
        }

        if(shouldShowRequestPermissionRationale) {
            // hey you must allow game ...
            dialogManager.showApril("مجوزها رو فعال کن", "برای اینکه بازی روی گوشی های با اندروید ۶ کار کند، لطفا در دیالوگی که باز میشد allow یا اجازه داده شد را لمس نمایید.", "باشه", null );
        }

        if(permissionsNeeded.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == 0){
            if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                toastManager.show("برخی از قسمت های برنامه اجازه کار نگرفتند");
            }
        }
    }

    private void setupTapsell() {
        TapsellDeveloperInfo.getInstance().setDeveloperKey("njpnrjmltniileemjeesebobrqjmqgstbpjcrofmedfnjeifbfiqrocdqlcngbbqsmjkkr", this);

        TapsellDeveloperInfo.getInstance().setPurchaseNotifier((sku, purchaseId) -> {
            switch (sku) {
                case "tap_300":
                    consumeTapsell(sku, 300);
                    break;
                case "tap_600":
                    consumeTapsell(sku, 600);
                    break;
                case "tap_1200":
                    consumeTapsell(sku, 1200);
                    break;
                case "tap_3000":
                    consumeTapsell(sku, 3000);
                    break;
            }
        });
    }

    private void consumeTapsell(String sku, int coins){
        TapsellDeveloperInfo.getInstance().consumeProduct(sku, this, new ConsumeProductInterface() {
            @Override
            public void consumeProduct(String skuf, boolean consumed, boolean connected) {
                if(consumed)
                {
                    GameUser.getCurrScore().incrementCoinsCount(coins);
                    GameUser.getCurrScore().saveEventually();
                    EventBus.getDefault().post(new CoinsIncrementEvent(GameUser.getCurrScore().getCoinsCount()));
                }
                if(connected)
                    if (!consumed)
                        System.err.println("Product Is not Consumed");
                if(!connected)
                    System.err.println("Couldn't reach server");
            }
        });
    }

    private void showChocolateAds() {
        if (lastRefreshTime == null || lastRefreshTime.plusMinutes(30).isBeforeNow()) {
            GameUser.getGameUser().getStatistics().fetchInBackground((stats, e) -> {
                if (e == null) {
                    stats.pinInBackground((e2) -> {
                        AppStatistics statistics = GameUser.getGameUser().getStatistics();
                        if (uow.getLocalRepo().canShowAds(statistics.getFullAdsPic())) {
                            findViewById(R.id.ch_ads).setVisibility(View.VISIBLE);
                            Picasso.with(MainActivity.this).load(statistics.getFullAdsPic()).placeholder(R.drawable.banner_default).into((ImageView) findViewById(R.id.ch_ads_img));
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DeveloperCtaInterface.TAPSELL_DIRECT_ADD_REQUEST_CODE) {
            if (data == null
                    || !data.hasExtra(DeveloperCtaInterface.TAPSELL_DIRECT_CONNECTED_RESPONSE)
                    || !data.hasExtra(DeveloperCtaInterface.TAPSELL_DIRECT_AVAILABLE_RESPONSE)
                    || !data.hasExtra(DeveloperCtaInterface.TAPSELL_DIRECT_AWARD_RESPONSE)) {
                // User didn’t open ad
                return;
            }

            boolean connected = data.getBooleanExtra(DeveloperCtaInterface.TAPSELL_DIRECT_CONNECTED_RESPONSE, false);
            boolean available = data.getBooleanExtra(DeveloperCtaInterface.TAPSELL_DIRECT_AVAILABLE_RESPONSE, false);
            int award = data.getIntExtra(DeveloperCtaInterface.TAPSELL_DIRECT_AWARD_RESPONSE, -1);
            if (!connected) {
                // Couldn't connect to server
            }
            else if (!available) {
                // No such Ad was avaialbe
            } else {
                // user got {award} tomans. pay him!!!!
                if(award > 0) {
                    GameUser.getCurrScore().incrementCoinsCount(award);
                    GameUser.getCurrScore().saveEventually();
                    EventBus.getDefault().post(new CoinsIncrementEvent(GameUser.getCurrScore().getCoinsCount()));
                }
            }
        }
    }

    private void refreshCreateAndSolvesCount() { //todo
        // get two textboxes...
        int createCount = uow.getLocalRepo().getCanCreateCount(),
                solveCount = uow.getLocalRepo().getCanSolveCount();

        BiFunction<Integer, Integer> getIconRes = count -> {
            return count == 3 ? R.drawable.notification3 : count == 2 ?
                    R.drawable.notification2 : count == 1 ? R.drawable.notification1 : R.drawable.notification0;
        };

        imgCreateNot.setImageResource(getIconRes.run(createCount));
        imgSolveNot.setImageResource(getIconRes.run(solveCount));
    }

    private void handleIntent(Intent intent) {
        // Figure out what to do based on the intent type
        if (intent != null) {
            if (intent.hasExtra("activity")) {
                try {
                    AnalyzeUtil.track("notify", intent.getStringExtra("activity"));
                    startActivity(new Intent(this, Class.forName(intent.getStringExtra("activity"))).putExtra("fromPush", true));
                    return;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else if(intent.hasExtra("startAct")){
                intent.removeExtra("startAct");
                startActivity(intent);
            }

            if (intent.getType() != null && intent.getType().indexOf("image/") != -1) {
                Intent intent2 = new Intent(this, SolveType.class);
                intent2.putExtra("state", "solve_img"); //Optional parameters
                Uri data = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                intent2.setData(data);
                startActivity(intent2);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshCreateAndSolvesCount();

        if (isRating) {
            toastManager.youGotRateGift();
            UserScore userScore = GameUser.getGameUser().getScore();
            userScore.put("rated", true);
            userScore.pinInBackground();
            new Handler().postDelayed(() -> {
                moveTaskToBack(true);
            }, 2000);
            isRating = false;
        }

        if(GameUser.getCurrScore() != null) {
            EventBus.getDefault().post(new CoinsIncrementEvent(GameUser.getCurrScore().getCoinsCount()));
        }
    }

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        UserScore userStore = GameUser.getGameUser().getScore();
        if(userStore.getBoolean("rated") || userStore.getBoolean("feedback")) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                PicassoTools.clearCache(Picasso.with(this));
//                ParseQuery.clearAllCachedResults();

                moveTaskToBack(true);
                return;
            } else {
                Toast.makeText(getBaseContext(), "برای خروج یکبار دیگه دکمه بازگشت رو لمس کن", Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        } else {
            rateChoclate();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_create:
                AnalyzeUtil.track("button_create");

                int createCount = uow.getLocalRepo().getCanCreateCount();
                if (createCount > 0) {
                    Intent intent = new Intent(this, CreateTypeActivity.class);
                    checkNetAndStartAct(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                } else {
                    dialogManager.showSolveOneOrPay(uow.getLocalRepo(), () -> {
                        refreshCreateAndSolvesCount();
                        EventBus.getDefault().post(new CoinsIncrementEvent(GameUser.getGameUser().getScore().getCoinsCount()));
                        onClick(v);
                    });
                }
                break;
            case R.id.button_solve:
                AnalyzeUtil.track("button_solve");

                int solveCount = uow.getLocalRepo().getCanSolveCount();
                if (solveCount > 0) {
                    startActivity(new Intent(this, SolveType.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                } else {
                    dialogManager.showCreateOneOrPay(uow.getLocalRepo(), () ->
                    {
                        refreshCreateAndSolvesCount();
                        EventBus.getDefault().post(new CoinsIncrementEvent(GameUser.getGameUser().getScore().getCoinsCount()));
                        onClick(v);
                    });
                }
                break;
            case R.id.button_store:
                AnalyzeUtil.track("button_store");

                startActivity(new Intent(this, ItemsStoreActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.button_mypuzzle:
                AnalyzeUtil.track("button_mypuzzle");

                checkNetAndStartAct(new Intent(this, MyPuzzlesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.button_settings:
                AnalyzeUtil.track("button_settings");

                checkNetAndStartAct(new Intent(this, SettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.button_scoreboard:
                AnalyzeUtil.track("button_scoreboard");
                startActivity(new Intent(this, ScoreBoardActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.button_ranking:
                AnalyzeUtil.track("button_ranking");

                checkNetAndStartAct(new Intent(this, RankingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.button_achievements:
                AnalyzeUtil.track("button_achievements");

                checkNetAndStartAct(new Intent(this, AchievementsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                break;
            case R.id.ch_ads_img:
                if(!TextUtils.isEmpty(GameUser.getGameUser().getStatistics().getFullAdsLink())) {
                    ScoreBoardActivity.openLink(this, GameUser.getGameUser().getStatistics().getFullAdsLink());
                }
                findViewById(R.id.ch_ads).setVisibility(View.GONE);
                break;
            case R.id.ch_close_ads:
                findViewById(R.id.ch_ads).setVisibility(View.GONE);
                break;
//            case R.id.btn_tapsell:
//                DeveloperCtaInterface.getInstance().showNewCta(null, null, this);
//                break;
        }

        sfxPlayer.Play(SfxResource.Button);
    }

    private void checkNetAndStartAct(Intent intent) {
        if (!Utility.isConnectingToInternet(this)) {
            dialogManager.showServerReceiveError(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                }
            });
        }else{
            startActivity(intent);
        }
    }

    private void rateChoclate() {
        sfxPlayer.Play(SfxResource.Tip);
        final UserScore userStore = GameUser.getGameUser().getScore();
        final View likeDislikeView = findViewById(R.id.rate_start),
                likedView = findViewById(R.id.rate_liked),
                dislikedView = findViewById(R.id.rate_disliked);

        View.OnClickListener exitCallback = (v) -> {
            likeDislikeView.setVisibility(View.GONE);
            likedView.setVisibility(View.GONE);
            dislikedView.setVisibility(View.GONE);

            MainActivity.this.moveTaskToBack(true);
        };

        likeDislikeView.findViewById(R.id.close_rate_start).setOnClickListener(exitCallback);
        likedView.findViewById(R.id.close_rate_liked).setOnClickListener(exitCallback);
        dislikedView.findViewById(R.id.close_rate_disliked).setOnClickListener(exitCallback);

        likeDislikeView.findViewById(R.id.btn_rate_yes).setOnClickListener((v) -> {
            likeDislikeView.setVisibility(View.GONE);
            likedView.setVisibility(View.VISIBLE);

            likedView.findViewById(R.id.btn_rate_submit).setOnClickListener(v2 ->{
                likedView.setVisibility(View.GONE);
                if (Utility.rate(MainActivity.this)) {
                    MainActivity.isRating = true;
                }
            });
        });
        likeDislikeView.findViewById(R.id.btn_rate_no).setOnClickListener((v) -> {
            likeDislikeView.setVisibility(View.GONE);
            dislikedView.setVisibility(View.VISIBLE);

            dislikedView.findViewById(R.id.btn_rate_send).setOnClickListener(v2 ->{
                String feedBack = ((EditText)dislikedView.findViewById(R.id.txt_feedback)).getText().toString();
                if (!TextUtils.isEmpty(feedBack)) {
                    ParseObject parseObject = new ParseObject("Feedbacks");
                    parseObject.put("msg", feedBack);
                    parseObject.put("android_version", Build.VERSION.SDK_INT);
                    parseObject.put("model", Build.MODEL);
                    parseObject.put("user", GameUser.getGameUser());
                    parseObject.saveEventually();
                }
                userStore.put("feedback", true);
                userStore.pinInBackground();

                exitCallback.onClick(v2);
            });
        });
        likeDislikeView.setVisibility(View.VISIBLE);
    }
}
