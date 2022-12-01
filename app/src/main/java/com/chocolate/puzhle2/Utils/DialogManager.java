package com.chocolate.puzhle2.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.chocolate.puzhle2.CoinStoreActivity;
import com.chocolate.puzhle2.CreateTypeActivity;
import com.chocolate.puzhle2.ItemsStoreActivity;
import com.chocolate.puzhle2.MainActivity;
import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.ShareDialogue;
import com.chocolate.puzhle2.SolveType;
import com.chocolate.puzhle2.WinDialogue;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.events.BiFunction2;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.LocalRepo;
import com.parse.ParseObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by choc01ate on 5/12/2015.
 */
public class DialogManager {
    private Context context = null;
    private ToastManager toastManager = null;
    private Typeface defaultTypeface = null;

    private Map<String, MaterialDialog> dialogsCache;
    private boolean isSettedFilter = false;

    public SfxPlayer sfxPlayer = null;

    public DialogManager(Context context, ToastManager toastManager) {
        this.context = context;
        this.toastManager = toastManager;
        defaultTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/BMorvarid.ttf");
        dialogsCache = new HashMap<>();
    }

    public MaterialDialog showServerProgress() {
        return showDialog("showServerProgress",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(false)
                        .content(R.string.server_connecting_wait)
                        .progress(true, 0));
    }

    public MaterialDialog showRefreshWords(boolean isWord, int coins, MaterialDialog.ButtonCallback callback) {
        callback.onPositive(null);
//        return showDialog("RefreshWords", getNewDialog()
//                .autoDismiss(true)
//                .title(isWord ? "کلمات جدید" : "پازل جدید")
//                .content(isWord ? R.string.refresh_words_text : R.string.refresh_puzzle, coins)
//                .positiveText(R.string.btn_ok)
//                .iconRes(R.drawable.icon_coin)
//                .negativeText(R.string.btn_no)
//                .callback(callback));
        return null;
    }

    public MaterialDialog showUpdatingProgress() {
        return showDialog("showUpdatingProgress",
                getNewDialog()
                        .autoDismiss(false)
                        .content(R.string.server_updating_wait)
                        .progress(true, 0));
    }

    public MaterialDialog showDirectoryOrFileCreationFailed() {
        return showDialog("showUpdatingProgress",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .content(R.string.directory_Or_File_Creation_Failed)
                        .progress(false, 0));
    }

    public MaterialDialog showAppProgress() {
        return showDialog("showAppProgress",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .content(R.string.app_progress_wait)
                        .progress(true, 0));
    }

    public MaterialDialog showBazaarProgress() {
        return showDialog("showBazaarProgress",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .content(AppStatistics.getStoreConnectionMsgId())
                        .progress(true, 0));
    }

    public MaterialDialog showLoginToBazaar() {
        return showDialog("showLoginToBazaar",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .content(AppStatistics.getStoreLoginMsgId())
                        .positiveText(R.string.btn_ok));
    }

    public MaterialDialog showServerReceiveError(MaterialDialog.ButtonCallback callback) {
        return showServerReceiveError(R.string.server_receive_err_text, R.drawable.icon_wifi, callback);
    }

    public MaterialDialog showServerReceiveError(int msgResId, MaterialDialog.ButtonCallback tryCallback) {
        return showServerReceiveError(msgResId, R.drawable.icon_error, tryCallback);
    }

    public MaterialDialog showServerReceiveError(int msgResId, int iconResId, MaterialDialog.ButtonCallback tryCallback) {
        return showDialog("showServerReceiveError",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .content(msgResId)
                        .title(R.string.error_title)
                        .iconRes(iconResId)
                        .positiveText(R.string.btn_try_again)
                        .callback(tryCallback));
    }

    public MaterialDialog showClearCanvasReally(MaterialDialog.ButtonCallback clearCallback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showClearCanvasReally",
                getNewDialog()
                        .autoDismiss(true)
                        .content(R.string.clear_canvas_really)
                        .iconRes(R.drawable.april)
                        .positiveText(R.string.btn_ok)
                        .negativeText(R.string.btn_no)
                        .callback(clearCallback));
    }

    public MaterialDialog showGalleryAlreadyUpdated() {
        return showDialog("showServerReceiveError",
                getNewDialog()
                        .autoDismiss(false)
                        .content(R.string.gallery_already_updated)
                        .positiveText(R.string.btn_ok)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        }));
    }

    public MaterialDialog bitmapFailed() {
        return showDialog("showServerReceiveError",
                getNewDialog()
                        .autoDismiss(false)
                        .content(R.string.bitmap_failed)
                        .positiveText(R.string.btn_ok)
                        .iconRes(R.drawable.icon_error)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        }));
    }

    public void doYouWantToUpdate(MaterialDialog.ButtonCallback callback) {
        showDialog("doYouWantToUpdate",
                getNewDialog()
                        .autoDismiss(false)
                        .content(R.string.Do_You_Want_To_Update)
                        .positiveText(R.string.btn_ok)
                        .negativeText(R.string.not_enough_coin_btncancel)
                        .callback(callback));
    }

    public MaterialDialog showNotEnoughCoin() {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showNotEnoughCoin",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.not_enough_coin_title)
                        .content(R.string.not_enough_coin_msg)
                        .positiveText(R.string.not_enough_coin_btnget)
                        .negativeText(R.string.not_enough_coin_btncancel)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent intent = new Intent(context, CoinStoreActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        }));
    }

    public MaterialDialog showNotEnoughLamp() {
        sfxPlayer.Play(SfxResource.Tip);
        final boolean introduced = GameUser.getGameUser().getScore().getBoolean("introduce");

        return showDialog("showNotEnoughLamp",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.not_enough_lamp_title)
                        .content(introduced ? R.string.not_enough_lamp_msg : R.string.not_enough_lamp_msg_rate)
                        .positiveText(R.string.not_enough_lamp_btnget)
                        .negativeText(R.string.not_enough_lamp_btncancel)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent intent = new Intent(context, introduced ? ItemsStoreActivity.class : CoinStoreActivity.class);
                                intent.putExtra("lamps", true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                context.startActivity(intent);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        }));
    }

    public MaterialDialog showLoginDialog(MaterialDialog.ButtonCallback callback) {
        return showDialog("showLoginDialog",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .title(R.string.connect_gplus_title)
                        .content(R.string.connect_gplus_msg)
                        .iconRes(R.drawable.icon_warning)
                        .negativeText(R.string.connect_gplus_no)
                        .positiveText(R.string.connect_gplus_yes)
                        .callback(callback));
    }

    public MaterialDialog showSignupDialog(MaterialDialog.ButtonCallback callback) {
        return showDialog("showSignupDialog",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .title(R.string.connect_gplus_title)
                        .content(R.string.signup_gplus_msg)
                        .iconRes(R.drawable.icon_warning)
                        .negativeText(R.string.connect_gplus_no)
                        .positiveText(R.string.connect_gplus_yes)
                        .callback(callback));
    }

    public MaterialDialog showInputDialog(String title, String message, String hint, String prefill, boolean allowEmpty, int maxLength, MaterialDialog.ButtonCallback callback) {
        return showDialog("showLoginDialog",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .input(hint, prefill, allowEmpty, (materialDialog, charSequence) -> {

                        })
                        .inputMaxLength(maxLength)
                        .title(title)
                        .content(message)
                        .iconRes(R.drawable.april)
                        .negativeText("لغو")
                        .positiveText("ثبت")
                        .callback(callback));
    }

    public MaterialDialog showReturnFromWin(MaterialDialog.ButtonCallback callback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showReturnFromWin",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .iconRes(R.drawable.april)
                        .title(R.string.win_return_title)
                        .content(R.string.win_return_msg)
                        .positiveText(R.string.win_return_yes)
                        .callback(callback));
    }

    public MaterialDialog showSetDisplayName(final MaterialDialog.ButtonCallback callback) {
        return showApril(R.string.april_intro_title, R.string.april_intro, -1, () -> {
            showDialog("showSetDisplayName", getNewDialog()
                    .autoDismiss(false)
                    .cancelable(false)
                    .inputMaxLength(15)
                    .title(R.string.display_name_title)
                    .content(R.string.display_name_msg)
                    .inputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                    .iconRes(R.drawable.april)
                    .input(R.string.display_name_hint, R.string.display_name_prefill, false, (materialDialog, charSequence) -> {
                    })
                    .positiveText(R.string.display_name_enter)
                    .callback(callback));
        });
    }

    public MaterialDialog showApril(int title, int msg, int ok, Runnable onOkCallback) {
        final Resources resources = context.getResources();
        return showApril(title == -1 ? null : resources.getString(title), resources.getString(msg), ok == -1 ? null : resources.getString(ok), onOkCallback);
    }

    public MaterialDialog showApril(String title, String msg, String ok, Runnable onOkCallback){
        sfxPlayer.Play(SfxResource.Tip);

        final Resources resources = context.getResources();

        MaterialDialog.Builder builder = getNewDialog()
                .autoDismiss(true)
                .cancelable(false)
                .iconRes(R.drawable.april)
                .positiveText(!TextUtils.isEmpty(ok) ? ok : resources.getString(R.string.btn_ok))
                .content(msg)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (onOkCallback != null) onOkCallback.run();
                    }
                });
        if (!TextUtils.isEmpty(title)) {
            builder = builder.title(title);
        }

        return showDialog("showApril", builder);
    }

    public MaterialDialog showEnableUndoRedo() {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showEnableUndoRedo",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.enable_undo_title)
                        .content(R.string.enable_undo_msg)
                        .positiveText(R.string.enable_undo_yes)
                        .negativeText(R.string.enable_undo_no)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                context.startActivity(new Intent(context,ItemsStoreActivity.class).putExtra("others", true).putExtra("val", "undo"));
                            }
                        }));
    }

    public MaterialDialog showPurchaseItem(String itemName, int coinsNeeded, MaterialDialog.ButtonCallback callback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showPurchaseItem",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .iconRes(R.drawable.april)
                        .title(R.string.enable_item_title)
                        .content(context.getResources().getString(R.string.enable_item_msg).replace("اسم آیتم", "«" + itemName + "»").replace("مقدار", coinsNeeded + ""))
                        .positiveText(R.string.enable_item_yes)
                        .negativeText(R.string.enable_item_no)
                        .callback(callback));
    }

    public MaterialDialog showEnablePicture() {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showEnablePicture",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.enable_picture_title)
                        .content(R.string.enable_picture_msg)
                        .positiveText(R.string.enable_picture_yes)
                        .negativeText(R.string.enable_picture_no)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                context.startActivity(new Intent(context,ItemsStoreActivity.class).putExtra("others", true).putExtra("val", "picture"));
                            }
                        }));
    }

    public MaterialDialog showQuestion(String title, String msg, String ok, String cancel, Runnable okCallback, Runnable cancelCallback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showEnablePicture",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .title(title)
                        .content(msg)
                        .positiveText(ok)
                        .negativeText(cancel)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                okCallback.run();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                cancelCallback.run();
                            }
                        }));
    }

    public MaterialDialog showReport(Runnable reportCallback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showReport",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.report_title)
                        .content(R.string.report_text)
                        .positiveText(R.string.report_yes)
                        .negativeText(R.string.report_no)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                reportCallback.run();
                            }
                        }));
    }

    public MaterialDialog showShareCreation(BiFunction<Integer, String> callback‌) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showShareCreation",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title("پازلت چقدر می ارزه؟")
                        .content("پازلی که ساختی در چه حده؟! (پازل های خفن به افراد بیشتری نمایش داده میشن...)")
                        .positiveText("خفن(۲۰۰ سکه)")
                        .negativeText("معمولی(رایگان)")
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                callback‌.run(200);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                callback‌.run(0);
                            }
                        }));
    }

    public MaterialDialog showProfileReport(Runnable reportCallback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showProfileReport",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.report_title)
                        .content(R.string.report_profile_text)
                        .positiveText(R.string.report_yes)
                        .negativeText(R.string.report_no)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                reportCallback.run();
                            }
                        }));
    }

    public MaterialDialog showComplaint(Runnable complaintCallback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showComplaint",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.complaint_title)
                        .content(R.string.complaint_text)
                        .positiveText(R.string.complaint_yes)
                        .negativeText(R.string.complaint_no)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                complaintCallback.run();
                            }
                        }));
    }

    public MaterialDialog showEnableText() {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showEnableText",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.enable_text_title)
                        .content(R.string.enable_text_msg)
                        .positiveText(R.string.enable_text_yes)
                        .negativeText(R.string.enable_text_no)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                context.startActivity(new Intent(context,ItemsStoreActivity.class).putExtra("others", true).putExtra("val", "text"));
                            }
                        }));
    }

    public MaterialDialog showSolveOneOrPay(final LocalRepo localRepo, Runnable onCharged) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showSolveOneOrPay",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.solve_pay_title)
                        .content(R.string.solve_pay_msg)
                        .positiveText(R.string.solve_pay_solve)
                        .negativeText(R.string.solve_pay_pay)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                context.startActivity(new Intent(context, SolveType.class));
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                GameUser gameUser = GameUser.getGameUser();
                                if (gameUser.getScore().decrementCoinsCount(30)) {
                                    localRepo.setCanCreateCount(3);
                                    onCharged.run();
                                } else {
                                    showNotEnoughCoin();
                                }
                            }
                        }));
    }

    public MaterialDialog showCreateOneOrPay(final LocalRepo localRepo, Runnable onCharged) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showSolveOneOrPay",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .title(R.string.create_pay_title)
                        .content(R.string.create_pay_msg)
                        .positiveText(R.string.create_pay_create)
                        .negativeText(R.string.create_pay_pay)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                context.startActivity(new Intent(context, CreateTypeActivity.class));
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                GameUser gameUser = GameUser.getGameUser();
                                if (gameUser.getScore().decrementCoinsCount(30)) {
                                    localRepo.setCanSolveCount(3);
                                    onCharged.run();
                                } else {
                                    showNotEnoughCoin();
                                }
                            }
                        }));
    }

    public MaterialDialog showExitDialog() {
        sfxPlayer.Play(SfxResource.Tip);

        final UserScore userStore = GameUser.getGameUser().getScore();

        return showDialog("showExitDialog",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .iconRes(R.drawable.april)
                        .title(R.string.love_game_title)
                        .content(R.string.love_game_msg)
                        .positiveText(R.string.love_game_yes)
                        .negativeText(R.string.love_game_no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                showDialog("showRate",
                                        getNewDialog()
                                                .cancelable(false)
                                                .autoDismiss(true)
                                                .title(R.string.rate_game_title)
                                                .iconRes(R.drawable.april)
                                                .content(R.string.rate_game_msg)
                                                .positiveText(R.string.rate_game_yes)
                                                .callback(new MaterialDialog.ButtonCallback() {
                                                    @Override
                                                    public void onPositive(MaterialDialog dialog) {
                                                        if (Utility.rate(MainActivity.instance)) {
                                                            MainActivity.isRating = true;
                                                        }
                                                    }
                                                }));
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                showDialog("showFeedback",
                                        getNewDialog()
                                                .cancelable(true)
                                                .autoDismiss(true)
                                                .title(R.string.feedback_game_title)
                                                .iconRes(R.drawable.april)
                                                .content(R.string.feedback_game_msg)
                                                .inputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                                                .input("متن نقد", "", true, (materialDialog, charSequence) -> {
                                                })
                                                .positiveText(R.string.feedback_game_yes)
                                                .callback(new MaterialDialog.ButtonCallback() {
                                                    @Override
                                                    public void onPositive(MaterialDialog dialog) {
                                                        String feedBack = dialog.getInputEditText().getText().toString();
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

                                                        MainActivity.instance.moveTaskToBack(true);
                                                    }
                                                }));
                            }
                        }));
    }

    public ShareDialogue showSetPuzzleMessage(boolean isPublic, final BiFunction2<Integer, String, String> callback) {
        sfxPlayer.Play(SfxResource.Tip);

        ShareDialogue dlg = new ShareDialogue(context, this, isPublic, callback);
        dlg.show();
        return dlg;

//        MaterialDialog dlg = showDialog("showSetPuzzleAnswer",
//                getNewDialog()
//                        .autoDismiss(false)
//                        .cancelable(true)
//                        .inputMaxLength(40)
//                        .title(R.string.puzzle_answer_title)
//                        .content(R.string.puzzle_answer_msg)
//                        .input(R.string.puzzle_answer_hint, R.string.puzzle_answer_prefill, true, (materialDialog, charSequence) -> {
//                        })
//                        .iconRes(R.drawable.april)
//                        .positiveText(R.string.puzzle_answer_enter)
//                        .callback(new MaterialDialog.ButtonCallback() {
//                            @Override
//                            public void onPositive(MaterialDialog dialog) {
//                                final EditText input = dialog.getInputEditText();
//                                if (finalAnswerCheck(input.getText().toString())) {
//                                    callback.onPositive(dialog);
//                                }
//                            }
//
//                            @Override
//                            public void onNegative(MaterialDialog dialog) {
//                                callback.onNegative(dialog);
//                            }
//
//                            @Override
//                            public void onNeutral(MaterialDialog dialog) {
//                                callback.onNeutral(dialog);
//                            }
//                        }));
//
//        return dlg;
    }

    public MaterialDialog showCancelPuzzleDrawing(final MaterialDialog.ButtonCallback callback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("CancelPuzzleDrawing", getNewDialog()
                .title(R.string.leave_drawing_title)
                .content(R.string.leave_drawing_msg)
                .autoDismiss(true)
                .iconRes(R.drawable.april)
                .negativeText(R.string.leave_drawing_no)
                .positiveText(R.string.leave_drawing_yes)
                .callback(callback));
    }

    public void showYouWin(UserPuzzle userPuzzle, final Runnable callback) {
        final String creatorMsg = userPuzzle.getPuzzleMessage();
        final int gift = userPuzzle.getPuzzleCoinGift();
        String msg = context.getResources().getString(R.string.win_msg);
        msg = msg.replace("5", gift + "").replace("8", userPuzzle.GetSolveScore() + "");
        if(!TextUtils.isEmpty(creatorMsg)){
            msg += "\n" + "پیام سازنده: "+ creatorMsg;
        }
        final int icon = gift == 10 ? R.drawable.dialoge_coins1 : gift == 20 ? R.drawable.dialoge_coins2
                : gift == 30 ? R.drawable.dialoge_coins3 : R.drawable.dialoge_coins4;

        final WinDialogue dlg = new WinDialogue(context, userPuzzle, msg, icon, callback);
        dlg.show();
    }

    public MaterialDialog showPurchased(String message) {
        sfxPlayer.Play(SfxResource.BuyCoin);

        return showDialog("showPurchased",
                getNewDialog()
                        .content(message)
                        .autoDismiss(true)
                        .cancelable(true)
                        .iconRes(R.drawable.april)
                        .title("پول داریا")
                        .positiveText("خب"));
    }

    public MaterialDialog showYouSolvedBefore() {
        return showDialog("showYouSolvedBefore",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .iconRes(R.drawable.icon_warning)
                        .content(R.string.solve_err_solved)
                        .positiveText(R.string.solve_err_solved_ok));
    }

    public MaterialDialog showThisIsYourOwnPuzzle() {
        return showDialog("showThisIsYourOwnPuzzle",
                getNewDialog()
                        .cancelable(true)
                        .autoDismiss(true)
                        .iconRes(R.drawable.icon_warning)
                        .content(R.string.solve_err_own)
                        .positiveText(R.string.solve_err_own_ok));
    }

    public MaterialDialog showSharePuzzleSuccess() {
        return showDialog("showSharePuzzleSuccess",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .title(R.string.successful_share_title)
                        .content(R.string.successful_share_msg)
                        .positiveText(R.string.successful_share_yes)
                        .iconRes(R.drawable.april)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                context.startActivity(new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            }
                        }));
    }

    public MaterialDialog showMainActStart(Runnable callback){
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showMainActStart",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .iconRes(R.drawable.april)
                        .title(R.string.main_act_start_title)
                        .content(R.string.main_act_start)
                        .positiveText(R.string.btn_ok)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                callback.run();
                            }
                        })
        );
    }

    public MaterialDialog showCreateTypeActStart(Runnable callback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showCreateTypeActStart",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .iconRes(R.drawable.april)
                        .content(R.string.createtype_act_start)
                        .title(R.string.createtype_act_start_title)
                        .positiveText(R.string.btn_ok)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                callback.run();
                            }
                        })
        );
    }

    public MaterialDialog showPublicShareStart(Runnable callback) {
        sfxPlayer.Play(SfxResource.Tip);

        return showDialog("showPublicShareStart",
                getNewDialog()
                        .cancelable(false)
                        .autoDismiss(true)
                        .iconRes(R.drawable.april)
                        .content(R.string.publicshare_act_start)
                        .title(R.string.publicshare_act_start_title)
                        .positiveText(R.string.btn_ok)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                callback.run();
                            }
                        })
        );
    }

    // ----------------------------------- Helpers -------------------------------

    private CharSequence getFilteredAnswerStr(CharSequence str) {
        String retval = "";
        for (int i = 0; i < str.length(); i++) {
            char cc = str.charAt(i);
            if (Character.isSpaceChar(cc) || Mapper.allPeLetters.contains(cc + "")) {
                if (i > 0 && Character.isSpaceChar(cc) && Character.isSpaceChar(str.charAt(i - 1))) {
                    // dont allow two space next together...
                } else {
                    retval += cc;
                }
            } else {
                toastManager.useOnlyFarsiLetters();
            }
        }
        return retval;
    }

    private boolean finalAnswerCheck(String str) {
        return true;

//		String[] arr = str.split(" ");
//		ArrayList<String> arrTemp = new ArrayList<>();
//		for (String ss : arr)
//		{
//			if (!ss.isEmpty())
//			{
//				arrTemp.add(ss);
//			}
//		}
//		arr = arrTemp.toArray(new String[]{});
//
//		if (arr.length > 3)
//		{
//			toastManager.useOnlyThreeWords();
//			return false;
//		}
//		for (String s : arr)
//		{
//			if (s.length() < 2)
//			{
//				toastManager.eachWordsShouldHaveTwoLetter();
//				return false;
//			}
//		}
//
//		return true;
    }


    private MaterialDialog showDialog(String key, MaterialDialog.Builder builder) {
        MaterialDialog dialog = null;
        try {
            if (dialogsCache.containsKey(key)) {
                dialog = dialogsCache.get(key);
                if (!dialog.isShowing()) {
                    dialog = builder.show();
                    dialogsCache.put(key, dialog);
                    dialog.show();
                }
            } else {
                dialog = builder.show();
                dialogsCache.put(key, dialog);
                dialog.show();
            }
        }
        catch (Exception ex){}
        return dialog;
    }

    private MaterialDialog.Builder getNewDialog() {
        MaterialDialog.Builder retval = new MaterialDialog.Builder(context)
                .titleGravity(GravityEnum.CENTER)
                .contentGravity(GravityEnum.START)
                .buttonsGravity(GravityEnum.END)
                .theme(Theme.LIGHT)
                .backgroundColor(Color.WHITE) // c0ff00
                .positiveColor(Color.parseColor("#079100"))
                .negativeColor(Color.parseColor("#ff6339"))
                .typeface(defaultTypeface, Typeface.DEFAULT); //BMorvarid

        return retval;
    }
}
