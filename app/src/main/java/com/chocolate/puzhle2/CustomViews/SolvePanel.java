package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.Utils.SfxPlayer;
import com.chocolate.puzhle2.Utils.SfxResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mahdi on 6/26/15.
 */
public class SolvePanel extends LinearLayout implements View.OnClickListener {
    private final int ColsCapacity = 10;
    public ArrayList<TextView> buttons = new ArrayList<>();
    private LinearLayout linearLayout1 = null;
    private LinearLayout linearLayout2 = null;
    private OnLetterClicked letterClickCallback = null;
    private float solveButtonTextSize = 6;
    private int boxSize = 0;

    public SolvePanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundResource(R.drawable.letters_frame);
        final float density = getContext().getResources().getDisplayMetrics().density;
        solveButtonTextSize = getContext().getResources().getDimension(R.dimen.solve_letter_size) / density;

        boxSize = getResources().getDimensionPixelSize(R.dimen.answer_box);
    }

    public int answerLetters = 0;
    public void setupSolvePanel(String answer, OnLetterClicked callback) {
        letterClickCallback = callback;
        setupSolvePanel(answer);
        answerLetters = answer.replace(" ", "").length();
    }

    private void setupSolvePanel(String word) {
        int firstLettersCount = 0;
        String word2 = "";
        if (word.length() <= ColsCapacity) {
            word2 = new StringBuilder(word).reverse().toString();
        } else {
            String layout1Str = word.substring(0, ColsCapacity);
            firstLettersCount = layout1Str/*.replaceAll(" ", "")*/.length();
            String layout2Str = word.substring(ColsCapacity, word.length());
            word2 = (new StringBuilder(layout1Str).reverse().toString()) + (new StringBuilder(layout2Str).reverse().toString());
        }
        List<String> words = Arrays.asList(word2.split(" "));
        LinearLayout layout = getRow();

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/BTitrBd.ttf");
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/BYekan.ttf");

        int addedBtnsCount = 0;
        for (int i = 0; i < words.size(); i++) {
            String w = words.get(i);
            for (char c : w.toCharArray()) {
                TextView btn = new TextView(getContext());
                btn.setGravity(Gravity.CENTER);
                btn.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(boxSize, boxSize);
                params.setMargins(1, 1, 1, 1);
                btn.setLayoutParams(params);
                if (++addedBtnsCount > ColsCapacity) {
                    layout = getRow();
                    addedBtnsCount = 0;
                }
                layout.addView(btn);
                buttons.add(btn);
            }

            if (i < words.size() - 1) // add space button
            {
                TextView spaceBtn = new TextView(getContext());
                spaceBtn.setGravity(Gravity.CENTER);
                spaceBtn.setTextColor(Color.WHITE);
                spaceBtn.setTag("blank-btn");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(boxSize, boxSize);
                spaceBtn.setLayoutParams(params);
                if (++addedBtnsCount > ColsCapacity) {
                    layout = getRow();
                    addedBtnsCount = 0;
                }
                layout.addView(spaceBtn);
                buttons.add(spaceBtn);
            }
        }

        int layoutBtns = layout.getChildCount();
        for (int i = layoutBtns; i < ColsCapacity; i++) // add empty buttons
        {
            TextView emptyBtn = new TextView(getContext());
            emptyBtn.setGravity(Gravity.CENTER);
            emptyBtn.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(boxSize, boxSize);
            emptyBtn.setLayoutParams(params);
            layout.addView(emptyBtn);
            buttons.add(emptyBtn);
        }

        if (buttons.size() > ColsCapacity) {
            List<TextView> btnLst1 = buttons.subList(0, firstLettersCount);
            Collections.reverse(btnLst1);
            List<TextView> btnLst2 = buttons.subList(firstLettersCount, buttons.size());
            Collections.reverse(btnLst2);
            buttons = new ArrayList<>(btnLst1);
            buttons.addAll(btnLst2);
        } else {
            Collections.reverse(buttons); // TODO right to left problem?!
            //rtlLayout(linearLayout1);
        }
//		rtlLayout(linearLayout2);

        char[] wwword = word/*.replace(" ", "")*/.toCharArray();
        for (int i = 0; i < wwword.length; i++) {
            if (wwword[i] != ' ') {
                TextView btn = buttons.get(i);
                btn.setTag(wwword[i] + "");
                btn.setBackgroundResource(R.drawable.letter_big_empty);
                btn.setTypeface(tf);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, solveButtonTextSize);
                btn.setOnClickListener(this);
            }
        }
        for (int i = wwword.length; i < buttons.size(); i++) {
            TextView emptyBtn = buttons.get(i);
            emptyBtn.setTag("blank-btn");
            emptyBtn.setVisibility(linearLayout2 == null ? GONE : INVISIBLE);
        }
    }

    private void rtlLayout(final LinearLayout layout) {
        if (layout == null) return;

        ArrayList<View> views = new ArrayList<>();
        for (int x = 0; x < layout.getChildCount(); x++) {
            views.add(layout.getChildAt(x));
        }
        layout.removeAllViews();
        for (int x = views.size() - 1; x >= 0; x--) {
            layout.addView(views.get(x));
        }
    }

    private LinearLayout getRow() {
        if (linearLayout2 != null) return linearLayout2;

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(HORIZONTAL);

        if (linearLayout1 == null) {
            linearLayout1 = linearLayout;
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
        } else {
            linearLayout2 = linearLayout;
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
        }
        addView(linearLayout);

        return linearLayout;
    }

    public void clearSolvePanel() {
        for (int i = buttons.size() - 1; i >= 0; i--) {
            TextView btn = buttons.get(i);
            if (btn.getVisibility() == VISIBLE && !btn.getText().equals("") && btn.getTag() != null && !btn.getTag().equals("blank-btn")) {
                onClick(btn);
            }
        }
    }

    private TextView getLastNonEmptyAnswerButton() {
        for (int i = 0; i < buttons.size(); i++) {
            TextView btn = buttons.get(i);
            if ((btn.getTag() == null || !btn.getTag().equals("blank-btn")) && btn.getText().equals("")) {
                if (i > 0) return buttons.get(i - 1);
            } else if (i == buttons.size() - 1) {
                return btn;
            }
        }
        return null;
    }

    public int filledLetters = 0;
    public TextView getLastEmptyAnswerButton() {
        for (TextView btn : buttons) {
            if (!(btn.getTag() == null || btn.getTag().equals("blank-btn")) && btn.getText().equals("")) {
                ++filledLetters;
                return btn;
            } else {

            }
        }
        return null;
    }

    public Boolean isAnswerCorrect() {
        if (filledLetters < answerLetters) return false;

        for (TextView btn : buttons) {
            if (btn.getTag() != null && !btn.getTag().equals("blank-btn")) {
                if (!btn.getText().equals((String) btn.getTag())) return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        TextView btn = (TextView) view;
        if ((view.getTag() != null && view.getTag().equals("blank-btn")) || btn.getText().equals(""))
            return;
        if (letterClickCallback != null) {
            SfxPlayer.getInstance(null, null).Play(SfxResource.Letter);

            btn.setBackgroundResource(R.drawable.letter_big_empty);
            String btnText = btn.getText().toString();
            letterClickCallback.onClick(btnText);
            btn.setText("");
            --filledLetters;
        }
    }

    public interface OnLetterClicked {
        public void onClick(String letter); // add new infos to params
    }
}
