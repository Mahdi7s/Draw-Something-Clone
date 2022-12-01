package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.ScoreBoardActivity;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.SfxPlayer;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.UnitOfWork;

import de.greenrobot.event.EventBus;

/**
 * Created by mahdi on 9/28/15.
 */
public class HeaderLeagueButton extends ImageView {
    private final UnitOfWork uow;

    public HeaderLeagueButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        ToastManager toastManager = new ToastManager(context);
        DialogManager dialogManager = new DialogManager(context, toastManager);
        uow = new UnitOfWork(context, dialogManager, toastManager);

        setOnClickListener(v -> {
            AnalyzeUtil.track("button_scoreboard");

            SfxPlayer.getInstance(null, null).Play(SfxResource.Button);
            context.startActivity(new Intent(context, ScoreBoardActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        });

        EventBus.getDefault().register(this);
    }

    public void onEvent (CoinsIncrementEvent event)
    {
        refreshLeague();
    }

    private static UserScore userScore = null;
    @Override
    protected void onAttachedToWindow ()
    {
        super.onAttachedToWindow();
        refreshLeague();
    }

    private void refreshLeague(){
        if (!uow.getLocalRepo().isFirstRun()) {
            if(userScore == null) {
                userScore = GameUser.getGameUser().getScore();
            }
            final int league = userScore.getLeague(),
                    leagueId = league == 0 ? R.drawable.league1 : league == 1 ? R.drawable.league2 : league == 2 ? R.drawable.league3
                            : league == 3 ? R.drawable.league4 : league == 4 ? R.drawable.league5 : R.drawable.league6;
            setImageResource(leagueId);
        }
    }
}
