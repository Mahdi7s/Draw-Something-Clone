package com.chocolate.puzhle2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.Asyncer;
import com.chocolate.puzhle2.Utils.BitmapUtility;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class SolveType extends BaseActivity implements Target, View.OnClickListener {
    private static UserPuzzle loadedPuzzle = null; // these statics cause problem
    private static boolean isFriendsClicked = false;
    private final String PuzzleCID = "FetchPuzzle";
    private ImageView imgPuzzle = null;
    private File puzzleFile = null;
    private int randomCount = 0;
    private Intent solveIntent = null;

    public static void resetPuzzle() {
        loadedPuzzle = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_type);

        imgPuzzle = (ImageView) findViewById(R.id.puzzle_thumbnail);
        enableSolveButton(false);

        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra("state") != null && intent.getStringExtra("state").equals("solve_img")) {
            isFriendsClicked = true;
            final Uri bmpUri = intent.getData();
            puzzleFile = new File(bmpUri.getPath());
            BitmapUtility.decodeBitmapAsync(this, bmpUri, true, this);
        }

        if (uow.getLocalRepo().isActFirstSeen(this)) {
            dialogManager.showApril(R.string.solve_type_act_title, R.string.solve_type_act_msg, -1, null);
        }

        if (intent != null && intent.getBooleanExtra("channel", false)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/puzhle")).setPackage("org.telegram.messenger"));
        }

        solveIntent = new Intent(this, SolvePuzzleActivity.class);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().post(new CoinsIncrementEvent(GameUser.getGameUser().getScore().getCoinsCount()));

        randomCount = uow.getLocalRepo().getSolveRandomCount();
        updateRandomButton();
        if (loadedPuzzle == null) {
            imgPuzzle.setImageResource(R.drawable.desc_solve);
            enableSolveButton(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void updateRandomButton() {
        ((ImageView) findViewById(R.id.button_sRandom)).setImageResource(
                randomCount == 0 ? R.drawable.btn_random_puzzle_free : randomCount == 1 ? R.drawable.btn_random_puzzle1 :
                        randomCount == 2 ? R.drawable.btn_random_puzzle3 : randomCount == 3 ? R.drawable.btn_random_puzzle5 : R.drawable.btn_random_puzzle5/*R.drawable.btn_random_puzzle10*/);
    }

    private int getCoinsToRefresh() {
        int cnt = Math.max(0, randomCount - 1);
        return cnt == 0 ? 5 : cnt == 1 ? 10 : cnt == 2 ? 30 : cnt == 3 ? 50 : 50;//100;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.button_sFriends:
                if (uow.getLocalRepo().isFirstSeen("button_sFriends")) {
                    dialogManager.showApril(R.string.btn_friends_title, R.string.btn_friends_text, -1, () -> onClick(findViewById(R.id.button_sFriends)));
                    return;
                }

                AnalyzeUtil.track("button_sFriends");
                sfxPlayer.Play(SfxResource.Button);

                isFriendsClicked = true;

                Intent openImage = FileUtility.getSocialImageIntents(this, false);
                startActivityForResult(openImage, CreatePuzzleActivity.REQUEST_IMAGE_CAPTURE);
                break;
            case R.id.button_sRandom:
                if (uow.getLocalRepo().isFirstSeen("button_sRandom")) {
                    dialogManager.showApril(R.string.btn_random_title, R.string.btn_random_text, -1, () -> onClick(findViewById(R.id.button_sRandom)));
                    return;
                }

                AnalyzeUtil.track("button_sRandom");
                sfxPlayer.Play(SfxResource.Button);

                isFriendsClicked = false;

                final UserScore userStore = GameUser.getGameUser().getScore();
                BiFunction<Boolean, Object> refreshRandPuzzle = freeRefresh -> {
                    if (freeRefresh || userStore.decrementCoinsCount(getCoinsToRefresh())) {
                        errorableBlock(PuzzleCID, cid -> {
                            uow.getPuzzleRepo().getRandomPuzzle((randomPuzzle, e) -> {
                                Runnable onErr = () -> {
                                    if (!freeRefresh) {
                                        userStore.incrementCoinsCount(getCoinsToRefresh());
                                    }
                                    if (randomCount > 0)
                                        --randomCount;
                                    EventBus.getDefault().post(new CoinsIncrementEvent(userStore.getCoinsCount()));
                                    dialogManager.showServerReceiveError(R.string.server_receive_busy, new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                        }
                                    });
                                };

                                if (handleError(cid, e)) {
                                    if (randomPuzzle != null) {
                                        loadedPuzzle = randomPuzzle;

                                        randomPuzzle.getPuzzlePicture().getDataInBackground((bytes, e2) -> {
                                            if (handleError(cid, e2)) {
                                                puzzleFile = FileUtility.getTempFile(SolveType.this, bytes);
                                                if(puzzleFile != null) {
                                                    BitmapUtility.decodeBitmapAsync(SolveType.this, puzzleFile, true, SolveType.this);
                                                } else {
                                                    toastManager.show("حافظه گوشی کم است");
                                                    onBitmapFailed(null);
                                                }
                                            } else {
                                                onBitmapFailed(null);
                                            }
                                        });

                                        updateRandomButton();
                                        if (!freeRefresh) {
                                            sfxPlayer.Play(SfxResource.BuyItem);
                                            EventBus.getDefault().post(new CoinsIncrementEvent(userStore.getCoinsCount()));
                                        }
                                        uow.getLocalRepo().setSolveRandomCount(randomCount);
                                    } else {
                                        onErr.run();
                                    }
                                } else {
                                    onErr.run();
                                }
                            });
                            return null;
                        }, data -> {
                        });
                    } else {
                        if (!freeRefresh) {
                            dialogManager.showNotEnoughCoin();
                        }
                    }
                    return null;
                };

                ++randomCount;
                boolean isFree = false; //++randomCount <= 1;
                if (!isFree) {
                    dialogManager.showRefreshWords(false, getCoinsToRefresh(), new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            loadedPuzzle = null;
                            refreshRandPuzzle.run(isFree);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                        }
                    });
                } else {
                    refreshRandPuzzle.run(isFree);
                }
                break;
            case R.id.btn_solve:
                AnalyzeUtil.track("btn_solve");
                sfxPlayer.Play(SfxResource.Button);

                if (loadedPuzzle != null) {
                    startActivity(solveIntent);
                } else {
                    View friendsBtn = findViewById(R.id.button_sFriends);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            friendsBtn.startAnimation(AnimationUtils.loadAnimation(SolveType.this, R.anim.anim_create_type_refresh));
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    findViewById(R.id.button_sRandom).startAnimation(animation);
                }
                break;
            case R.id.btn_report:
                if (loadedPuzzle != null) {
                    dialogManager.showReport(() -> {
                        loadedPuzzle.addReport();
                        loadedPuzzle.getCreatorScore().incPuzzleReports();
                        loadedPuzzle.put("CumulativeReports", loadedPuzzle.getCreatorScore().getPuzzleReports());
                        ParseObject.saveAllInBackground(Arrays.asList(loadedPuzzle.getCreatorScore(), loadedPuzzle), (e) -> {
                            e.printStackTrace();
                        });
                        v.setVisibility(View.INVISIBLE);
                        toastManager.show("گزارشت ثبت شد.");
                    });
                }
                break;
            case R.id.btn_profile:
                ProfileActivity.userScore = loadedPuzzle.getCreatorScore();
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }
    }

    private void processPuzzleBitmap(final Bitmap wholeBitmap) {
        if (wholeBitmap != null) {
            Asyncer.runAsync(new Asyncer.ResultProvider<Map<String, Object>>() {
                @Override
                public Map<String, Object> runAsync(Object... params) {
                    try {
                        return FileUtility.getImageUserAttrs(SolveType.this, wholeBitmap);
                    } catch (IOException e) {
                        return null;
                    }
                }
            }, (map) -> {
                if (map == null) {// handle error
                    handleError(PuzzleCID, null, true);
                    toastManager.puzzleNotSupported();
                } else {
                    final String userId = (String) map.get("userId"),
                            puzzleId = (String) map.get("puzzleId");
                    final int newH = (int) map.get("newH"),
                            newW = wholeBitmap.getWidth();
                    final Bitmap solveBitmap = (Bitmap) map.get("solveBitmap");

                    if (loadedPuzzle == null || isFriendsClicked) {
                        isFriendsClicked = false;
                        errorableBlock(PuzzleCID, cid -> {
                            uow.getPuzzleRepo().getByIdForSolve(userId, puzzleId, (data, e) ->
                            {
                                if (handleError(cid, e, true)) {
                                    if (data != null) {
                                        loadedPuzzle = data;
                                        startSolving(solveBitmap, newW, newH);
                                    } else {
                                        // solved this before!
                                        dialogManager.showYouSolvedBefore();
                                    }
                                } else {
                                    if (e.getCode() == ParseException.OTHER_CAUSE) {
                                        // its yours
                                        dialogManager.showThisIsYourOwnPuzzle();
                                    }
                                }
                            });
                            return null;
                        }, data -> {
                        });
                    } else {
                        handleError(PuzzleCID, null, true);
                        startSolving(solveBitmap, newW, newH);
                    }
                }
            });
        } else {
            handleError(PuzzleCID, null, true);
            toastManager.errorWhileImageLoad();
        }
    }

    private void startSolving(Bitmap solveBmp, int newW, int newH) {
        solveIntent.putExtra("newW", newW);
        solveIntent.putExtra("newH", newH);
        solveIntent.putExtra("puzzleFile", puzzleFile);
        solveIntent.putExtra("puzzleId", loadedPuzzle.getObjectId());
        solveIntent.putExtra("scoreId", loadedPuzzle.getCreatorScore().getObjectId());

        ParseObject.unpinAllInBackground("loadedPuzzle", (e) -> {
            ArrayList<ParseObject> arrayList = new ArrayList<>(Arrays.asList(loadedPuzzle, loadedPuzzle.getCreatorScore()));
            if (loadedPuzzle.getPuzzleWord() != null) {
                solveIntent.putExtra("wordId", loadedPuzzle.getPuzzleWord().getObjectId());
                arrayList.add(loadedPuzzle.getPuzzleWord());
            }
            ParseObject.pinAllInBackground("loadedPuzzle", arrayList, e2 -> {
                enableSolveButton(true);
            });
        });
        // set coin gift, image

        imgPuzzle.setImageBitmap(solveBmp);
    }

//    private int secondCounter = 30;
//    private void startPuzzleTimer() {
//        secondCounter = 30;
//        final TextView txtTime = (TextView) findViewById(R.id.txtSolveTypeTimer);
//
//        Handler timeHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                --secondCounter;
//                if (secondCounter < 0) { // disable solve button
//                    enableSolveButton(false);
//                    onClick(findViewById(R.id.btn_solve));
//                } else {
//                    txtTime.setText(secondCounter + "s");
//                    sendMessageDelayed(obtainMessage(), 1 * 1000);
//                }
//            }
//        };
//        timeHandler.sendMessageDelayed(timeHandler.obtainMessage(), 1 * 1000);
//    }

    private void enableSolveButton(boolean enability) {
        int coinGift = loadedPuzzle == null ? 10 : loadedPuzzle.getPuzzleCoinGift();
        ImageView imgRew = (ImageView) findViewById(R.id.srew1);
        imgRew.setImageResource(enability ? R.drawable.btn_solve_puzzle : R.drawable.btn_solve_puzzle_disabled);

        ImageView imgReward = (ImageView) findViewById(R.id.reward);
        imgReward.setImageResource(coinGift == 10 ? (enability ? R.drawable.btn_solve_reward1 : R.drawable.btn_solve_reward1_disabled) :
                coinGift == 20 ? (enability ? R.drawable.btn_solve_reward2 : R.drawable.btn_solve_reward2_disabled) :
                        coinGift == 30 ? (enability ? R.drawable.btn_solve_reward3 : R.drawable.btn_solve_reward3_disabled) :
                                (enability ? R.drawable.btn_solve_reward4 : R.drawable.btn_solve_reward4_disabled));

        try {
            if (enability) {
                findViewById(R.id.btn_solve).startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh));
                findViewById(R.id.btn_report).setVisibility(loadedPuzzle.canAddReport() ? View.VISIBLE : View.INVISIBLE);
                findViewById(R.id.btn_profile).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.btn_report).setVisibility(View.INVISIBLE);
                findViewById(R.id.btn_profile).setVisibility(View.INVISIBLE);
            }
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!FileUtility.onImageResult(this, requestCode, resultCode, data, true, this)) {
            // show err to ...
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        processPuzzleBitmap(bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        final UserScore userStore = GameUser.getGameUser().getScore();
        if (randomCount > 0)
            --randomCount;
        userStore.incrementCoinsCount(getCoinsToRefresh());
        EventBus.getDefault().post(new CoinsIncrementEvent(userStore.getCoinsCount()));

        updateRandomButton();

        puzzleFile = null;
        processPuzzleBitmap(null);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
