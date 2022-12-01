package com.chocolate.puzhle2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.ProgressImage;
import com.chocolate.puzhle2.models.UserScore;

/**
 * Created by System 1 on 22/11/2015.
 */
public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    public static UserScore userScore = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AdsUtils.setListenerForBanner(this, R.id.banner_ad_view);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.progressbar_1);
        ProgressImage img = new ProgressImage(bmp, 0.5f);
        ((ImageView) findViewById(R.id.progress_fill)).setImageDrawable(img);

        ScoreBoardActivity.updateView(this, userScore);

        findViewById(R.id.report_profile).setVisibility(userScore.canAddProfileReport() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.player_link:
                ScoreBoardActivity.openLink(this, userScore.getLink());
                break;
            case R.id.report_profile:
                dialogManager.showProfileReport(() -> {
                    userScore.addProfileReport();
                    userScore.saveInBackground();
                    findViewById(R.id.report_profile).setVisibility(View.INVISIBLE);
                    toastManager.show("گزارشت ثبت شد.");
                });
                break;
        }
    }
}
