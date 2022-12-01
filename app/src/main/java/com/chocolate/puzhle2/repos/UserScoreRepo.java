package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.events.BiFunction2;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.models.UserStatistics;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by choc01ate on 5/11/2015.
 */
public class UserScoreRepo extends BaseRepo<UserScore> {
    private GameUserRepo gameUserRepo;

    public UserScoreRepo(Context context, DialogManager dialogManager, ToastManager toastManager, GameUserRepo gameUserRepo) {
        super(context, dialogManager, toastManager, UserScore.class);
        this.gameUserRepo = gameUserRepo;
    }

    private static List<UserScore> topScores = null;
    private static List<UserScore> topScoresMine = null;

    public void getTops(boolean mine, int limit, FindCallback<UserScore> callback) {
        if (GameUserRepo.isRefreshed || (mine ? topScoresMine == null : topScores == null) || topScores.size() == 0) {
            GameUser gUser = GameUser.getGameUser();
            ParseQuery<UserScore> query = ParseQuery.getQuery(UserScore.class);
            if (mine) {
                query = query.whereEqualTo("League", gUser.getScore().getLeague());
            }
            query.orderByDescending("TotalScore").whereExists("UserId").whereExists("DisplayName").setLimit(limit)
                    .findInBackground((tops, e) -> {
                        if (mine) {
                            topScoresMine = tops;
                        } else {
                            topScores = tops;
                        }
                        callback.done(tops, e);
                    });

            GameUserRepo.isRefreshed = false;
        } else {
            callback.done(mine ? topScoresMine : topScores, null);
        }
    }

    //-------------------------------Achievements------------------------------
//    public void initAchievement(final GameUser currentGameUser, SaveCallback callback) {
//        GameUser.getGameUser().getScore().refreshAchievements();
//        GameUser.getGameUser().getScore().saveInBackground(e -> {
//            if (e == null) {
//                GameUser.getGameUser().getScore().pinInBackground();
//                currentGameUser.setAchievement(achievements);
//                currentGameUser.saveInBackground(callback);
//            } else {
//                callback.done(e);
//            }
//        });
//    }

    public void getUserAchievements(final BiFunction2<List<AchievementViewModel>, ParseException, Object> callback) {
        gameUserRepo.refreshGameUser((gameUser, e) -> {
            if (e == null) {
                final UserScore score = gameUser.getScore();
                score.refreshAchievements();
                final ArrayList<AchievementViewModel> achievementViewModels = new ArrayList<>();

                // create puzzle / davin ci
                AchievementViewModel avm = new AchievementViewModel();
                avm.IconId = R.drawable.icon_davinci;
                avm.TitleId = R.drawable.text_davinci;
                avm.Description1Id = R.drawable.text_davinci_1;
                avm.Description2Id = R.drawable.text_davinci_2;
                avm.Description3Id = R.drawable.text_davinci_3;
                avm.AchieveName = "دوناتلو";
                avm.GottenStep = score.getPuzzleCreationGotStep();
                avm.AchievedStep = score.getPuzzleCreationAchievedStep();
                achievementViewModels.add(avm);

                // solve puzzle / EQ sun
                avm = new AchievementViewModel();
                avm.IconId = R.drawable.icon_iqio;
                avm.TitleId = R.drawable.text_iqio;
                avm.Description1Id = R.drawable.text_iqio_1;
                avm.Description2Id = R.drawable.text_iqio_2;
                avm.Description3Id = R.drawable.text_iqio_3;
                avm.AchieveName = "لئوناردو";
                avm.GottenStep = score.getPuzzleSolvingGotStep();
                avm.AchievedStep = score.getPuzzleSolvingAchievedStep();
                achievementViewModels.add(avm);

                // like / kherse mehraboon
                avm = new AchievementViewModel();
                avm.IconId = R.drawable.icon_bear;
                avm.TitleId = R.drawable.text_bear;
                avm.Description1Id = R.drawable.text_bear_1;
                avm.Description2Id = R.drawable.text_bear_2;
                avm.Description3Id = R.drawable.text_bear_3;
                avm.AchieveName = "مایکل آنجلو";
                avm.GottenStep = score.getPuzzleLikesGotStep();
                avm.AchievedStep = score.getPuzzleLikesAchievedStep();
                achievementViewModels.add(avm);

                // top 3 solver / zebel khan
                avm = new AchievementViewModel();
                avm.IconId = R.drawable.icon_zebel;
                avm.TitleId = R.drawable.text_zebel;
                avm.Description1Id = R.drawable.text_zebel_1;
                avm.Description2Id = R.drawable.text_zebel_2;
                avm.Description3Id = R.drawable.text_zebel_3;
                avm.AchieveName = "رفایل";
                avm.GottenStep = score.getTopSolversGotStep();
                avm.AchievedStep = score.getTopSolversAchieveStep();
                achievementViewModels.add(avm);

                // using design tools / bob ras
                avm = new AchievementViewModel();
                avm.IconId = R.drawable.icon_bob;
                avm.TitleId = R.drawable.text_bob;
                avm.Description1Id = R.drawable.text_bob_1;
                avm.Description2Id = R.drawable.text_bob_2;
                avm.Description3Id = R.drawable.text_bob_3;
                avm.AchieveName = "کیسی";
                avm.GottenStep = score.getUsedToolsGotStep();
                avm.AchievedStep = score.getUsedToolsAchievedStep();
                achievementViewModels.add(avm);

                // league /
                avm = new AchievementViewModel();
                avm.IconId = R.drawable.icon_luke;
                avm.TitleId = R.drawable.text_luke;
                avm.Description1Id = R.drawable.text_luke_1;
                avm.Description2Id = R.drawable.text_luke_2;
                avm.Description3Id = R.drawable.text_luke_3;
                avm.AchieveName = "استاد اسپلینتر";
                avm.GottenStep = score.getLeagueGotStep();
                avm.AchievedStep = score.getLeagueAchieveStep();
                achievementViewModels.add(avm);

                callback.run(achievementViewModels, e);
            } else {
                callback.run(null, e);
            }
        });
    }

    public class AchievementViewModel {
        public int AchievedStep = 0;
        public int GottenStep = 0;
        public int IconId;
        public int TitleId;
        public int Description1Id;
        public int Description2Id;
        public int Description3Id;

        public String AchieveName = "";
        private String GotNotify = "تونستی %s افتخار %s رو کسب کنی، برو به بخش افتخارات تا جایزه شو بگیری :)";

        public String getGotNotify() {
            if (canGetAchievement()) {
                return String.format(GotNotify, AchievedStep == 1 ? "اولین" : AchievedStep == 2 ? "دومین" : "سومین", AchieveName);
            }
            return null;
        }

        public int getStars() {
            return AchievedStep;
        }

        public int getTitleId() {
            return TitleId;
        }

        public int getDescriptionId() {
            return GottenStep == 0 ? Description1Id : GottenStep == 1 ? Description2Id : Description3Id;
        }

        public boolean canGetAchievement() {
            return GottenStep < AchievedStep && AchievedStep > 0;
        }

        public int getButtonId() {
            boolean enabled = canGetAchievement();

            if (IconId == R.drawable.icon_luke) {
                return GottenStep == 0 ? (enabled ? R.drawable.btn_reward100 : R.drawable.btn_reward100_disable) :
                        GottenStep == 1 ? (enabled ? R.drawable.btn_reward200 : R.drawable.btn_reward200_disable) :
                                (enabled ? R.drawable.btn_reward300 : R.drawable.btn_reward300_disable);
            } else {
                return GottenStep == 0 ? (enabled ? R.drawable.btn_reward5 : R.drawable.btn_reward5_disable) :
                        GottenStep == 1 ? (enabled ? R.drawable.btn_reward25 : R.drawable.btn_reward25_disable) :
                                (enabled ? R.drawable.btn_reward150 : R.drawable.btn_reward150_disable);
            }
        }

        public int getGiftCoins() {
            if (IconId == R.drawable.icon_luke) {
                return GottenStep == 0 ? 1000 : GottenStep == 1 ? 2000 : 3000;
            } else {
                return (GottenStep == 0 ? UserStatistics.Achievement1CoinGift : GottenStep == 1 ? UserStatistics.Achievement2CoinGift
                        : UserStatistics.Achievement3CoinGift) * 10;
            }
        }
    }
}
