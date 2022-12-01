package com.chocolate.puzhle2;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.chocolate.puzhle2.Utils.AdsUtils;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.events.CoinsIncrementEvent;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MyPuzzlesActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_puzzles);
        AdsUtils.setListenerForBanner(this, R.id.banner_ad_view);

        refreshMyPuzzles();
    }

    private void refreshMyPuzzles() {
        final ListView listView = (ListView) findViewById(R.id.myPuzzleList);
        errorableBlock("MyPuzzles", cid -> {
            uow.getPuzzleRepo().getMyPuzzles((list, e) -> {
                if (handleError(cid, e, true)) {
                    listView.setAdapter(new MyPuzzlesDataAdapter(MyPuzzlesActivity.this, list));
                }
            });
            return null;
        }, data -> { });
    }

    @Override
    public void onClick(View view) {
//		switch (view.getId()){
//			case R.id.btn_mypuzzles_refresh:
//				UserPuzzleRepo.isDirty = true;
//				refreshMyPuzzles();
//				break;
//		}
    }

    public class MyPuzzlesDataAdapter extends BaseAdapter {
        private List<UserPuzzle> myPuzzles = null;
        private Context context = null;

        public MyPuzzlesDataAdapter(Context context, List<UserPuzzle> myPuzzles) {
            this.myPuzzles = myPuzzles;
            this.context = context;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return myPuzzles.size() <= 0 ? 1 : myPuzzles.size();
        }

        @Override
        public UserPuzzle getItem(int i) {
            return myPuzzles.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) MyPuzzlesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.mypuzzles_list_item, viewGroup, false);
            }

            if (myPuzzles.size() <= 0 && i == 0) {
                view.findViewById(R.id.no_puzzle_image).setVisibility(View.VISIBLE);
                view.findViewById(R.id.puzzle_item_panel).setVisibility(View.GONE);
                view.findViewById(R.id.myPuzzlesFooter).setVisibility(View.VISIBLE);
                return view;
            }

            ImageView puzPic = (ImageView) view.findViewById(R.id.imgPuzPic);
            ImageView sentToFav = (ImageView) view.findViewById(R.id.btnSendToFavourites);

            TextView txtSolved = (TextView) view.findViewById(R.id.txtMSolveCount);
            TextView txtLiked = (TextView) view.findViewById(R.id.txtMLikesCount);

            UserPuzzle puzzle = myPuzzles.get(i);
            File puzzleFile = new File(FileUtility.getMyPuzzleFolder(context) + puzzle.getObjectId() + ".jpg");

            if (puzzleFile.exists()) {
                sentToFav.setOnClickListener(view1 -> {
                    dialogManager.showQuestion("ارسال به دوستان", "دوست داری پازلی رو که ساختی واسه دوستات ارسال کنی؟ (هزینه: ۵۰ سکه)", "هان", "نوچ", () -> {
                        final UserScore score = GameUser.getGameUser().getScore();
                        if (score.decrementCoinsCount(50)) {
                            score.pinInBackground();
                            EventBus.getDefault().post(new CoinsIncrementEvent(score.getCoinsCount()));
                            FileUtility.shareImage(context, uow, puzzle, new File[]{puzzleFile}, false);
                        } else {
                            dialogManager.showNotEnoughCoin();
                        }
                    }, () -> {
                    });
                });
                Picasso.with(context).load(Uri.fromFile(puzzleFile))/*.networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)*/.config(Bitmap.Config.RGB_565).into(puzPic);
            } else {
                // puzPic.setAlpha(.5f); requiers api call 11 ***
            }

            final ImageView btnPromote = (ImageView) view.findViewById(R.id.btn_promote);
            final boolean canPromote = puzzle.getSentToPublic() && puzzle.getPromoteDate().plusHours(2).isBeforeNow();
            btnPromote.setImageResource(canPromote ? R.drawable.btn_promote : R.drawable.btn_promote_disabled);
            btnPromote.setOnClickListener((v) -> {
                if (canPromote) {
                    dialogManager.showQuestion("معروف شو!", "دوست داری پازلی رو که ساختی به افراد بیشتری نمایش بدی؟! (هزینه: ۳۰۰ سکه)", "هان", "نوچ", () -> {
                        final UserScore score = GameUser.getGameUser().getScore();
                        if (score.decrementCoinsCount(300)) {
                            puzzle.setPromote();
                            errorableBlock("Promote", cid -> {
                                ParseObject.saveAllInBackground(Arrays.asList(score, puzzle), (e) -> {
                                    if (handleError(cid, e, true)) {
                                        toastManager.show("هورااا، پازلت رفت جزو تازه ترین ها !");
                                        btnPromote.setImageResource(R.drawable.btn_promote_disabled);
                                        btnPromote.setEnabled(false);
                                    } else {
                                        score.incrementCoinsCount(300);
                                    }
                                    score.pinInBackground();
                                    EventBus.getDefault().post(new CoinsIncrementEvent(score.getCoinsCount()));
                                });
                                return null;
                            }, dt -> {
                            });
                        } else {
                            dialogManager.showNotEnoughCoin();
                        }
                    }, () -> { });
                } else {
                    toastManager.show(puzzle.getSentToPublic() ? "هنوز وقتش نشده :(" : "ارسال عمومی نشده که :(");
                }
            });

            RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
            ratingBar.setRating(puzzle.getAvgDifficulty());

            txtLiked.setText(" " + puzzle.getLikes());
            txtSolved.setText(puzzle.getSolves() + "");

            if (myPuzzles.size() == i + 1) {
                view.findViewById(R.id.myPuzzlesFooter).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.myPuzzlesFooter).setVisibility(View.GONE);
            }

            return view;
        }
    }

}
