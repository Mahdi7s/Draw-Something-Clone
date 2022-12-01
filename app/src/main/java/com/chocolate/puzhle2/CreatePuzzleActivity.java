package com.chocolate.puzhle2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.CustomViews.BrushSizeSelector;
import com.chocolate.puzhle2.CustomViews.ColorPallets;
import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.Asyncer;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.events.AlarmReceiver;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.events.EventMsgType;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.repos.GameUserRepo;
import com.chocolate.puzhle2.repos.UserPuzzleRepo;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mahdi7s on 12/17/2014.
 */

public class CreatePuzzleActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener, Target {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static CreatePuzzleActivity instance = null;

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, saveBtn, textBtn, importBtn;

    private RelativeLayout directionOverlay = null;

    private BrushSizeSelector penSize = null;
    private BrushSizeSelector eraserSize = null;
    private ColorPallets colorPallets = null;

    private boolean isFreeWord = false;
    private DrawingWord drawingWord = null;
    private String freeWord = null;
    private boolean uploadPuzzle = false;
    private List<DrawingWord> wordsToUnpin = null;

    private static boolean puzzleShared = false;

    public boolean UsedColorPallet = false, UsedImageImport = false, UsedTextImport = false, UsedUndo = false;

    private DateTime startTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_puzzle);
        AdsUtils.prepareInterstitialAd();
        instance = this;

        directionOverlay = (RelativeLayout) findViewById(R.id.direction_overlay);

        drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        importBtn = (ImageButton) findViewById(R.id.import_btn);
        importBtn.setOnClickListener(this);

        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        textBtn = (ImageButton) findViewById(R.id.text_btn);
        textBtn.setOnClickListener(this);

        drawView = (DrawingView) findViewById(R.id.drawing);
        drawView.setOnTouchListener(this);
        drawView.setBrushSize(10);
        drawView.dialogManager = dialogManager;

        findViewById(R.id.color_pallete).setOnClickListener(this);

        findViewById(R.id.clear_canvas_btn).setOnClickListener(this);

        penSize = (BrushSizeSelector) findViewById(R.id.pen_size_panel);
        penSize.messageType = EventMsgType.BrushSizeChange;
        eraserSize = (BrushSizeSelector) findViewById(R.id.eraser_size_panel);
        eraserSize.messageType = EventMsgType.EraserSelection;

        colorPallets = (ColorPallets) findViewById(R.id.pallets);
        colorPallets.hide();

        // ---------------------------------------------------------------------------------
        Intent intent = getIntent();
        isFreeWord = intent.hasExtra("freeWord");
        if (isFreeWord) {
            freeWord = intent.getStringExtra("freeWord");
        } else {
            drawingWord = ParseObject.createWithoutData(DrawingWord.class, intent.getStringExtra("drawingWordId"));
            try {
                drawingWord.fetchFromLocalDatastore();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        uploadPuzzle = intent.getBooleanExtra("publicShare", false);
        wordsToUnpin = new ArrayList<>();
        wordsToUnpin.add(ParseObject.createWithoutData(DrawingWord.class, intent.getStringExtra("wi1")));
        wordsToUnpin.add(ParseObject.createWithoutData(DrawingWord.class, intent.getStringExtra("wi2")));
        wordsToUnpin.add(ParseObject.createWithoutData(DrawingWord.class, intent.getStringExtra("wi3")));
        // ---------------------------------------------------------------------------------

        final TextView txtPuzWord = (TextView) findViewById(R.id.puzzle_name_desc);

        String wordOfPuzzle = "";
        if (isFreeWord) {
            txtPuzWord.setText("الان در حال طراحی «پازل» هستی".replace("پازل", freeWord));
            wordOfPuzzle = freeWord;
        } else {
            txtPuzWord.setText("الان در حال طراحی «پازل» هستی".replace("پازل", drawingWord.getWord()));
            wordOfPuzzle = drawingWord.getWord();
        }

        if (uow.getLocalRepo().isActFirstSeen(this)) {
            dialogManager.showApril(getResources().getString(R.string.create_intro1_title), getResources().getString(R.string.create_intro1).replace("تندرستي", wordOfPuzzle), null, () -> {
                new Handler().postDelayed(() -> {
                    runOnUiThread(()->dialogManager.showApril(getResources().getString(R.string.create_intro2_title), getResources().getString(R.string.create_intro2), null, null));
                }, 5000);
            });
        }

        final int createCount = GameUser.getGameUser().getScore().getCreatedCount();
        if (createCount > 3 && uow.getLocalRepo().isFirstSeen("FeaturesExtraScore") || createCount > 9 && uow.getLocalRepo().isFirstSeen("FeaturesExtraScore2")) {
            dialogManager.showApril("پازل قشنگتر، امتیاز بیشتر!", "یادت باشه هرچی از ابزارهای بیشتری در طراحی پازل استفاده کنی، امتیازی که از این پازل به دست میاری بیشتر میشه.", "خب", null);
        }

        if ((!uow.getLocalRepo().isFirstSeen("GoodPuzzleDesc") && uow.getLocalRepo().isFirstSeen("GoodPuzzleDesc1")) || (createCount > 10 && uow.getLocalRepo().isFirstSeen("GoodPuzzleDesc2"))) {
            View img = findViewById(R.id.good_puzzle_desc);
            img.setOnClickListener((v) -> {
                v.setVisibility(View.GONE);
            });
            new Handler().postDelayed(() -> {
                runOnUiThread(()->img.setVisibility(View.VISIBLE));
            }, 1000);
        }

        startTime = DateTime.now();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.draw_btn:
                AnalyzeUtil.track("draw_btn");

                hideSizePanels(view);
                positionViewOnTopOf(penSize, view);
                penSize.show();
                break;
            case R.id.erase_btn:
                AnalyzeUtil.track("erase_btn");

                hideSizePanels(view);
                positionViewOnTopOf(eraserSize, view);
                eraserSize.show();
                break;
            case R.id.text_btn:
                AnalyzeUtil.track("text_btn");

                if (GameUser.getGameUser().getScore().hasTypeUnlocked() || canUseItem()) {
                    TextDialog txtDlg = new TextDialog(this, drawView, this);
                    txtDlg.show();
                } else {
                    dialogManager.showEnableText();
                }
                break;
            case R.id.save_btn:
                AnalyzeUtil.track("save_btn");

                if (drawView.isEmpty()) {
                    toastManager.drawViewIsEmpty();
                    break;
                }
                if (puzzleShared) break;

                dialogManager.showSetPuzzleMessage(uploadPuzzle, (coins, msg) -> {
                        errorableBlock("SharePuzzle", cid -> {
                            final UserPuzzle data = new UserPuzzle();
                            if (UsedColorPallet) data.setUsedColorPallet();
                            if (UsedImageImport) data.setUsedImageImport();
                            if (UsedTextImport) data.setUsedTextImport();
                            if (UsedUndo) data.setUsedUndo();
                            data.setMode(coins == 0 ? "one" : "two");

                            data.setSentToPublic(uploadPuzzle);
                            final Period createPeriod = new Period(startTime, DateTime.now());
                            data.put("CreateSeconds", createPeriod.getHours() * 360 + createPeriod.getMinutes() * 60 + createPeriod.getSeconds());

                            uow.getPuzzleRepo().createPuzzle(data, drawingWord, freeWord, msg, e -> {
                                if (handleError(cid, e, !uploadPuzzle)) {
                                    if (freeWord == null) {
                                        uow.getDrawingWordRepo().clearLastWords();
                                        ParseObject.unpinAllInBackground("DrawingWords", wordsToUnpin);
                                    }
                                    // ----------------------------------------------------
                                    final String puzzleId = data.getObjectId(),
                                            userId = data.getCreatorId();

                                    final MaterialDialog appProgress = dialogManager.showAppProgress();
                                    Asyncer.runAsync(new Asyncer.ResultProvider<File>() {
                                        @Override
                                        public File runAsync(Object... params) {
                                            return FileUtility.makeAndSavePuzzle(CreatePuzzleActivity.this, getBitmap(), userId, puzzleId);
                                        }
                                    }, new Asyncer.ResultConsumer<File>() {
                                        @Override
                                        public void postRun(File result) {
                                            drawView.destroyDrawingCache();
                                            appProgress.dismiss();

                                            uow.getLocalRepo().setCanCreateCount(uow.getLocalRepo().getCanCreateCount() - 1);
                                            uow.getLocalRepo().setCanSolveCount(3);
                                            UserPuzzleRepo.isDirty = true;
                                            GameUserRepo.isDirty = true;
                                            AlarmReceiver.scheduleLikeNotification(CreatePuzzleActivity.this);
                                            if (canUseItem()) {
                                                uow.getLocalRepo().incFreeSolveCount();
                                            }

                                            BiFunction<Object, Object> sharePuzzleFunc = p -> {
//                                                dialog.dismiss();

                                                FileUtility.shareImage(CreatePuzzleActivity.this, uow, data, new File[]{result}, false);
                                                puzzleShared = true;

                                                return null;
                                            };
                                            if (uploadPuzzle) {
                                                uow.getPuzzleRepo().UploadRandomPuzzle(data, e2 -> {
                                                    if (handleError(cid, e2, true)) {
                                                        dialogManager.showQuestion("ارسال عمومی موفق", "ارسال عمومی پازل با موفقیت انجام شد، دوست داری پازلو واسه دوستای تلگرامو واتس اپت هم ارسال کنی؟", "آری", "نوچ", () -> {
                                                            sharePuzzleFunc.run(null);
                                                        }, () -> {
                                                            startActivity(new Intent(CreatePuzzleActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                                                        });
                                                    }
                                                });
                                            } else {
                                                sharePuzzleFunc.run(null);
                                            }
                                        }
                                    });

                                    if (data.isFreePuzzle()) {
                                        CreateTypeActivity.refreshClickCount = 0;
                                    }
                                }
                            });
                            return null;
                        }, dlg -> {
//                            dialog.dismiss();
                        });
                    return null;
                });
                break;
            case R.id.import_btn:
                AnalyzeUtil.track("import_btn");

                if (GameUser.getGameUser().getScore().hasImageImportUnlocked() || canUseItem()) {
                    startActivityForResult(FileUtility.getSocialImageIntents(this, true), REQUEST_IMAGE_CAPTURE);
                } else {
                    dialogManager.showEnablePicture();
                }
                break;
            case R.id.color_pallete:
                AnalyzeUtil.track("color_pallete");

                positionViewOnTopOf(colorPallets, findViewById(R.id.buttons_layout));
                colorPallets.show();

                UsedColorPallet = (GameUser.getGameUser().getScore().hasColorsUnlocked(1)
                        || GameUser.getGameUser().getScore().hasColorsUnlocked(2) ||
                        GameUser.getGameUser().getScore().hasColorsUnlocked(3) ||
                        GameUser.getGameUser().getScore().hasColorsUnlocked(4)) || canUseItem();
                break;
            case R.id.clear_canvas_btn:
                AnalyzeUtil.track("clear_canvas_btn");

                dialogManager.showClearCanvasReally(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        drawView.startNew();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                });
                break;
        }

        hideSizePanels(view);
    }

    public static boolean canUseItem() {
        final int solveCount = GameUser.getCurrScore().getSolvedCount(),
                createCount = GameUser.getCurrScore().getCreatedCount();
        return /*solveCount <= 6 && */ createCount <= 3;
    }

    private void hideSizePanels(View view) {
        // hide size panels
        if (view.getId() != R.id.erase_btn) {
            eraserSize.hide();
        }
        if (view.getId() != R.id.draw_btn) {
            penSize.hide();
        }
        if (view.getId() != R.id.color_pallete) {
            colorPallets.hide();
        }
    }

    private Bitmap getBitmap() {
        drawView.setDrawingCacheEnabled(true);
        Bitmap drawBitmap = drawView.getDrawingCache();
        return drawBitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!FileUtility.onImageResult(this, requestCode, resultCode, data, false, this)) {
            // show err loading bitmap
        }
    }

    public void onInsertDirectionClick(View view) {
        String[] dirStr = view.getTag().toString().split(",");
        float xRatio = Float.parseFloat(dirStr[0]);
        float yRatio = Float.parseFloat(dirStr[1]);

        drawView.setInsertionAnchors(xRatio, yRatio);
        drawView.onTouchEvent(MotionEvent.obtain(new Date().getTime(), new Date().getTime(), MotionEvent.ACTION_UP, 0, 0, 0));
    }

    public void goToInsertionMode(boolean isPicture) {
        directionOverlay.setVisibility(View.VISIBLE);

        if (uow.getLocalRepo().isFirstSeen("CreateInsertion" + (isPicture ? "Picture" : "Text"))) {
            dialogManager.showApril(isPicture ? R.string.picture_help_intro_title : R.string.text_help_intro_title,
                    isPicture ? R.string.picture_help_intro : R.string.text_help_intro, -1, null);
        }
    }

    public void goToNormalMode() {
        directionOverlay.setVisibility(View.INVISIBLE);
    }

    public void onRedoClick(View view) {
        drawView.redoDraw(canUseItem());
        UsedUndo = GameUser.getGameUser().getScore().hasUndoUnlocked();
    }

    public void onUndoClick(View view) {
        drawView.undoDraw(canUseItem());
        UsedUndo = GameUser.getGameUser().getScore().hasUndoUnlocked();
    }

    private void positionViewOnTopOf(View view, View viewToRel) {
        Point relPos = getViewPosition(viewToRel);
        setPosition(view, relPos.x, relPos.y);
    }

    private void setPosition(View view, int left, int top) {
        top -= view.getHeight();
        //left -= view.getWidth();

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        lp.setMargins(left, top, 0, 0);
        view.setLayoutParams(lp);
    }

    private Point getViewPosition(View view) {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int topOffset = dm.heightPixels - findViewById(R.id.create_root_layout).getMeasuredHeight();
        int pos[] = new int[2];
        view.getLocationOnScreen(pos);
        return new Point(pos[0], pos[1] + 24 + topOffset - view.getHeight() / 2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (puzzleShared) {
            puzzleShared = false;
            dialogManager.showSharePuzzleSuccess();
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (bitmap != null) {
            final CropDialog cropDlg = new CropDialog(CreatePuzzleActivity.this, bitmap, drawView, this);
            cropDlg.show();
        }
    }

    @Override
    public void onBackPressed() {
        dialogManager.showCancelPuzzleDrawing(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                finish();
            }
        });
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideSizePanels(view);
        return false;
    }
}
