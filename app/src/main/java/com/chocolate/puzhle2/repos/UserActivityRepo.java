package com.chocolate.puzhle2.repos;

import android.content.Context;

import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.Utils.ToastManager;
import com.chocolate.puzhle2.models.UserActivity;

/**
 * Created by home pc on 28/08/2015.
 */
public class UserActivityRepo extends BaseRepo<UserActivity>
{
	public UserActivityRepo (Context context, DialogManager dialogManager, ToastManager toastManager)
	{
		super(context, dialogManager, toastManager, UserActivity.class);
	}

//	public void addUserActivity (final UserPuzzle solvedPuzzle, final UserPuzzle createdPuzzle, final UserPuzzle likedPuzzle, SaveCallback callback){
//		addUserActivity(solvedPuzzle, 0, 0, createdPuzzle, likedPuzzle, callback);
//	}

	// 1 call <3
//	public void addUserActivity (final UserPuzzle solvedPuzzle, final int lampsUsed, final float solveSecs, final UserPuzzle createdPuzzle, final UserPuzzle likedPuzzle, SaveCallback callback)
//	{
//		final UserActivity newActivity = new UserActivity();
//		final GameUser gameUser = GameUser.getGameUser();
//		newActivity.setUserId(gameUser.getObjectId());
//
//		final UserScore score = gameUser.getScore();
//		final UserStore store = gameUser.getStore();
//
//		if (solvedPuzzle != null)
//		{
//			final int puzzleSolveCount = solvedPuzzle.getSolves();
//			newActivity.put("SolvedStore", solvedPuzzle.getCreatorStore());
//			newActivity.put("SolvedScore", solvedPuzzle.getCreatorScore());
//
//			newActivity.setSolvedPuzzle(solvedPuzzle, puzzleSolveCount < 3);
//			score.incrementSolvedCount();
//
//			int coins = solvedPuzzle.getPuzzleCoinGift() - (solvedPuzzle.getSentToPublic() ? 1 : 0);
//			score.increment("SolveScore", (60 + coins * 10) + (solvedPuzzle.getSentToPublic() ? 5 : 0));
//			store.incrementCoinsCount(solvedPuzzle.getPuzzleCoinGift());
//
//			score.getSolvedPuzzles().add(solvedPuzzle);
//			solvedPuzzle.addSolverUser(score);
//			solvedPuzzle.incrementSolves();
//			solvedPuzzle.increment("SolveMins", solveSecs);
//			solvedPuzzle.incrementLamps(lampsUsed);
//
//			score.saveEventually();
//			store.saveEventually();
//			newActivity.saveEventually();
//
//			callback.done(null);
//		}
//		else if (likedPuzzle != null)
//		{
//			newActivity.setLikedPuzzle(likedPuzzle);
//			likedPuzzle.incrementLikes();
//			final UserScore pScore = likedPuzzle.getCreatorScore();
//			pScore.incrementLikesCount();
//
//			newActivity.saveEventually();
//			pScore.saveEventually();
//
//			callback.done(null);
//		}
//		else  if (createdPuzzle != null)
//		{
//			newActivity.setCreatedPuzzle(createdPuzzle);
//			score.getCreatedPuzzles().add(createdPuzzle);
//
//			final ArrayList<ParseObject> objectsToSave = new ArrayList<>();
//			objectsToSave.add(newActivity);
//			objectsToSave.add(score);
//
//			if (createdPuzzle.getSentToPublic()) {
//				AppStatistics statistics = gameUser.getStatistics();
//				statistics.incrementRandomPuzzlesCount();
//				objectsToSave.add(statistics);
//			}
//
//			ParseObject.saveAllInBackground(objectsToSave, (e) ->{
//				score.pinInBackground();
//				callback.done(e);
//			});
//		}
//	}
}
