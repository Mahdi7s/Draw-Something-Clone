package com.chocolate.puzhle2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.chocolate.puzhle2.CustomViews.CTextView;
import com.chocolate.puzhle2.models.UserPuzzle;

/**
 * Created by mahdi on 12/2/15.
 */
public class WinDialogue extends Dialog {
    private UserPuzzle userPuzzle;
    private String message = "";
    private int iconRes = -1;
    private Runnable callback;
    public WinDialogue(Context context, UserPuzzle userPuzzle, String msg, int ico, Runnable callback) {
        super(context);
        this.userPuzzle = userPuzzle;

        message = msg;
        iconRes = ico;
        this.callback = callback;
    }

    private int rateing = 2;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.win_dialogue);

        if (!TextUtils.isEmpty(message)) {
            ((CTextView)findViewById(R.id.text_win)).setText(message);
        } else {
            findViewById(R.id.text_win).setVisibility(View.GONE);
            findViewById(R.id.items_seperator_img).setVisibility(View.GONE);
        }

        ImageView img = (ImageView) findViewById(R.id.winCoins);
        img.setImageResource(iconRes);

        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, v, b) -> {
            CTextView diffTxt = (CTextView) findViewById(R.id.text_difficulty);
            rateing = Math.max((int) v, 1);
            ratingBar.setRating(rateing);
            String diffSubjects[] = {"مسخره بود", "راحت بود", "زیاد سخت نبود", "سخت بود", "خیلی سخت بود"};
            diffTxt.setText(diffSubjects[rateing-1]);
        });

        findViewById(R.id.btn_submit_rate).setOnClickListener((sbt) -> {
            userPuzzle.incAllDifficulties(rateing);
            callback.run();
            dismiss();
        });
    }
}
