package com.chocolate.puzhle2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.UserScoreRepo;

import java.util.List;

import de.greenrobot.event.EventBus;

public class AchievementsActivity extends BaseActivity implements View.OnClickListener
{
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievements);
		AdsUtils.setListenerForBanner(this, R.id.banner_ad_view);

		refreshAchievements();
	}

	private void refreshAchievements() {
		final ListView achievementsList = (ListView) findViewById(R.id.achievementsList);
		errorableBlock("Achievements", cid -> {
			uow.getScoreRepo().getUserAchievements((achievements, e) -> {
				if(handleError(cid, e, true)) {
					achievementsList.setAdapter(new AchievementsDataAdapter(achievements));

					if(uow.getLocalRepo().isActFirstSeen(AchievementsActivity.this)){
						dialogManager.showApril(R.string.achievements_act_title, R.string.achievements_act_msg, -1, null);
					}
				}
				return null;
			});
			return null;
		}, data -> {});
	}

	@Override
	public void onClick(View view) {
//		switch (view.getId()){
//			case R.id.btn_achievement_refresh:
//				AchievementsRepo.isDirty = true;
//				refreshAchievements();
//				break;
//		}
	}

	public class AchievementsDataAdapter extends BaseAdapter {
		private final List<UserScoreRepo.AchievementViewModel> achievementViewModels;
		public AchievementsDataAdapter(List<UserScoreRepo.AchievementViewModel> achievementViewModels) {
			this.achievementViewModels = achievementViewModels;
		}

		@Override
		public int getCount() {
			return achievementViewModels.size();
		}

		@Override
		public Object getItem(int i) {
			return achievementViewModels.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			if (view == null)
			{
				LayoutInflater inflater = (LayoutInflater) AchievementsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.achievement_list_item, viewGroup, false);
			}

			ImageView achievePic = (ImageView) view.findViewById(R.id.imgAchievePic);
			ImageView getGift = (ImageView) view.findViewById(R.id.btnAchieveGetGift);

			ImageView star1 = (ImageView) view.findViewById(R.id.imgAStar1);
			ImageView star2 = (ImageView) view.findViewById(R.id.imgAStar2);
			ImageView star3 = (ImageView) view.findViewById(R.id.imgAStar3);

			ImageView imgTitle = (ImageView) view.findViewById(R.id.imgAchieveTitle);
			ImageView imgText = (ImageView) view.findViewById(R.id.imgAchieveText);

			UserScoreRepo.AchievementViewModel achievement = achievementViewModels.get(i);
			int achieveStar = achievement.AchievedStep;
			achievePic.setImageResource(achievement.IconId);
			getGift.setImageResource(achievement.getButtonId());
			imgTitle.setImageResource(achievement.TitleId);
			imgText.setImageResource(achievement.getDescriptionId());

			star1.setImageResource(R.drawable.star_icon_empty);
			star2.setImageResource(R.drawable.star_icon_empty);
			star3.setImageResource(R.drawable.star_icon_empty);

			if (achieveStar >= 1)
			{
				star1.setImageResource(R.drawable.star_icon);
			}
			if (achieveStar >= 2)
			{
				star2.setImageResource(R.drawable.star_icon);
			}
			if (achieveStar >= 3)
			{
				star3.setImageResource(R.drawable.star_icon);
			}

			getGift.setOnClickListener((btn) -> {
				if(achievement.canGetAchievement()){
					final UserScore score = GameUser.getGameUser().getScore();

					score.incrementCoinsCount(achievement.getGiftCoins());
					EventBus.getDefault().post(new CoinsIncrementEvent(score.getCoinsCount()));

					switch (achievement.IconId) {
						case R.drawable.icon_davinci:
							score.incPuzzleCreationGotStep();
							break;
						case R.drawable.icon_iqio:
							score.incPuzzleSolvingGotStep();
							break;
						case R.drawable.icon_bob:
							score.incUsedToolsGotStep();
							break;
						case R.drawable.icon_bear:
							score.incPuzzleLikesGotStep();
							break;
						case R.drawable.icon_zebel:
							score.incTopSolversGotStep();
							break;
						case R.drawable.icon_luke:
							score.incLeagueGotStep();
							break;
					}
					++achievement.GottenStep;
					score.pinInBackground();
					AchievementsDataAdapter.this.notifyDataSetChanged();
				}
			});

			View footer = view.findViewById(R.id.ach_list_footer);
			if (achievementViewModels.size() == i + 1) {
				footer.setVisibility(View.VISIBLE);
			} else {
				footer.setVisibility(View.GONE);
			}

			return view;
		}
	}
}
