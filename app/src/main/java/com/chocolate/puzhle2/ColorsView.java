package com.chocolate.puzhle2;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;

import de.greenrobot.event.EventBus;

/**
 * Created by Mahdi7s on 12/21/2014.
 */
public class ColorsView extends LinearLayout implements View.OnClickListener {
    private ImageButton currPaint = null;
    private Color currColor;

    public ColorsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setClickable(true);
        this.setEnabled(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        View colorsLayout = LayoutInflater.from(context).inflate(R.layout.colors_layout, this, true);

        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        for (int i = 0; i < paintLayout.getChildCount(); i++) {
            View v = paintLayout.getChildAt(i);
            if (v instanceof ImageButton) {
                v.setOnClickListener(this);
            }
        }
    }

    public Color getSelectedColor() {
        return currColor;
    }

    @Override
    public void onClick(View view) {
        if (view != currPaint) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            EventBus.getDefault().post(new EventMessage(EventMsgType.BrushColorChange, color, R.id.pen_size_panel));

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }
}
