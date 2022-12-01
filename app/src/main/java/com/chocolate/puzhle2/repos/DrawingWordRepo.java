package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.events.BiFunction3;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.ArrayList;

/**
 * Created by System 1 on 06/09/2015.
 */
public class DrawingWordRepo extends BaseRepo<DrawingWord> {
    private final static String PinTag = "DrawingWords";
    private static ArrayList<DrawingWord> LastWords = new ArrayList<>();

    public DrawingWordRepo(Context context, DialogManager dialogManager, ToastManager toastManager) {
        super(context, dialogManager, toastManager, DrawingWord.class);
    }

    public void get3Words(final FindCallback<DrawingWord> wordsCallback) { // 3 calls for 10 use -> 1use = 0.3 call <3
        try {
            if (LastWords != null && LastWords.size() == 3) {
                wordsCallback.done(LastWords, null);
                return;
            }

            LastWords = new ArrayList<>();
            final GetCallback<DrawingWord> getCallback = (drawingWord, e) -> {
                if (e == null) {
                    LastWords.add(drawingWord);

                    if (LastWords.size() == 3) {
                        wordsCallback.done(LastWords, e);
                    }
                } else {
                    wordsCallback.done(null, e);
                }
            };

            final BiFunction3<ParseQuery<DrawingWord>, ParseQuery<DrawingWord>, Integer, Object> randomQ = (q1, q2, c) -> {
                q1.fromPin(PinTag).getFirstInBackground((localWord, e) -> {
                    if (e == null && localWord != null) {
                        getCallback.done(localWord, e);
                    } else {
                        q2.orderByAscending("updatedAt")/*.setSkip(new Random().nextInt(c - 15)).setLimit(10)*/.findInBackground((words, e2) -> {
                            if (e2 == null && words.size() > 0) {
                                ArrayList<ParseObject> arr = new ArrayList<>();
                                ParseRelation<DrawingWord> rel = GameUser.getCurrScore().getSeenWords();
                                for (DrawingWord w : words) {
                                    w.incrementViews();
                                    rel.add(w);
                                    arr.add(w);
                                }
                                arr.add(GameUser.getGameUser());
                                getCallback.done(words.get(0), e2);
                                ParseObject.saveAllInBackground(arr);
                                ParseObject.pinAllInBackground(PinTag, words);
//                                ParseObject.saveAllInBackground(arr, (e3) -> {
//                                    if (e3 == null) {
//                                        ParseObject.pinAllInBackground(PinTag, words);
//                                        getCallback.done(words.get(0), e2);
//                                    } else {
//                                        wordsCallback.done(null, e3);
//                                    }
//                                });
                            } else {
                                getCallback.done(null, e2);
                            }
                        });
                    }
                });
                return null;
            };

            GameUser gameUser = GameUser.getGameUser();
            final AppStatistics statistics = gameUser.getStatistics();

            final ParseQuery<DrawingWord> seenWordsQ = gameUser.getScore().getSeenWords().getQuery().setLimit(1000);

            ParseQuery<DrawingWord> bQ = ParseQuery.getQuery(DrawingWord.class).whereEqualTo("Mode", DrawingWord.EASY_WORD);
            ParseQuery<DrawingWord> bQ1 = ParseQuery.getQuery(DrawingWord.class).whereDoesNotMatchKeyInQuery("objectId", "objectId", seenWordsQ).whereEqualTo("Mode", DrawingWord.EASY_WORD);
            randomQ.run(bQ, bQ1, statistics.getEasyWordsCount());

            bQ = ParseQuery.getQuery(DrawingWord.class).whereEqualTo("Mode", DrawingWord.NORMAL_WORD);
            bQ1 = ParseQuery.getQuery(DrawingWord.class).whereDoesNotMatchKeyInQuery("objectId", "objectId", seenWordsQ).whereEqualTo("Mode", DrawingWord.NORMAL_WORD);
            randomQ.run(bQ, bQ1, statistics.getNormalWordsCount());

            bQ = ParseQuery.getQuery(DrawingWord.class).whereEqualTo("Mode", DrawingWord.HARD_WORD);
            bQ1 = ParseQuery.getQuery(DrawingWord.class).whereDoesNotMatchKeyInQuery("objectId", "objectId", seenWordsQ).whereEqualTo("Mode", DrawingWord.HARD_WORD);
            randomQ.run(bQ, bQ1, statistics.getHardWordsCount());
        } catch (Exception ex) {
            wordsCallback.done(null, new ParseException(ParseException.OTHER_CAUSE, ex.getMessage()));
        }
    }

    public void clearLastWords() {
        if (LastWords != null && LastWords.size() > 0) {
            try {
                ParseObject.unpinAll(PinTag, LastWords);
                LastWords = null;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
