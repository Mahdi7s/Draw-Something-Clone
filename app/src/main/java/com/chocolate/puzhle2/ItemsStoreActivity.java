package com.chocolate.puzhle2;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.events.BiFunction;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;
import com.chocolate.puzhle2.events.LampsIncrementEvent;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;

import de.greenrobot.event.EventBus;


public class ItemsStoreActivity extends BaseActivity implements View.OnClickListener
{
	private final UserScore userScore = GameUser.getGameUser().getScore();
	private ImageButton btnPicture, btnType, btnUndo;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_items_store);

		findViewById(R.id.btn_store_lamp10).setOnClickListener(this);
		findViewById(R.id.btn_store_lamp20).setOnClickListener(this);
		findViewById(R.id.btn_store_lamp50).setOnClickListener(this);

		btnPicture = (ImageButton) findViewById(R.id.btn_store_picture);
		btnType = (ImageButton) findViewById(R.id.btn_store_type);
		btnUndo = (ImageButton) findViewById(R.id.btn_store_undo);

		btnPicture.setOnClickListener(this);
		btnType.setOnClickListener(this);
		btnUndo.setOnClickListener(this);

		checkUnconsumables();
	}

	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if(hasFocus) {
			boolean getcolors = getIntent().getBooleanExtra("getcolors", false);
			boolean getLamps = getIntent().getBooleanExtra("lamps", false);
			boolean others = getIntent().getBooleanExtra("others", false);
			if (getLamps) {
				final int coins = (int) (userScore.getCoinsCount() * 0.75f);
				if(coins >= 1600) {
					getIntent().putExtra("lamps", false);

					dialogManager.showApril(R.string.getlamp_intro_title, R.string.getlamp_intro, -1, () -> {
						findViewById(R.id.btn_store_lamp50).startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh));
					});
				}
			} else if (getcolors) {
				new Handler().post(() -> {
					View lastColor = findViewById(R.id.items_seperator_img);
					ScrollView scrollView = ((ScrollView) findViewById(R.id.items_scroll));
					int sy = scrollView.getHeight() - lastColor.getBottom();
					scrollView.smoothScrollTo(0, sy);
				});
			} else if(others){
				new Handler().post(() -> {
					ScrollView scrollView = ((ScrollView) findViewById(R.id.items_scroll));
					int sy = scrollView.getBottom()+ scrollView.getMaxScrollAmount();
					scrollView.smoothScrollTo(0, sy);

					String val = getIntent().getStringExtra("val");
					Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_create_type_refresh);
					if(!TextUtils.isEmpty(val)) {
						switch (val){
							case "undo":
								findViewById(R.id.btn_store_undo).startAnimation(animation);
								break;
							case "text":
								findViewById(R.id.btn_store_type).startAnimation(animation);
								break;
							case "picture":
								findViewById(R.id.btn_store_picture).startAnimation(animation);
								break;
						}
					}
				});
			}
		}
	}

	private void checkUnconsumables() {
		if (userScore.hasColorsUnlocked(1)) {
			findViewById(R.id.frame_store_color1).setVisibility(View.GONE);
		}
		if (userScore.hasColorsUnlocked(2)) {
			findViewById(R.id.frame_store_color3).setVisibility(View.GONE);
		}
		if (userScore.hasColorsUnlocked(3)) {
			findViewById(R.id.frame_store_color2).setVisibility(View.GONE);
		}
		if (userScore.hasColorsUnlocked(4)) {
			findViewById(R.id.frame_store_color4).setVisibility(View.GONE);
		}

		if (userScore.hasImageImportUnlocked()) {
			findViewById(R.id.frame_store_picture).setVisibility(View.GONE);
		}
		if (userScore.hasTypeUnlocked()) {
			findViewById(R.id.frame_store_type).setVisibility(View.GONE);
		}
		if (userScore.hasUndoUnlocked()) {
			findViewById(R.id.frame_store_undo).setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick (View view)
	{
		switch (view.getId())
		{
			case R.id.btn_store_lamp10:
				hasEnoughCoin("بسته 10 تایی لامپ", 4000, (d) -> {
					userScore.incrementLampsCount(10);
					return null;
				});
				break;
			case R.id.btn_store_lamp20:
				hasEnoughCoin("بسته 20 تایی لامپ", 7200, (d) -> {
					userScore.incrementLampsCount(20);
					return null;
				});
				break;
			case R.id.btn_store_lamp50:
				hasEnoughCoin("بسته 50 تایی لامپ", 16000, (d) -> {
					userScore.incrementLampsCount(50);
					return null;
				});
				break;
			// -------------------------------------------------------------------------------
			case R.id.btn_store_color1:
				hasEnoughCoin("بسته 5 رنگی فانتزی", 3000, d -> {
					userScore.unlockColors(1);
					EventBus.getDefault().post(new EventMessage(EventMsgType.ColorPurchased, null, null));
					return null;
				});
				break;
			case R.id.btn_store_color2:
				hasEnoughCoin("بسته 5 رنگی طبیعت", 3000, d -> {
					userScore.unlockColors(3);
					EventBus.getDefault().post(new EventMessage(EventMsgType.ColorPurchased, null, null));
					return null;
				});
				break;
			case R.id.btn_store_color3:
				hasEnoughCoin("بسته 5 رنگی شکلات", 300, d -> {
					userScore.unlockColors(2);
					EventBus.getDefault().post(new EventMessage(EventMsgType.ColorPurchased, null, null));
					return null;
				});
				break;
			case R.id.btn_store_color4:
				hasEnoughCoin("بسته 5 رنگی دریا", 3000, d -> {
					userScore.unlockColors(4);
					EventBus.getDefault().post(new EventMessage(EventMsgType.ColorPurchased, null, null));
					return null;
				});
				break;

			case R.id.btn_store_picture:
				hasEnoughCoin("افزودن عکس به پازل", 7000, (d) -> {
					userScore.unlockImageImport();
					return null;
				});
				break;
			case R.id.btn_store_type:
				hasEnoughCoin("امکان تایپ در طراحی", 7000, (d) -> {
					userScore.unlockType();
					return null;
				});
				break;
			case R.id.btn_store_undo:
				hasEnoughCoin("افزایش تعداد حرکت به جلو و عقب", 7000, (d) -> {
					userScore.unlockUndo();
					return null;
				});
				break;
		}

		sfxPlayer.Play(SfxResource.Button);
	}

	private void hasEnoughCoin (final String itemName, final int coinAmount, BiFunction<Boolean, Object> purchaseFunc)
	{
		AnalyzeUtil.track(itemName);
		dialogManager.showPurchaseItem(itemName, coinAmount, new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						if (userScore.getCoinsCount() < coinAmount) {
							dialogManager.showNotEnoughCoin();
						} else {
							sfxPlayer.Play(SfxResource.BuyItem);

							purchaseFunc.run(true);
							userScore.decrementCoinsCount(coinAmount);
							userScore.saveEventually((e)-> {
								EventBus.getDefault().post(new LampsIncrementEvent(userScore.getLampsCount()));
								EventBus.getDefault().post(new CoinsIncrementEvent(userScore.getCoinsCount()));
								checkUnconsumables();
							});
						}
			}
		});
	}

	private void disableButton (ImageButton btn)
	{
		btn.setEnabled(false);
	}
}
