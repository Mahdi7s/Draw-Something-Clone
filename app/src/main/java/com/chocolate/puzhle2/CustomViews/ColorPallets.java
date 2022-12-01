package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chocolate.puzhle2.CoinStoreActivity;
import com.chocolate.puzhle2.CreatePuzzleActivity;
import com.chocolate.puzhle2.ItemsStoreActivity;
import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.LocalRepo;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by mahdi on 6/11/15.
 */
public class ColorPallets extends LinearLayout implements View.OnClickListener {
    private ArrayList<ImageView> colorsArr = new ArrayList<>();
    private boolean initialized = false;

    private static String selectedColor = "#000000";

    public ColorPallets(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        EventBus.getDefault().register(this);
    }

    public void onEvent(EventMessage msg) {
        if (msg.getMsgType() == EventMsgType.ColorPurchased) {
            removeAllViews();
            setupPallets();
            hide();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!initialized) {
            setupPallets();
            initialized = true;
        }
    }

    private void setupPallets() {
        Context context = getContext();
        final int colors[] = new int[]{
                R.drawable.pallete_01,
                R.drawable.pallete_02,
                R.drawable.pallete_03,
                R.drawable.pallete_04,
                R.drawable.pallete_05,

                R.drawable.pallete_06,
                R.drawable.pallete_07,
                R.drawable.pallete_08,
                R.drawable.pallete_09,
                R.drawable.pallete_10,

                R.drawable.pallete_11,
                R.drawable.pallete_12,
                R.drawable.pallete_13,
                R.drawable.pallete_14,
                R.drawable.pallete_15,

                R.drawable.pallete_16,
                R.drawable.pallete_17,
                R.drawable.pallete_18,
                R.drawable.pallete_19,
                R.drawable.pallete_20,

                R.drawable.pallete_21,
                R.drawable.pallete_22,
                R.drawable.pallete_23,
                R.drawable.pallete_24,
                R.drawable.pallete_25
        };

        final String colorsHex[] = new String[]{
                "#000000",
                "#676767",
                "#b4b4b4",
                "#000fff",
                "#ff0000",

                "#ff6666",
                "#ff79b8",
                "#f333ff",
                "#ff17a5",
                "#b1007d",

                "#ffb86c",
                "#ff8400",
                "#a53e11",
                "#683b00",
                "#8e0000",

                "#fffd6d",
                "#fff000",
                "#a8ff00",
                "#35ce00",
                "#006e03",

                "#9aebff",
                "#17d8df",
                "#008aff",
                "#404eb1",
                "#8400d7"
        };

        final UserScore userScore = GameUser.getGameUser().getScore();
        boolean canPurchase = false;
        final LocalRepo localRepo = new LocalRepo(getContext());
        // 5 * 5 pallets
        for (int i = 0; i < 5; i++) {
            if (i == 0 || (i > 0 && (userScore.hasColorsUnlocked(i) || CreatePuzzleActivity.canUseItem()))) {
                LinearLayout row = new LinearLayout(context);
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.weight = 1;
                lp.setMargins(5, 0, 5, 0);
                row.setLayoutParams(lp);
                row.setOrientation(HORIZONTAL);
                row.setTag(i + 1);
                for (int j = 0; j < 5; j++) {
                    int idx = i * 5 + j;

                    ImageView img = new ImageView(context);
                    img.setTag(colorsHex[idx]);
                    img.setPadding(2, 2, 2, 2);
                    img.setAdjustViewBounds(true);
                    img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    img.setImageResource(colors[idx]);
                    img.setBackgroundColor(Color.TRANSPARENT);
                    lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    lp.weight = 1;
                    img.setLayoutParams(lp);
                    img.setOnClickListener(this);
                    row.addView(img);
                    colorsArr.add(img);
                }
                addView(row);
                addRowBackground(i, canPurchase, row);
            } else {
                canPurchase = true;
            }

            if (i == 4) {
                LinearLayout row = addRowBackground(i, canPurchase, null);
                if(row != null)
                    addView(row);
            }
        }
    }

    private LinearLayout addRowBackground(int rowIndex, boolean canPurchase, LinearLayout row) {
        Context context = getContext();

        if (rowIndex == 4 && canPurchase) { // this is purchase button
            row = new LinearLayout(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            lp.setMargins(5, 0, 5, 0);
            row.setLayoutParams(lp);

            row.setBackgroundResource(R.drawable.color_panel_bottom_initial);
            row.setClickable(true);
            row.setOnClickListener(v -> {
                context.startActivity(new Intent(context, GameUser.getCurrScore().getBoolean("rated") ? ItemsStoreActivity.class : CoinStoreActivity.class).putExtra("getcolors", true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            });
        } else if(row != null) {// row is null !
            row.setBackgroundResource(rowIndex == 0 ? R.drawable.color_panel_top :
                    rowIndex == 4 ? R.drawable.color_panel_bottom : R.drawable.color_panel_middle);
        }
        return row;
    }

    @Override
    public void onClick(View view) {
        ImageView img = (ImageView) view;
        if (img.isEnabled()) {
            for (ImageView otherImg : colorsArr) {
                if (otherImg != img) {
                    otherImg.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            img.setBackgroundResource(R.drawable.highlight);
            EventBus.getDefault().post(new EventMessage(EventMsgType.BrushColorChange, img.getTag(), getId()));
            selectedColor = (String) img.getTag();
            //TODO a little delay?!
            hide();
        }
    }

    public void show() {
        setVisibility(VISIBLE);
        EventBus.getDefault().post(new EventMessage(EventMsgType.BrushColorChange, selectedColor, getId()));
    }

    public void hide() {
        setVisibility(INVISIBLE);
    }
}
