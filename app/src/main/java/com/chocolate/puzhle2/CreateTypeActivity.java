package com.chocolate.puzhle2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.ParseException;
import com.parse.SaveCallback;

import de.greenrobot.event.EventBus;

public class CreateTypeActivity extends BaseActivity implements View.OnClickListener {
    private TextView txtEasy, txtNormal, txtHard, txtFreeWord;
    private ImageView imgClickRefreshDesc;
    public static int refreshClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_type);

        txtEasy = (TextView) findViewById(R.id.text_easy);
        txtNormal = (TextView) findViewById(R.id.text_normal);
        txtHard = (TextView) findViewById(R.id.text_hard);

        txtFreeWord = (TextView) findViewById(R.id.free_word);

        imgClickRefreshDesc = (ImageView) findViewById(R.id.imgClickRefreshDesc);

        txtFreeWord.clearFocus();
        findViewById(R.id.btn_refresh_words).requestFocus();

        publicCheck();
        refreshWords(true);
        incrementRefreshCount(0);
    }

    private void refreshWords(final boolean isFree) {
        errorableBlock("refreshWords", cid -> {
            final UserScore score = GameUser.getGameUser().getScore();

            uow.getDrawingWordRepo().get3Words((data, e) -> {
                if (handleError(cid, e)) {

                    SaveCallback callback = e2 -> {
                        if (handleError(cid, e2)) {
                            for (DrawingWord dw : data) {
                                if (dw.getMode() == DrawingWord.EASY_WORD) {
                                    txtEasy.setTag(dw);
                                    txtEasy.setText(dw.getWord());
                                } else if (dw.getMode() == DrawingWord.NORMAL_WORD) {
                                    txtNormal.setTag(dw);
                                    txtNormal.setText(dw.getWord());
                                } else {
                                    txtHard.setTag(dw);
                                    txtHard.setText(dw.getWord());
                                }
                            }
                            if (!isFree) {
                                incrementRefreshCount(1);
                            }
                            handleError(cid, e2, true);

                            if (uow.getLocalRepo().isActFirstSeen(this)) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(() -> dialogManager.showCreateTypeActStart(() -> {}));
                                    }
                                }, 3000);
                            }
                        }
                        EventBus.getDefault().post(new CoinsIncrementEvent(score.getCoinsCount()));
                    };

                    if (!isFree) {
                        if (score.decrementCoinsCount(50)) {
                            sfxPlayer.Play(SfxResource.BuyItem);

                            score.pinInBackground(callback);
                            EventBus.getDefault().post(new CoinsIncrementEvent(score.getCoinsCount()));
                        } else {
                            dialogManager.showNotEnoughCoin();
                            callback.done(new ParseException(ParseException.OTHER_CAUSE, null));
                        }
                    } else {
                        callback.done(null);
                    }
                }
            });
            return null;
        }, ud -> {

        });
    }

    private void incrementRefreshCount(int amount) {
        refreshClickCount += amount;
        if (refreshClickCount == 0) {
            imgClickRefreshDesc.setImageResource(R.drawable.text_refresh_twice);

            setFreeButtonEnablity(false);
        } else if (refreshClickCount == 1) {
            imgClickRefreshDesc.setImageResource(R.drawable.text_refresh_once);
        } else if (refreshClickCount >= 2) {
            imgClickRefreshDesc.setImageResource(R.drawable.text_refresh_gone);

            setFreeButtonEnablity(true);
        }
    }

    private void setFreeButtonEnablity(boolean enablity) {
        final ImageView freeBtnCoin = (ImageView) findViewById(R.id.btn_free_coin);
        final ImageView freeBtnText = (ImageView) findViewById(R.id.btn_free_text);

        freeBtnCoin.setImageResource(isPublicShare() ? (enablity ? R.drawable.btn_create_free_coin3 : R.drawable.btn_create_free_coin3_disabled)
                : (enablity ? R.drawable.btn_create_free_coin2 : R.drawable.btn_create_free_coin2_disabled));
        freeBtnText.setImageResource(enablity ? R.drawable.btn_create_free : R.drawable.btn_create_free_disabled);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create_easy:
                AnalyzeUtil.track("btn_create_easy");
                GoToCreate((DrawingWord) txtEasy.getTag(), null);
                break;
            case R.id.btn_create_normal:
                AnalyzeUtil.track("btn_create_normal");
                GoToCreate((DrawingWord) txtNormal.getTag(), null);
                break;
            case R.id.btn_create_hard:
                AnalyzeUtil.track("btn_create_hard");
                GoToCreate((DrawingWord) txtHard.getTag(), null);
                break;
            case R.id.btn_create_free:
                if (refreshClickCount >= 2) {
                    if (!TextUtils.isEmpty(txtFreeWord.getText())) {
                        AnalyzeUtil.track("btn_create_free");
                        GoToCreate(null, txtFreeWord.getText().toString());
                    } else {
                        findViewById(R.id.btn_create_free).startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh));
                    }
                } else {
                    findViewById(R.id.btn_refresh_words).startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh));
                }
                break;
            case R.id.imgClickRefreshDesc:
                findViewById(R.id.btn_refresh_words).startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh));
                break;
            case R.id.btn_refresh_words:
                AnalyzeUtil.track("btn_refresh_words");
                dialogManager.showRefreshWords(true, 50, new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (GameUser.getGameUser().getScore().getCoinsCount() >= 50) {
                            uow.getDrawingWordRepo().clearLastWords();
                        }
                        refreshWords(false);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                });
                break;
            case R.id.btn_check_public:
                if (uow.getLocalRepo().isFirstSeen("public_share")) {
                    dialogManager.showPublicShareStart(() -> {
                    });
                }

                publicCheck();
                break;
        }

        sfxPlayer.Play(SfxResource.Button);
    }

    private void publicCheck() {
        final ImageView btnCheck = (ImageView) findViewById(R.id.btn_check_public);
        boolean isChecked = btnCheck.getTag().equals("checked");

        final ImageView easyC = (ImageView) findViewById(R.id.btn_easy_coin),
                normalC = (ImageView) findViewById(R.id.btn_normal_coin),
                hardC = (ImageView) findViewById(R.id.btn_hard_coin),
                freeC = (ImageView) findViewById(R.id.btn_free_coin);

        if (isChecked) { // uncheck
            easyC.setImageResource(R.drawable.btn_create_easy_private);
            normalC.setImageResource(R.drawable.btn_create_normal_private);
            hardC.setImageResource(R.drawable.btn_create_hard_private);
            freeC.setImageResource(R.drawable.btn_create_free_coin2);

            btnCheck.setTag("unchecked");
            btnCheck.setImageResource(R.drawable.btn_uncheck_public);
        } else { // check
            easyC.setImageResource(R.drawable.btn_create_easy_public);
            normalC.setImageResource(R.drawable.btn_create_normal_public);
            hardC.setImageResource(R.drawable.btn_create_hard_public);
            freeC.setImageResource(R.drawable.btn_create_free_coin3);

            btnCheck.setTag("checked");
            btnCheck.setImageResource(R.drawable.btn_check_public);
        }

        setFreeButtonEnablity(refreshClickCount >= 2);
    }

    private void GoToCreate(DrawingWord drawingWord, String freeWord) {
        Intent intent = new Intent(this, CreatePuzzleActivity.class);
        if (drawingWord != null) {
            intent.putExtra("drawingWordId", drawingWord.getObjectId());
        } else {
            intent.putExtra("freeWord", freeWord);
        }
        intent.putExtra("publicShare", isPublicShare());
        intent.putExtra("wi1", ((DrawingWord) txtEasy.getTag()).getObjectId());
        intent.putExtra("wi2", ((DrawingWord) txtNormal.getTag()).getObjectId());
        intent.putExtra("wi3", ((DrawingWord) txtHard.getTag()).getObjectId());
        startActivity(intent);
    }

    private boolean isPublicShare() {
        return findViewById(R.id.btn_check_public).getTag().equals("checked");
    }
}
