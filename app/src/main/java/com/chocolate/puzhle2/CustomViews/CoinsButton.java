package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chocolate.puzhle2.CoinStoreActivity;
import com.chocolate.puzhle2.R;
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
 * Created by choc01ate on 5/11/2015.
 */
public class CoinsButton extends RelativeLayout implements View.OnClickListener
{
	private final UnitOfWork uow;
	private CTextView numView = null;

	public CoinsButton (Context context, AttributeSet attrs)
	{
		super(context, attrs);
		ToastManager toastManager = new ToastManager(context);
		DialogManager dialogManager = new DialogManager(context, toastManager);
		uow = new UnitOfWork(context, dialogManager, toastManager);

		final ImageView imgBg = new ImageView(this.getContext());
		imgBg.setImageResource(R.drawable.coin_numbers);
		imgBg.setAdjustViewBounds(true);
		imgBg.setBackgroundColor(Color.TRANSPARENT);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		imgBg.setLayoutParams(params);
		addView(imgBg);

		numView = new CTextView(this.getContext(), null);
		final float density = getContext().getResources().getDisplayMetrics().density;
		numView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.header_font_size) / density);
		numView.setTextColor(Color.WHITE);
		params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		params.setMargins(3, 0, 0, 3);
		numView.setLayoutParams(params);
		addView(numView);

		setOnClickListener(this);

		EventBus.getDefault().register(this);
	}

	public void onEvent (CoinsIncrementEvent event)
	{
		numView.setText(event.getCoinsCount() + "");
	}

	private static UserScore userScore = null;
	@Override
	protected void onAttachedToWindow ()
	{
		super.onAttachedToWindow();

		refreshCoins();
	}

	public void refreshCoins ()
	{
		if (!uow.getLocalRepo().isFirstRun())
		{
			if(userScore == null) {
				userScore = GameUser.getGameUser().getScore();
			}
			numView.setText(userScore.getCoinsCount() + "");
		}
	}

	@Override
	public void onClick (View v)
	{
		AnalyzeUtil.track("button_coins");
		SfxPlayer.getInstance(null, null).Play(SfxResource.Button);

		Intent intent = new Intent(getContext(), CoinStoreActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		getContext().startActivity(intent);
	}
}
