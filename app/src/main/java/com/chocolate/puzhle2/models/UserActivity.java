package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by home pc on 28/08/2015.
 */
@ParseClassName ("UserActivity")
public class UserActivity extends ParseObject
{
	public GameUser getUser ()
	{
		return (GameUser) getParseObject("User");
	}

	public void setUserId (String userId)
	{
		put("UserId", userId);
	}

	public void setSolvedPuzzle (UserPuzzle solvedPuzzle, boolean isInTopSolvers)
	{
		put("SolvedPuzzle", solvedPuzzle);
		if(isInTopSolvers) {
			put("IsInTopSolvers", isInTopSolvers);
			UserScore userScore = GameUser.getGameUser().getScore();
			userScore.incrementWasInTopSolversCount();
			userScore.pinInBackground();
		}
	}

	public UserPuzzle getSolvedPuzzle ()
	{
		return (UserPuzzle) getParseObject("SolvedPuzzle");
	}

	public boolean getIsInTopSolvers ()
	{
		return getBoolean("IsInTopSolvers");
	}

	public UserPuzzle getCreatedPuzzle ()
	{
		return (UserPuzzle) getParseObject("CreatedPuzzle");
	}

	public void setCreatedPuzzle (UserPuzzle createdPuzzle)
	{
		put("CreatedPuzzle", createdPuzzle);
	}

	public UserPuzzle getLikedPuzzle ()
	{
		return (UserPuzzle) getParseObject("LikedPuzzle");
	}

	public void setLikedPuzzle (UserPuzzle likedPuzzle)
	{
		put("LikedPuzzle", likedPuzzle);
	}
}
