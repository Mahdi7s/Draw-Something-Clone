package com.chocolate.puzhle2;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chocolate.puzhle2.CustomViews.SolvePanel;
import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.BitmapUtility;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.events.LampsIncrementEvent;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.GameUserRepo;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Ariana Gostar on 12/27/2014.
 */
public class SolvePuzzleActivity extends BaseActivity implements View.OnClickListener {
    private final int allLettersCount = 3 * 7;
    private LinearLayout allLettersPanel;
    private LinearLayout gamePanel;
    private SolvePanel solvePanel;
    private ImageView showImg;
    private Button lampsCount = null;
    private UserScore userStore = null;
    private int boxSize = 0;
    private List<TextView> letterButtons = new ArrayList<>();
    private File puzzleFile = null;

    private int lampsUsed = 0;

    private UserPuzzle userPuzzle = null;
    private boolean canShareToFriends = false;
    //    private static boolean initGame = true;
    private boolean isFirstActSeen = false;

    private DateTime startTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_puzzle);
        AdsUtils.setListenerForBanner(this, R.id.banner_ad_view);

        boxSize = getResources().getDimensionPixelSize(R.dimen.suggestion_box);

        gamePanel = (LinearLayout) findViewById(R.id.gamePanel);
        allLettersPanel = (LinearLayout) findViewById(R.id.allLettersPanel);
        solvePanel = (SolvePanel) findViewById(R.id.solveLettersPanel);
        showImg = (ImageView) findViewById(R.id.showImg);
        lampsCount = (Button) findViewById(R.id.btn_lamps_count2);

        showImg.setOnClickListener(this);
        findViewById(R.id.btn_lamp).setOnClickListener(this);
        findViewById(R.id.btn_clear_answer).setOnClickListener(this);

        userStore = GameUser.getGameUser().getScore();
        reduceLampsCount(0);

        ((Button) findViewById(R.id.btn_lamps_count2)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/BYekan.ttf"));
        //------------------------------------------------------------------------------------

        final Intent intent = getIntent();
        canShareToFriends = intent.getBooleanExtra("canShareToFriends", canShareToFriends);
        String puzzleId = intent.getStringExtra("puzzleId");
        userPuzzle = ParseObject.createWithoutData(UserPuzzle.class, puzzleId);

        new Handler().post(() -> {
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
                //------------------------------------------------------------------------------------
                puzzleFile = (File) intent.getSerializableExtra("puzzleFile");
                final int newW = intent.getIntExtra("newW", 0),
                        newH = intent.getIntExtra("newH", 0);
                initGame(newW, newH);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private void initGame(int newW, int newH) {
        fillAllLettersPanel(userPuzzle.getIndependentAnswer(), userPuzzle.getKeyboard());
        solvePanel.setupSolvePanel(userPuzzle.getIndependentAnswer(), letter -> {
            TextView letBtn = getLetterButton(letter);
            letBtn.setText(letter);
            letBtn.setBackgroundResource(R.drawable.letter_big);

            solvePanel.setBackgroundResource(R.drawable.letters_frame);
        });

        BitmapUtility.resizeInto(this, puzzleFile, newW, newH, showImg);

        startTime = DateTime.now();
    }

    private int maxShakesCount = 2;

    private void shakeLampButton() {
        if (--maxShakesCount < 0) return;

        runOnUiThread(() -> findViewById(R.id.btn_lamp).startAnimation(AnimationUtils.loadAnimation(SolvePuzzleActivity.this, R.anim.anim_create_type_refresh)));
    }

    private boolean reduceLampsCount(final int amount) {
        if (userStore.decrementLampsCount(amount)) {
            userStore.pinInBackground();
            lampsCount.setText(userStore.getLampsCount() + "");

            EventBus.getDefault().post(new LampsIncrementEvent(userStore.getLampsCount()));

            return true;
        }
        lampsCount.setText("0");
        if (amount > 0) {
            dialogManager.showNotEnoughLamp();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        dialogManager.showQuestion("جا زدی؟!", "مطمینی نمی تونی حلش کنی؟(لامپ هایی که اینجا استفاده کردی رو از دست میدیا)", "آره :(", "می تونم", () -> {
            if (lampsUsed > 0) {
                userPuzzle.incrementLamps(lampsUsed);
            }
            userPuzzle.increment("SolveMins", DateTime.now().minus(startTime.getMillis()).getSecondOfDay() / 60);
            userPuzzle.saveEventually();
            super.onBackPressed();
        }, () -> {
        });
    }

    private void fillAllLettersPanel(String word, String keyboard) {
        char[] letters = keyboard.toCharArray();
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/BTitrBd.ttf");
//        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/BYekan.ttf");
        final float density = getResources().getDisplayMetrics().density;
        final float sugTextSize = getResources().getDimension(R.dimen.suggestion_letter_size) / density;
        int rowsCount = allLettersCount / 7;
        int colsCount = allLettersCount / 3;
        for (int row = 0; row < rowsCount; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //params.weight = 1;
            rowLayout.setLayoutParams(params);
            rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int col = 0; col < colsCount; col++) {
                int idx = (row * colsCount) + col;

                TextView btn = new TextView(this);
                btn.setTypeface(tf);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, sugTextSize);
                btn.setGravity(Gravity.CENTER);
                btn.setBackgroundResource(R.drawable.letter_big);
                btn.setTextColor(Color.WHITE);
                params = new LinearLayout.LayoutParams(boxSize, boxSize);
                //params.weight = 1;
                btn.setLayoutParams(params);
                btn.setId(idx);
                btn.setText(letters[idx] + "");
                btn.setTag("letter-" + btn.getText());
                btn.setOnClickListener(this);
                rowLayout.addView(btn);
                letterButtons.add(btn);
            }
            allLettersPanel.addView(rowLayout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        reduceLampsCount(0);

        isFirstActSeen = uow.getLocalRepo().isActFirstSeen(this);
        if (isFirstActSeen) {
            dialogManager.showApril(R.string.solve_intro_title, R.string.solve_intro, -1, null);
        } else {
            new Handler().postDelayed(() -> {
                if (solvePanel.filledLetters == 0) {
                    shakeLampButton();
                }
            }, 1000 * 15);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_clear_answer) {
            solvePanel.clearSolvePanel();
        } else if (v.getId() == R.id.btn_lamp) {
            if (uow.getLocalRepo().isFirstSeen("btn_lamp")) {
                dialogManager.showApril(R.string.btn_lamp_title, R.string.btn_lamp_text, -1, () -> onClick(v));
                return;
            }

            if (lampsUsed < 3 && reduceLampsCount(1)) {
                solvePanel.clearSolvePanel();

                sfxPlayer.Play(SfxResource.Lamp);

                final String pLetters = userPuzzle.getIndependentAnswer().replace(" ", "");
                final int answerSize = pLetters.length(),
                        fakeLetters = allLettersCount - answerSize;

                if (lampsUsed < 2) {
                    removeFakeLetter(lampsUsed == 0 ? fakeLetters / 2 : fakeLetters - (fakeLetters / 2));
                    ++lampsUsed;
                } else if (lampsUsed == 2) { // last lamp and we must clear , fill but not last one!
                    solvePanel.clearSolvePanel();
                    for (int i = 0; i < answerSize - 1; i++) {
                        BiFunction<String, Object> function = (btnTag) -> {
                            ArrayList<TextView> btns = new ArrayList<>(letterButtons);
                            for (TextView sbtn : btns) {
                                if (!sbtn.getText().equals("") && ((String) sbtn.getTag()).equals(btnTag)) {
                                    onClick(sbtn);
                                    break;
                                }
                            }
                            return null;
                        };
                        final String btnTag = "letter-" + pLetters.charAt(i);
                        function.run(btnTag);
                    }
                    ++lampsUsed;
                } else {
                    // nothing ... :)
                }
            }
        } else if (v.getId() == R.id.btn_share_puzzle) {
            Runnable sharePuzzle = () -> {
                FileUtility.shareImage(this, uow, null, new File[]{puzzleFile}, true);
            };

            if (!canShareToFriends) {
                dialogManager.showQuestion("از دوستات کمک بگیر", "می خوای پازلو واسه دوستات بفرستی تا از اونا تو حلش کمک بگیری؟ (هزینه: ۲۰ سکه)", "هان", "نوچ", () -> {
                    final UserScore store = GameUser.getGameUser().getScore();
                    if (store.decrementCoinsCount(20)) {
                        canShareToFriends = true;
                        sharePuzzle.run();
                        store.pinInBackground();
                        EventBus.getDefault().post(new CoinsIncrementEvent(store.getCoinsCount()));
                    } else {
                        dialogManager.showNotEnoughCoin();
                    }
                }, () -> {
                });
            } else {
                sharePuzzle.run();
            }
        } else if (v.getId() == showImg.getId()) {
            if (gamePanel.getVisibility() == View.GONE) {
                gamePanel.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_clear_answer).setVisibility(View.VISIBLE);
            } else {
                gamePanel.setVisibility(View.GONE);
                findViewById(R.id.btn_clear_answer).setVisibility(View.GONE);
            }
        } else if (v instanceof TextView) {
            final TextView btn = (TextView) v;
            if (((String) btn.getTag()).startsWith("letter-")) {
                if (!btn.getText().equals("")) {
                    TextView lastBtn = solvePanel.getLastEmptyAnswerButton();
                    if (lastBtn != null) {
                        sfxPlayer.Play(SfxResource.Letter);

                        lastBtn.setText(btn.getText());
                        lastBtn.setBackgroundResource(R.drawable.letter_big);
                        btn.setText("");
                        btn.setBackgroundColor(Color.TRANSPARENT);
                        letterButtons.remove(getLetterBtnIndex(btn));
                        letterButtons.add(0, btn); // check if size is correct
                        if (solvePanel.isAnswerCorrect()) { // you win!
                            solvePanel.setBackgroundResource(R.drawable.letters_frame_win);
                            Toast.makeText(this, "یوهوو ;)", Toast.LENGTH_LONG).show();

                            final Period solvePeriod = new Period(startTime, DateTime.now());
                            final int solveSecs = solvePeriod.getHours() * 360 + solvePeriod.getMinutes() * 60 + solvePeriod.getSeconds();

                            sfxPlayer.Play(SfxResource.Win);
                            dialogManager.showYouWin(userPuzzle, () -> {
                                uow.getLocalRepo().setCanSolveCount(uow.getLocalRepo().getCanSolveCount() - 1);
                                uow.getLocalRepo().setCanCreateCount(3);
                                uow.getLocalRepo().setSolveRandomCount(0);

                                SolveType.resetPuzzle();
                                GameUserRepo.isDirty = true;
                                // ---------------------------------------------------------------------------
                                Intent winIntent = new Intent(SolvePuzzleActivity.this, WinActivity.class);
                                winIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                winIntent.putExtra("newW", getIntent().getIntExtra("newW", 0));
                                winIntent.putExtra("newH", getIntent().getIntExtra("newH", 0));
                                winIntent.putExtra("puzzleFile", puzzleFile);
                                winIntent.putExtra("puzzleId", userPuzzle.getObjectId());
                                winIntent.putExtra("scoreId", userPuzzle.getCreatorScore().getObjectId());
                                if (userPuzzle.getPuzzleWord() != null) {
                                    winIntent.putExtra("wordId", userPuzzle.getPuzzleWord().getObjectId());
                                }
                                winIntent.putExtra("solveSecs", solveSecs);
                                winIntent.putExtra("lampsUsed", lampsUsed);
                                startActivity(winIntent);
                            });
                        } else {
                            if (solvePanel.filledLetters == solvePanel.answerLetters) { // lose
                                solvePanel.setBackgroundResource(R.drawable.letters_frame_lose);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isFirstActSeen) {
                                            runOnUiThread(() -> {
                                                dialogManager.showApril(R.string.lamp_intro_title, R.string.lamp_intro, -1, () -> {
                                                    shakeLampButton();
                                                });
                                            });
                                        } else shakeLampButton();
                                    }
                                }, 1000 * 3);

                                sfxPlayer.Play(SfxResource.Lose);
                            } else {
                                solvePanel.setBackgroundResource(R.drawable.letters_frame);
                            }
                        }
                    }
                }
            }
        }
    }

//    private int secondCounter = 60 * 3;
//    private void startPuzzleTimer() {//// TODO: 9/23/15
//        secondCounter = 26;
//        final TextView txtTime = (TextView) findViewById(R.id.txtSolveTypeTimer);
//
//        Handler timeHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                --secondCounter;
//                if (secondCounter < 0) { // disable solve button
//                    onClick(findViewById(R.id.btn_solve));
//                } else {
//                    txtTime.setText(secondCounter + "s");
//                    sendMessageDelayed(obtainMessage(), 1 * 1000);
//                }
//            }
//        };
//        timeHandler.sendMessageDelayed(timeHandler.obtainMessage(), 1 * 1000);
//    }


    private ArrayList<TextView> fakeButtons = null;

    private void removeFakeLetter(int count) {
        if (count <= 0) return;

        if (fakeButtons == null) {
            fakeButtons = new ArrayList<>(letterButtons);
            for (char c : userPuzzle.getIndependentAnswer().toCharArray()) {
                for (int i = 0; i < fakeButtons.size(); i++) {
                    TextView btnFake = fakeButtons.get(i);
                    String letter = btnFake.getText().toString().replace("letter-", "");
                    if (letter.equals(c + "")) {
                        fakeButtons.remove(i);
                        break;
                    }
                }
            }
        } else if (fakeButtons.size() <= 0) return;

        int rndIdx = (int) (Math.random() * fakeButtons.size());
        TextView btnFake = fakeButtons.remove(rndIdx);
        btnFake.setVisibility(View.INVISIBLE);
        btnFake.setEnabled(false);
        btnFake.setText("");

        removeFakeLetter(count - 1);
    }

    private boolean areSolveAndAllPanelsEqual() {
        ArrayList<TextView> remainedButtons = getRemainedButtons();

        return remainedButtons.size() == userPuzzle.getIndependentAnswer().replaceAll(" ", "").length();
    }

    private ArrayList<TextView> getRemainedButtons() {
        ArrayList<TextView> remainedButtons = new ArrayList<>();
        for (TextView btn : letterButtons) {
            if (btn.getText().length() > 0) {
                remainedButtons.add(btn);
            }
        }
        return remainedButtons;
    }

    private int getLetterBtnIndex(TextView btn) {
        for (int i = 0; i < letterButtons.size(); i++)
            if (letterButtons.get(i).getId() == btn.getId())
                return i;
        return -1;
    }

    private TextView getLetterButton(String letter) {
        for (TextView btn : letterButtons) {
            if (btn.getText().equals("") && ((String) btn.getTag()).split("-")[1].equals(letter))
                return btn;
        }
        return null;
    }
}
