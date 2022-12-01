package com.chocolate.puzhle2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.Utils.ProgressImage;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.models.UserStatistics;
import com.chocolate.puzhle2.repos.UserScoreRepo;

import java.util.List;

public class ScoreBoardActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);
        AdsUtils.setListenerForBanner(this, R.id.banner_ad_view);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.progressbar_1);
        ProgressImage img = new ProgressImage(bmp, 0.5f);
        ((ImageView) findViewById(R.id.progress_fill)).setImageDrawable(img);

        updateView(this, GameUser.getCurrScore());
        findViewById(R.id.edit_name).setVisibility(GameUser.getCurrScore().getDisplayNameChangeCount() < 3 ? View.VISIBLE : View.GONE);
        errorableBlock("ScoreBoard", cid -> {
            uow.getScoreRepo().getUserAchievements((achievementViewModels, e) -> {
                if (handleError(cid, e, true)) {
                    updateView(this, GameUser.getCurrScore());

                    showAchieveApril(achievementViewModels);
                }
                return null;
            });
            return null;
        }, dt -> {
        });
    }

    private void showAchieveApril(List<UserScoreRepo.AchievementViewModel> achievementViewModels) {
        for (UserScoreRepo.AchievementViewModel avm : achievementViewModels) {
            final String msg = avm.getGotNotify();
            if (!TextUtils.isEmpty(msg)) {
                dialogManager.showApril("یه مژده :)", msg, "هوراا", null);
                break;
            }
        }
    }

    public static void updateView(Activity a, UserScore userScore) {
        ((TextView) a.findViewById(R.id.txtCreatedPuzzles)).setText(" " + userScore.getCreatedCount());
        ((TextView) a.findViewById(R.id.txtSolvedPuzzles)).setText(" " + userScore.getSolvedCount());
        ((TextView) a.findViewById(R.id.txtLikedPuzzles)).setText(" " + userScore.getLikesCount());

        final int league = userScore.getLeague();
        int minScore = 0, maxScore = 0, leagueRes = 0;
        String leagueName = "";
        switch (league) {
            case 0:
                leagueRes = R.drawable.progressbar_1;
                leagueName = "آکبند";
                minScore = 0;
                maxScore = UserStatistics.League1ScoreNeed;
                break;
            case 1:
                leagueRes = R.drawable.progressbar_2;
                leagueName = "آی کیو";
                minScore = UserStatistics.League1ScoreNeed;
                maxScore = UserStatistics.League2ScoreNeed;
                break;
            case 2:
                leagueRes = R.drawable.progressbar_3;
                leagueName = "عقل کل";
                minScore = UserStatistics.League2ScoreNeed;
                maxScore = UserStatistics.League3ScoreNeed;
                break;
            case 3:
                leagueRes = R.drawable.progressbar_4;
                leagueName = "دانشمند";
                minScore = UserStatistics.League3ScoreNeed;
                maxScore = UserStatistics.League4ScoreNeed;
                break;
            case 4:
                leagueRes = R.drawable.progressbar_5;
                leagueName = "نابغه";
                minScore = UserStatistics.League4ScoreNeed;
                maxScore = UserStatistics.League5ScoreNeed;
                break;
            case 5:
                leagueRes = R.drawable.progressbar_6;
                leagueName = "اعجوبه";
                minScore = UserStatistics.League5ScoreNeed;
                maxScore = UserStatistics.League6ScoreNeed;
                break;
        }

        TextView txtTotalScore = (TextView) a.findViewById(R.id.total_score);
        txtTotalScore.setText((userScore.getTotalScore()) + " / " + maxScore);

        TextView txtLeague = (TextView) a.findViewById(R.id.league_subject);
        txtLeague.setText("لیگ «" + leagueName + "»");

        final Bitmap bmpTemp = BitmapFactory.decodeResource(a.getResources(), leagueRes);
        int iconWidth = bmpTemp.getWidth() / 4,
                progressWidth = bmpTemp.getWidth() - iconWidth,
                scoreVal = userScore.getTotalScore() - minScore,
                scoreMaxVal = maxScore - minScore,
                currProgress = (progressWidth * scoreVal) / scoreMaxVal;
        final Bitmap bitmap = Bitmap.createBitmap(bmpTemp, 0, 0, iconWidth + currProgress, bmpTemp.getHeight());
        final Bitmap bmpProgress = Bitmap.createBitmap(bmpTemp.getWidth(), bmpTemp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpProgress);
        bmpTemp.recycle();

        canvas.drawBitmap(bitmap, 0, 0, new Paint());
        final ImageView imgv = (ImageView) a.findViewById(R.id.progress_fill);
        imgv.setImageBitmap(bmpProgress);

        // ---------------------------------------------------------------------------

        updateProfile(a, userScore);
    }

    private static boolean isDirty = false;

    private static void updateProfile(Activity a, UserScore userScore) {
        ((TextView) a.findViewById(R.id.player_name)).setText(userScore.getDisplayName());
        final String signature = userScore.getSignature(),
                link = userScore.getLink();

        ((TextView) a.findViewById(R.id.player_signature)).setText(TextUtils.isEmpty(signature) ? "امضا" : signature);
        ((TextView) a.findViewById(R.id.player_link)).setText(TextUtils.isEmpty(link) ? "لینک" : link);

        isDirty = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isDirty) {
            GameUser.getCurrScore().saveInBackground(e -> {
                if (e != null) {
                    GameUser.getCurrScore().saveEventually();
                }
            });
            isDirty = false;
        }
    }

    @Override
    public void onClick(View view) {
        final GameUser gameUser = GameUser.getGameUser();
        switch (view.getId()) {
            case R.id.btn_share_score:
                View scoreFrame = findViewById(R.id.score_share_frame);
                scoreFrame.setBackgroundColor(Color.parseColor("#64b8ff"));
                final Bitmap bmp = Bitmap.createBitmap(scoreFrame.getWidth(), scoreFrame.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                scoreFrame.draw(canvas);
                FileUtility.shareWin(this, uow, null, bmp);
                bmp.recycle();
                scoreFrame.setBackgroundColor(Color.TRANSPARENT);
                break;
            case R.id.goToAchievements0:
            case R.id.goToAchievements:
                startActivity(new Intent(this, AchievementsActivity.class));
                break;
            case R.id.goToMyPuzzles1:
                startActivity(new Intent(this, MyPuzzlesActivity.class));
                break;
            case R.id.league_progress_bar:
            case R.id.leagues_desc:
                View desc = findViewById(R.id.leagues_desc);
                if (desc.getVisibility() == View.GONE) {
                    desc.setVisibility(View.VISIBLE);
                } else {
                    desc.setVisibility(View.GONE);
                }
                break;
            case R.id.edit_link:
                dialogManager.showInputDialog("تغییر لینک", "اینجا میتونی لینک گروه یا شبکه تلگرام خودت رو قرار بدی تا بقیه بتونن پازلهایی که ساختی را ببینن و حل کنن. ضمنا هر لینک سیاسی و غیراخلاقی باعث بسته شدن حساب میشه!", "لینک", GameUser.getCurrScore().getLink(), true, 250, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        final String link = dialog.getInputEditText().getText().toString();
                        if (Patterns.WEB_URL.matcher(link).matches()) {
                            gameUser.getScore().setLink(link);
                            updateProfile(ScoreBoardActivity.this, gameUser.getScore());
                        } else {
                            toastManager.show("لینک معتبر نیست :(");
                        }
                    }
                });
                break;
            case R.id.player_link:
                openLink(this, GameUser.getCurrScore().getLink());
                break;

            case R.id.edit_name:
                dialogManager.showInputDialog("تغییر نام نمایشی", "اینجا میتونی نام نمایشی خودت که بقیه بازیکنها میبینن تغییر بدی. حواست باشه که نام نمایشی را فقط سه بار میتونی عوض کنی!", "نام نمایشی", GameUser.getCurrScore().getDisplayName(), false, 15, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        final String newName = dialog.getInputEditText().getText().toString();
                        if (!TextUtils.isEmpty(newName) && !gameUser.getScore().getDisplayName().equals(newName)) {
                            gameUser.getScore().setDisplayName(newName);
                            gameUser.getScore().incDisplayNameChangeCount();
                            updateProfile(ScoreBoardActivity.this, gameUser.getScore());
                        }
                    }
                });
                break;
            case R.id.player_name:
                break;

            case R.id.edit_signature:
                dialogManager.showInputDialog("تغییر امضا", "اینجا میتونی متن دلخواه خودت رو به عنوان امضا یادداشت کنی. نوشتن متن های سیاسی و غیراخلاقی هم ممنوع هست، نگی نگفتیم!", "امضا", GameUser.getCurrScore().getSignature(), true, 40, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        gameUser.getScore().setSignature(dialog.getInputEditText().getText().toString());
                        updateProfile(ScoreBoardActivity.this, gameUser.getScore());
                    }
                });
                break;
            case R.id.player_signature:
                break;
        }
    }

    public static void openLink(BaseActivity activity, String link) {
        if (!TextUtils.isEmpty(link)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(link));
                activity.startActivity(intent);
            } catch (Exception ex) {
                activity.toastManager.show("لینک مشکل داره :(");
            }
        }
    }
}
