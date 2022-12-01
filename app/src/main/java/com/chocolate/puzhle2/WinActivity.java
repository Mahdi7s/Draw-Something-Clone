package com.chocolate.puzhle2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.Asyncer;
import com.chocolate.puzhle2.Utils.BitmapUtility;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.Utils.Utility;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WinActivity extends BaseActivity implements View.OnClickListener {
    private File puzzleFile = null;
    private int newW = 0, newH = 0;
    private UserPuzzle userPuzzle = null;
    private int lampsUsed = 0;
    private int solveSecs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winboard);

        final Intent intent = getIntent();
        solveSecs = intent.getIntExtra("solveSecs", 0);
        lampsUsed = intent.getIntExtra("lampsUsed", 0);
        newW = intent.getIntExtra("newW", 0);
        newH = intent.getIntExtra("newH", 0);
        puzzleFile = (File) intent.getSerializableExtra("puzzleFile");
        String pid = intent.getStringExtra("puzzleId");
        userPuzzle = ParseObject.createWithoutData(UserPuzzle.class, pid);

        Asyncer.runAsync(new Asyncer.ResultProvider<Object>() {
            @Override
            public Object runAsync(Object... params) {
                try {
                    userPuzzle.fetchFromLocalDatastore();
                    UserScore us = ParseObject.createWithoutData(UserScore.class, intent.getStringExtra("scoreId"));
                    us.fetchFromLocalDatastore();
                    userPuzzle.setCreatorScore(us);

                    if (intent.hasExtra("wordId")) {
                        DrawingWord dw = ParseObject.createWithoutData(DrawingWord.class, intent.getStringExtra("wordId"));
                        dw.fetchFromLocalDatastore();
                        userPuzzle.setPuzzleWord(dw);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return userPuzzle;
            }
        }, new Asyncer.ResultConsumer<Object>() {
            @Override
            public void postRun(Object result) {
                if (result != null) {
                    AdsUtils.prepareInterstitialAd();
                    setupWin();
                }
            }
        });

//        if (userPuzzle != null) {
//            AdsUtils.prepareInterstitialAd();
//            setupWin();
//        }
    }

    private List<UserScore> otherSolvers = null;

    private void setupWin() {
        BitmapUtility.resizeInto(this, puzzleFile, newW, newH, (ImageView) findViewById(R.id.designer_picpuzz));
        ((TextView) findViewById(R.id.designer_likes)).setText(" " + userPuzzle.getLikes());

        final UserScore score = userPuzzle.getCreatorScore();

        ((TextView) findViewById(R.id.designer_name)).setText(score.getDisplayName());
        ((TextView) findViewById(R.id.designer_score)).setText(score.getTotalScore() + "");

        int leagueRes = getLeagueIcon(score.getLeague());
        ((ImageView) findViewById(R.id.designer_league)).setImageResource(leagueRes);

        GameUser solver = GameUser.getGameUser();
        UserScore solverScore = solver.getScore();

//        errorableBlock("WinFetch", cid -> { // object not found err !
        userPuzzle.getSolverScores().getQuery().setLimit(3).findInBackground((data, e2) ->
        {
//                if (handleError(cid, e2, true)) {
            if (e2 == null) {
                otherSolvers = new ArrayList<>();
                for (UserScore s : data) {
                    if (!s.getObjectId().equals(solverScore.getObjectId())) {
                        otherSolvers.add(s);
                    }
                }

                if (otherSolvers.size() >= 1) {
                    UserScore score1 = otherSolvers.get(0);
                    ((TextView) findViewById(R.id.name1)).setText(score1.getDisplayName());
                    ((TextView) findViewById(R.id.score1)).setText(score1.getTotalScore() + "");
                    ((ImageView) findViewById(R.id.league1)).setImageResource(getLeagueIcon(score1.getLeague()));
                }
                if (otherSolvers.size() >= 2) {
                    UserScore score1 = otherSolvers.get(1);
                    ((TextView) findViewById(R.id.name2)).setText(score1.getDisplayName());
                    ((TextView) findViewById(R.id.score2)).setText(score1.getTotalScore() + "");
                    ((ImageView) findViewById(R.id.league2)).setImageResource(getLeagueIcon(score1.getLeague()));
                }
                if (otherSolvers.size() >= 3) {
                    UserScore score1 = otherSolvers.get(2);
                    ((TextView) findViewById(R.id.name3)).setText(score1.getDisplayName());
                    ((TextView) findViewById(R.id.score3)).setText(score1.getTotalScore() + "");
                    ((ImageView) findViewById(R.id.league3)).setImageResource(getLeagueIcon(score1.getLeague()));
                }

                int userOrder = userPuzzle.getSolves();
                userOrder = otherSolvers.size() < 3 ? otherSolvers.size() + 1 : userOrder;

                ((TextView) findViewById(R.id.sName)).setText(solverScore.getDisplayName());
                ((TextView) findViewById(R.id.sScore)).setText(solverScore.getTotalScore() + "");
                ((TextView) findViewById(R.id.sNumber)).setText(" ." + userOrder);
                ((ImageView) findViewById(R.id.sLeague)).setImageResource(getLeagueIcon(solverScore.getLeague()));

                if (userOrder <= 3) {
                    findViewById(R.id.user3Row).setVisibility(View.GONE);
                }
                if (userOrder <= 2) {
                    findViewById(R.id.user2Row).setVisibility(View.GONE);
                }
                if (userOrder <= 1) {
                    findViewById(R.id.user1Row).setVisibility(View.GONE);
                }

                // __________________________________________________________________________

                final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh);
                animation.setRepeatCount(100);
                animation.setRepeatMode(Animation.REVERSE);
                findViewById(R.id.btn_like).startAnimation(animation);

                if (uow.getLocalRepo().isActFirstSeen(this)) {
                    new Handler().postDelayed(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      dialogManager.showApril(R.string.win_intro_title, R.string.win_intro, -1, null);
                                                  }
                                              }, 3000);
                } else if (uow.getLocalRepo().isFirstSeen("winMsg2")) {
                    new Handler().postDelayed(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      dialogManager.showApril(R.string.win_intro_title2, R.string.win_intro2, -1, null);
                                                  }
                                              }, 3000);
                }
            }
//                }
        });

//            return null;
//        }, dt -> {
//        });
    }

    public static int getLeagueIcon(int league) {
        int leagueRes = -1;
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
        return leagueRes;
    }

    @Override
    public void onBackPressed() {
        dialogManager.showReturnFromWin(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                final ViewGroup likePuzzlePanel = (ViewGroup) findViewById(R.id.likePuzzlePanel);
                if (likePuzzlePanel.isEnabled()) {
                    solvePuzzle(findViewById(R.id.btn_like).getTag().equals("liked"));
                }
                //---------------------------------------------------------------------------------------
                Intent intent = new Intent(WinActivity.this.getApplicationContext(), MainActivity.class);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT > 10) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    intent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.likePuzzlePanel:
                if(!getIntent().getBooleanExtra("liked", false)) {
                    TextView txtLike = (TextView) findViewById(R.id.designer_likes);
                    ImageView likeImg = (ImageView) findViewById(R.id.btn_like);

                    if (likeImg.getTag().equals("disliked")) {
                        txtLike.setText(" " + (userPuzzle.getLikes() + 1));
                        likeImg.setImageResource(R.drawable.like_icon);
                        likeImg.setTag("liked");
                    } else {
                        txtLike.setText(" " + userPuzzle.getLikes());
                        likeImg.setImageResource(R.drawable.like_icon_empty);
                        likeImg.setTag("disliked");
                    }
                }
                break;
            case R.id.btn_share:
                BiFunction<Object, Object> shareFunc = p -> {
                    view.setVisibility(View.INVISIBLE);
                    findViewById(R.id.btn_complaint).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btn_send_puzzle).setVisibility(View.INVISIBLE);

                    LinearLayout winLayout = (LinearLayout) findViewById(R.id.frameWin);
                    winLayout.setBackgroundColor(Color.parseColor("#64b8ff"));
                    winLayout.setDrawingCacheEnabled(true);
                    winLayout.buildDrawingCache();

                    Bitmap bmp = winLayout.getDrawingCache();
                    FileUtility.shareWin(this, uow, userPuzzle, bmp);
                    bmp.recycle();

                    winLayout.setDrawingCacheEnabled(false);
                    view.setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_complaint).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_send_puzzle).setVisibility(View.VISIBLE);
                    winLayout.setBackgroundColor(Color.TRANSPARENT);
                    return null;
                };

                final ViewGroup likePuzzlePanel = (ViewGroup) findViewById(R.id.likePuzzlePanel);
                if (likePuzzlePanel.isEnabled()) { // save like
                    boolean liked = findViewById(R.id.btn_like).getTag().equals("liked");
                    if (liked) {
                        solvePuzzle(liked);
                        likePuzzlePanel.setEnabled(false);
                        shareFunc.run(null);
                    } else {
                        shareFunc.run(null);
                    }
                } else {
                    shareFunc.run(null);
                }
                break;
            case R.id.btn_send_puzzle:
                FileUtility.shareImage(this, uow, userPuzzle, new File[]{puzzleFile}, true);
                break;
            case R.id.btn_complaint:
                dialogManager.showComplaint(() -> {
                    userPuzzle.incrementComplaints();
                    userPuzzle.saveInBackground(e -> {
                        if (e != null) {
                            userPuzzle.saveEventually();
                        }
                    });
                    view.setVisibility(View.INVISIBLE);
                    toastManager.show("اعتراضت ثبت شد.");
                });
                break;

            case R.id.winCreatorPanel:
                goToProfile(userPuzzle.getCreatorScore());
                break;
            case R.id.user1Row:
                goToProfile(otherSolvers.get(0));
                break;
            case R.id.user2Row:
                goToProfile(otherSolvers.get(1));
                break;
            case R.id.user3Row:
                goToProfile(otherSolvers.get(2));
                break;
        }
    }

    private void goToProfile(UserScore userScore) {
        if (userScore != null) {
            ProfileActivity.userScore = userScore;
            startActivity(new Intent(this, ProfileActivity.class));
        }
    }

    private void solvePuzzle(boolean liked) {
        if(!getIntent().getBooleanExtra("liked", false)) {
            if (liked) {
                userPuzzle.incrementLikes();
                userPuzzle.getLikerScores().add(GameUser.getCurrScore());
                userPuzzle.getCreatorScore().incrementLikesCount();
            }
            uow.getPuzzleRepo().solvedPuzzle(userPuzzle, lampsUsed, solveSecs);
        }
    }
}
