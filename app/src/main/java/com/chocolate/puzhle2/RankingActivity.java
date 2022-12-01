package com.chocolate.puzhle2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.SfxResource;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserScore;
import com.chocolate.puzhle2.repos.LocalRepo;

import java.util.List;


public class RankingActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        updateTab();
    }

    private void refreshList(boolean mine) {
        final ListView listView = (ListView) findViewById(R.id.rankList);
        errorableBlock("Ranking", cid -> {
            uow.getScoreRepo().getTops(mine, 20, (data, e) ->
            {
                if (handleError(cid, e, true)) {
                    final GameUser currUser = GameUser.getGameUser();
                    final UserScore currScore = currUser.getScore();

                    if (data.size() > 0 && currScore.getTotalScore() > 0 && data.get(data.size() - 1).getTotalScore() <= currScore.getTotalScore()) {
                        boolean seten = false;
                        for (int i = 0; i < data.size(); i++) {
                            if (data.get(i).getUserId().equals(currUser.getObjectId())) {
                                final UserScore score = data.get(i);

                                ((ImageView) findViewById(R.id.userLeague)).setImageResource(WinActivity.getLeagueIcon(score.getLeague()));
                                ((TextView) findViewById(R.id.txtUserRankId)).setText((i + 1) + "");
                                ((TextView) findViewById(R.id.txtRankUser)).setText(score.getTotalScore() + "");
                                seten = true;

                                break;
                            }
                        }

                        if (!seten) {
                            UserScore score = GameUser.getGameUser().getScore();
                            ((ImageView) findViewById(R.id.userLeague)).setImageResource(WinActivity.getLeagueIcon(score.getLeague()));
                            ((TextView) findViewById(R.id.txtUserRankId)).setText((data.size() + 1) + "");
                            ((TextView) findViewById(R.id.txtRankUser)).setText(score.getTotalScore() + "");
                        }
                    } else {
                        ((ImageView) findViewById(R.id.userLeague)).setImageResource(WinActivity.getLeagueIcon(currScore.getLeague()));
                        ((TextView) findViewById(R.id.txtRankUser)).setText(currScore.getTotalScore() + "");

                        if (data.size() > 0) {
                            final UserScore score = data.get(data.size() - 1);

                            ((TextView) findViewById(R.id.txtUserRankId)).setText(currScore.getTotalScore() <= 0 || score.getTotalScore() <= 0 ? "ته جدول" : (data.size() + (data.size() * (score.getTotalScore() - currScore.getTotalScore()) / score.getTotalScore())) + "");
                        } else {
                            ((TextView) findViewById(R.id.txtUserRankId)).setText("0");
                        }
                    }
                    listView.setAdapter(new RankDataAdapter(data));
                    AdsUtils.prepareInterstitialAd(() -> new Handler().postDelayed(() -> AdsUtils.showInterstitialAd(RankingActivity.this), 1000));
                }
            });
            return "OK";
        }, dt -> {
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        final LocalRepo localRepo = uow.getLocalRepo();
        sfxPlayer.Play(SfxResource.Button);
        switch (view.getId()){
            case R.id.all_leagues:
                localRepo.setRankTab("All");
                updateTab();
                break;
            case R.id.my_league:
                localRepo.setRankTab("Mine");
                updateTab();
                break;
        }
    }

    private void updateTab(){
        ImageView imgAll = (ImageView) findViewById(R.id.all_leagues),
                imgMine = (ImageView) findViewById(R.id.my_league);
        boolean all = uow.getLocalRepo().getRankTab().equals("All");
        if (all) {
            imgAll.setImageResource(R.drawable.btn_all_leagues_on);
            imgMine.setImageResource(R.drawable.btn_myleague);
        } else {
            imgMine.setImageResource(R.drawable.btn_myleague_on);
            imgAll.setImageResource(R.drawable.btn_all_leagues);
        }
        refreshList(!all);
    }

    // ----------------------------------------------------------------------------------------------------------

    public class RankDataAdapter extends BaseAdapter {
        private List<UserScore> scores = null;

        public RankDataAdapter(List<UserScore> scores) {

            this.scores = scores;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return scores.size();
        }

        @Override
        public UserScore getItem(int i) {
            return scores.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private boolean currentUserSet = false;

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) RankingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.rank_list_item, viewGroup, false);
            }

            TextView rankId = (TextView) view.findViewById(R.id.txtRankId);
            TextView rankName = (TextView) view.findViewById(R.id.txtRankName);
            TextView rankScore = (TextView) view.findViewById(R.id.txtRankScore);
            TextView rankSolved = (TextView) view.findViewById(R.id.txtRankSolved);
            TextView rankCreated = (TextView) view.findViewById(R.id.txtRankCreated);
            TextView likedPuzzles = (TextView) view.findViewById(R.id.txtLikedPuzzles);
            ImageView league = (ImageView) view.findViewById(R.id.ranking_league_icon);

            UserScore score = scores.get(i);
            rankId.setText(" ." + (i + 1));
            rankName.setText(score.getDisplayName());
            rankScore.setText(score.getTotalScore() + "");
            rankSolved.setText(score.getSolvedCount() + "");
            rankCreated.setText(score.getCreatedCount() + "");
            likedPuzzles.setText(score.getLikesCount() + "");
            league.setImageResource(WinActivity.getLeagueIcon(score.getLeague()));

            LinearLayout bgLayout = (LinearLayout) view.findViewById(R.id.frame_mpuzzles_bg);
            View userProfile = view.findViewById(R.id.rank_profile);
            if (GameUser.getGameUser().getObjectId().equals(score.getUserId())) {
                bgLayout.setBackgroundResource(R.drawable.frame_mypuzzle_player);
                userProfile.setVisibility(View.GONE);
            } else {
                bgLayout.setBackgroundResource(R.drawable.frame_my_puzzles);
                userProfile.setVisibility(View.VISIBLE);
                userProfile.setOnClickListener((up) -> {
                    ProfileActivity.userScore = score;
                    RankingActivity.this.startActivity(new Intent(RankingActivity.this, ProfileActivity.class));
                });
            }

            View footer = view.findViewById(R.id.achivement_footer);
            if (scores.size() == i + 1) {
                footer.setVisibility(View.VISIBLE);
            } else {
                footer.setVisibility(View.GONE);
            }

            return view;
        }
    }

}
