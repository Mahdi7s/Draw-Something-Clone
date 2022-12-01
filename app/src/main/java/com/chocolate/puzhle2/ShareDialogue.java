package com.chocolate.puzhle2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.SfxPlayer;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.events.BiFunction2;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;

/**
 * Created by mahdi on 2/28/16.
 */
public class ShareDialogue extends Dialog {
    private boolean isPublic;
    private BiFunction2<Integer, String, String> callbackFunc = null;
    private DialogManager dlgManager = null;

    public ShareDialogue(Context context, DialogManager dialogManager, boolean isPublic, BiFunction2<Integer, String, String> callback) {
        super(context);
        this.isPublic = isPublic;
        this.dlgManager = dialogManager;

        callbackFunc = callback;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(true);
        setContentView(R.layout.share_dialogue);

        if(!isPublic) {
            findViewById(R.id.pnl_share_btns).setVisibility(View.GONE);
            findViewById(R.id.items_seperator_img).setVisibility(View.GONE);
            findViewById(R.id.share_txt_1).setVisibility(View.GONE);
            findViewById(R.id.share_txt_2).setVisibility(View.GONE);

            findViewById(R.id.btn_share_private).setVisibility(View.VISIBLE);
        }

        final EditText txt = (EditText) findViewById(R.id.txt_feedback);

        findViewById(R.id.btn_share_normal).setOnClickListener((v) -> {
            callbackFunc.run(0, txt.getText().toString());
            dismiss();
        });
        findViewById(R.id.btn_share_private).setOnClickListener((v) -> {
            callbackFunc.run(0, txt.getText().toString());
            dismiss();
        });

        findViewById(R.id.btn_share_vip).setOnClickListener((v) -> {
            final UserScore score = GameUser.getCurrScore();
            if (score.decrementCoinsCount(200)) {
                score.saveInBackground(e -> {
                    if(e != null) {
                        score.saveEventually();
                    }
                });
                callbackFunc.run(200, txt.getText().toString());
                dismiss();
            } else {
                dlgManager.showNotEnoughCoin();
            }
        });
    }
}
