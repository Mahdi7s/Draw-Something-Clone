package com.chocolate.puzhle2.repos;

import android.content.Context;
import android.text.TextUtils;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.FileUtility;
import com.chocolate.puzhle2.Utils.Mapper;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.models.AppStatistics;
import com.chocolate.puzhle2.models.DrawingWord;
import com.chocolate.puzhle2.models.GameUser;
import com.chocolate.puzhle2.models.UserPuzzle;
import com.chocolate.puzhle2.models.UserScore;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by choc01ate on 5/11/2015.
 */
public class UserPuzzleRepo extends BaseRepo<UserPuzzle> {
    public static boolean isDirty = true;
    private static DateTime lastRefreshTime = null;
    private GameUserRepo gameUserRepo;
    private LocalRepo localRepo;

    public UserPuzzleRepo(Context context, DialogManager dialogManager, ToastManager toastManager, GameUserRepo gameUserRepo, LocalRepo localRepo) {
        super(context, dialogManager, toastManager, UserPuzzle.class);
        this.gameUserRepo = gameUserRepo;
        this.localRepo = localRepo;
    }

    public void solvedPuzzle(final UserPuzzle userPuzzle, final int lampsUsed, final double solveSecs) {
        userPuzzle.setIsSolve();
        final UserScore score = GameUser.getCurrScore();

        if (userPuzzle.getSolves() < 3) {
            score.incrementWasInTopSolversCount();
        }
        score.incrementSolvedCount();
        score.getSolvedPuzzles().add(userPuzzle);
        userPuzzle.addSolverUser(score);
        userPuzzle.incrementSolves();
        userPuzzle.increment("SolveSeconds", solveSecs);
        userPuzzle.incrementLamps(lampsUsed);

        score.increment("SolveScore", userPuzzle.GetSolveScore());
        score.incrementCoinsCount(userPuzzle.getPuzzleCoinGift());

        ParseObject.saveAllInBackground(Arrays.asList(score, userPuzzle), (e) -> {
            if (e != null) {
                score.saveEventually();
                userPuzzle.saveEventually();
            }
        });
    }

    public void createPuzzle(final UserPuzzle puzzle, DrawingWord drawingWord, String puzzleWord, String message, SaveCallback callback) {
        final GameUser user = GameUser.getGameUser();
        final UserScore score = user.getScore();

        if (drawingWord != null) {
            puzzle.setPuzzleWord(drawingWord);
            puzzle.setKeyboard(getRandomKeyboard(drawingWord.getWord()));
        } else {
            puzzle.setPuzzleAnswer(puzzleWord);
            puzzle.setKeyboard(getRandomKeyboard(puzzleWord));
        }
        puzzle.setPuzzleMessage(message);
        puzzle.setCreatorId(user.getObjectId());
        puzzle.setCreatorScore(score);
        if (puzzle.usedColorPallet() || puzzle.usedImageImport() || puzzle.usedTextImport() || puzzle.usedUndo()) {
            score.incrementUsedProFeaturesCount();
        }

        if (puzzle.getSentToPublic()) {
            AppStatistics statistics = user.getStatistics();
            statistics.incrementRandomPuzzlesCount();

            statistics.saveEventually();
        }

        puzzle.saveInBackground((e) -> {
            if (e == null) {
                puzzle.put("randomId", puzzle.getObjectId());
                score.getCreatedPuzzles().add(puzzle);
                score.getSeenPuzzles().add(puzzle);

                ParseObject.saveAllInBackground(Arrays.asList(score, puzzle), (e2) -> {
                    if (e2 != null) {
                        score.saveEventually();
                        puzzle.saveEventually();
                    }
                });
            }
            callback.done(e);
        });
    }

    public void UploadRandomPuzzle(UserPuzzle userPuzzle, SaveCallback callback) { // 2 call
        final String puzzName = userPuzzle.getObjectId() + ".jpg";
        final String picPath = FileUtility.getMyPuzzleFolder(context) + puzzName;
        File pFile = new File(picPath);
        try {
            final ParseFile puzzleFile = new ParseFile(pFile, "image/jpeg"); //new ParseFile(puzzName, picBytes);
            puzzleFile.saveInBackground((ParseException e) -> {
                if (e == null) {
                    userPuzzle.setPuzzlePicture(puzzleFile);
                    userPuzzle.saveInBackground(callback);
                } else {
                    callback.done(e);
                }
            });
        } catch (Exception ex) {
            callback.done(new ParseException(ParseException.OTHER_CAUSE, ex.getMessage()));
        }
    }

    private static ArrayList<UserPuzzle> cachedRandoms = new ArrayList<>();

    public void getRandomPuzzle(final GetCallback<UserPuzzle> callback) { //  1 call
        if (cachedRandoms != null && cachedRandoms.size() > 0) {
            final UserPuzzle retval = cachedRandoms.remove(0);
            retval.incrementViews();
            retval.saveInBackground();
            callback.done(retval, null);
            return;
        }

//        GameUser gameUser = GameUser.getGameUser();
        final UserScore score = GameUser.getCurrScore();
//        ParseQuery<UserPuzzle> mySolvedPuzzlesQ = score.getSolvedPuzzles().getQuery()
//                .whereEqualTo("SentToPublic", true)
//                .whereEqualTo("IsActive", true)
//                .whereExists("PuzzlePicture")
//                .orderByDescending("createdAt")
//                .setLimit(1000); // the max limit available
        ParseQuery<UserPuzzle> seenRandsQ = score.getSeenPuzzles().getQuery() // below comments :(
                .whereEqualTo("SentToPublic", true)
                .whereEqualTo("IsActive", true)
                .whereExists("PuzzlePicture")
                .orderByDescending("createdAt")
                .setLimit(1000); // the max limit available

        ParseQuery<UserPuzzle> query = ParseQuery.getQuery(UserPuzzle.class)
                .whereEqualTo("SentToPublic", true)
                .whereEqualTo("IsActive", true)
                .whereExists("PuzzlePicture")
                .whereExists("SortOrder")
                .whereExists("randomId")
                .whereExists("CreatorId")
                .whereExists("CreatorScore")
                .whereDoesNotMatchKeyInQuery("objectId", "objectId", seenRandsQ) // don't show my last viewed puzzles
//                .whereDoesNotMatchKeyInQuery("randomId", "objectId", seenRandsQ) // don't show my last viewed puzzles
//                .whereDoesNotMatchKeyInQuery("objectId", "objectId", mySolvedPuzzlesQ) // don't show my solved puzzles
//                .whereNotEqualTo("CreatorId", gameUser.getObjectId()) // don't show my own puzzles
                .include("PuzzleWord")
                .include("CreatorScore");

        final int solvedFeatures = localRepo.getSolvedFeaturedPuzzles();
        if (solvedFeatures < 2 && !gameUserRepo.isUserLoggedIn()) {
            query = query.whereEqualTo("featured", true).orderByAscending("Views");
            localRepo.setSolvedFeaturedPuzzles(solvedFeatures + 1);
        } else {
            query = query.whereEqualTo("featured", false).orderByDescending("SortOrder");
        }

        query.setLimit(3).findInBackground((rnds, e) -> { // limit = 3
            if (rnds != null && rnds.size() > 0 && e == null) {
                for (final UserPuzzle rnd : rnds) {
                    score.getSeenPuzzles().add(rnd);
                    cachedRandoms.add(rnd);
                }

                score.saveInBackground((e2) -> {
                    if (e2 != null) {
                        score.saveEventually();
                    }
                });

                callback.done(cachedRandoms.remove(0), e);
            } else {
                callback.done(null, e);
            }
        });
    }

    public void getByIdForSolve(final String userId, final String puzzleId, final GetCallback<UserPuzzle> callback) {
        GameUser gameUser = GameUser.getGameUser();
        if (!gameUser.getObjectId().equals(userId)) {
            ParseQuery.getQuery(UserPuzzle.class)
                    .whereEqualTo("objectId", puzzleId)
                    .whereExists("CreatorId")
                    .whereExists("CreatorScore")
                    .whereExists("randomId")
                    .whereDoesNotMatchKeyInQuery("randomId", "objectId", gameUser.getScore().getSolvedPuzzles().getQuery())
                    .include("PuzzleWord")
                    .include("CreatorScore")
                    .getFirstInBackground((puzzle, e) -> {
                        if (e == null && puzzle != null) {
                            gameUser.getScore().getSeenPuzzles().add(puzzle);
                            gameUser.getScore().saveInBackground();

                            puzzle.incrementViews();
                            puzzle.saveInBackground();
                        }
                        callback.done(puzzle, e);
                    });
        } else { // you cannot solve your own puzzle
            callback.done(null, new ParseException(ParseException.OTHER_CAUSE, ""));
        }
    }

    private static ArrayList<UserPuzzle> myPuzzles = null;

    public void getMyPuzzles(FindCallback<UserPuzzle> callback) {
        if (isDirty || lastRefreshTime == null || myPuzzles == null || lastRefreshTime.plusMinutes(5).isBeforeNow()) {
            ParseQuery<UserPuzzle> query = GameUser.getCurrScore().getCreatedPuzzles().getQuery()/*.include("PuzzleWord")*/.orderByDescending("createdAt").setLimit(20);
            query.findInBackground((puzzles, e) -> {
                if (e == null) {
                    myPuzzles = new ArrayList<>();
                    if (puzzles.size() > 0) {
                        myPuzzles.addAll(puzzles);
                    }
                    isDirty = false;
                    lastRefreshTime = DateTime.now();
                }
                callback.done(puzzles, e);
            });
        } else {
            callback.done(myPuzzles, null);
        }
    }

    // TODO make this private
    public String getRandomKeyboard(String answer) {
        answer = answer.replace("ى", "ی");
        final int allLettersCount = 3 * 7;
        List<Character> letters = new ArrayList<>();
        for (char c : answer.toCharArray())
            if (Mapper.allPeLetters.contains(c + ""))
                letters.add(c);

        List<Character> frequencyLets = new ArrayList<>();
        for (int i = letters.size(); i < allLettersCount; i++) {
            // add random letter
            while (true) {
                char nLet = Mapper.allPeLetters.get((int) (Math.random() * allLettersCount)).charAt(0);
                int countOfLet = countOf(letters, nLet);
                if ((countOfLet < 2 && frequencyLets.size() <= 3) || countOfLet == 0) {
                    letters.add(nLet);

                    if (countOfLet == 1) {
                        frequencyLets.add(nLet);
                    }
                    break;
                }
            }
        }
        frequencyLets = null;
        Collections.shuffle(letters);

        return TextUtils.join("", letters);
    }

    private int countOf(List<Character> arr, char c) {
        int count = 0;
        for (char cc : arr)
            if (cc == c)
                ++count;
        return count;
    }
}
