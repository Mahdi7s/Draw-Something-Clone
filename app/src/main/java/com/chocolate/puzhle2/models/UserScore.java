package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.List;

/**
 * Created by choc01ate on 5/10/2015.
 */
@ParseClassName ("UserScore")
public class UserScore extends ParseObject {
	public void init(String displayName) {
		put("SolvedCount", 0);
		put("CreatedCount", 0);
		put("LikesCount", 0);
		put("UsedProFeaturesCount", 0);
		put("WasInTopSolversCount", 0);
		put("TotalScore", 0);
		setDisplayName(displayName);

		// --------------Store-------------
		put("LampsCount", 6);
		put("CoinsCount", 500);

		put("Premium", false);

		put("ColorsUnlocked1", false);
		put("ColorsUnlocked2", false);
		put("ColorsUnlocked3", false);
		put("ColorsUnlocked4", false);

		put("TypeUnlocked", false);
		put("UndoUnlocked", false);
		put("ImageImportUnlocked", false);
	}

	// this should be called each time user go into achievements page
	public void refreshAchievements ()
	{
		final int createdPuzzles = getCreatedCount(),
				totalLikes = getLikesCount(),
				totalSolves =  getSolvedCount(),
				league = getLeague(),
				topSolversCount = getWasInTopSolversCount(),
				usedToolsCount = getUsedProFeaturesCount();

		put("CreationAchievedStep", createdPuzzles < UserStatistics.PuzzleCreationAchievement1 ? 0
				: createdPuzzles < UserStatistics.PuzzleCreationAchievement2 ? 1 :
				createdPuzzles < UserStatistics.PuzzleCreationAchievement3 ? 2 : 3);

		put("SolvingAchievedStep", totalSolves < UserStatistics.PuzzleSolvingAchievement1 ? 0
				: totalSolves < UserStatistics.PuzzleSolvingAchievement2 ? 1 :
				totalSolves < UserStatistics.PuzzleSolvingAchievement3 ? 2 : 3);

		put("LikesAchievedStep", totalLikes < UserStatistics.PuzzleLikeAchievement1 ? 0
				: totalLikes < UserStatistics.PuzzleLikeAchievement2 ? 1 :
				totalLikes < UserStatistics.PuzzleLikeAchievement3 ? 2 : 3);

		put("TopSolversAchieveStep", topSolversCount < UserStatistics.TopSolverAchievement1 ? 0
				: topSolversCount < UserStatistics.TopSolverAchievement2 ? 1 :
				topSolversCount < UserStatistics.TopSolverAchievement3 ? 2 : 3);

		put("UsedToolsAchieveStep", usedToolsCount < UserStatistics.UsedToolsAchievement1 ? 0
				: usedToolsCount < UserStatistics.UsedToolsAchievement2 ? 1 :
				usedToolsCount < UserStatistics.UsedToolsAchievement3 ? 2 : 3);

		put("LeagueAchieveStep", league < 3 ? 0 : league < 4 ? 1 : league < 5 ? 2 : 3);
	}

	//--------------------------------------------------------------------------

	public String getUserId ()
	{
		return getString("UserId");
	}

	public void setUserId (String userId)
	{
		put("UserId", userId);
	}

	public int getSolvedCount() {
		return getInt("SolvedCount");
	}
	public void incrementSolvedCount() {
		increment("SolvedCount");
	}

	public int getCreatedCount() {
		return getInt("CreatedCount");
	}
	public void incrementCreatedCount() {
		increment("CreatedCount");
	}

	public int getLikesCount() {
		return getInt("LikesCount");
	}
	public void incrementLikesCount() {
		increment("LikesCount");
	}

	public int getUsedProFeaturesCount() {
		return getInt("UsedProFeaturesCount");
	}
	public void incrementUsedProFeaturesCount() {
		increment("UsedProFeaturesCount");
	}

	public int getWasInTopSolversCount() {
		return getInt("WasInTopSolversCount");
	}
	public void incrementWasInTopSolversCount() {
		increment("WasInTopSolversCount");
	}

	public int getTotalScore() {
		return getInt("TotalScore");
	}

	public int getLeague() {
		return getInt("League");
	}

	//--------------------------------------------------------------------------

	public int getDisplayNameChangeCount(){
		return getInt("DnChangeCount");
	}

	public void incDisplayNameChangeCount(){
		increment("DnChangeCount");
	}

	public boolean canAddProfileReport() {
		final List<String> reporters = getList("ProfileReports");
		return reporters == null || !reporters.contains(GameUser.getGameUser().getObjectId());
	}

	public void addProfileReport() {
		addUnique("ProfileReports", GameUser.getGameUser().getObjectId());
	}
	public String getLink() {
		return getString("Link");
	}

	public void setLink(String link) {
		put("Link", link);
	}

	public String getSignature() {
		return getString("Signature");
	}

	public void setSignature(String signature) {
		put("Signature", signature);
	}

	public void incPuzzleReports() {
		increment("PuzzleReports");
	}

	public int getPuzzleReports(){
		return getInt("PuzzleReports");
	}

	public String getDisplayName() {
		return getString("DisplayName");
	}

	public void setDisplayName(String displayName) {
		put("DisplayName", displayName);
	}

	public ParseRelation<UserPuzzle> getSolvedPuzzles() {
		return getRelation("SolvedPuzzles");
	}

	public ParseRelation<UserPuzzle> getCreatedPuzzles() {
		return getRelation("CreatedPuzzles");
	}

	public ParseRelation<DrawingWord> getSeenWords() {
		return getRelation("SeenWords");
	}

	public ParseRelation<UserPuzzle> getSeenPuzzles() {
		return getRelation("SeenPuzzles");
	}

	//--------------------------Store---------------------
	public int getLampsCount ()
	{
		return getInt("LampsCount");
	}

	public void incrementLampsCount (int amount)
	{
		increment("LampsCount", amount);
	}

	public boolean decrementLampsCount (int amount)
	{
		boolean retval = getLampsCount() - amount >= 0;
		if(retval) {
			increment("LampsCount", -amount);
		}
		return retval;
	}

	public int getCoinsCount ()
	{
		return getInt("CoinsCount");
	}

	public void incrementCoinsCount (int amount)
	{
		increment("CoinsCount", amount);
	}

	public boolean decrementCoinsCount (int amount)
	{
		boolean retval = getCoinsCount() - amount >= 0;
		if (retval) {
			increment("CoinsCount", -amount);
		}
		return retval;
	}

	public boolean isPremium ()
	{
		return getBoolean("Premium");
	}

	public void setPremium ()
	{
		unlockColors(1);
		unlockColors(2);
		unlockColors(3);
		unlockColors(4);
		put("Premium", true);
	}

	public boolean hasColorsUnlocked (int colorPack)
	{
		return getBoolean("ColorsUnlocked" + colorPack);
	}

	public void unlockColors (int colorPack)
	{
		put("ColorsUnlocked" + colorPack, true);
	}

	public boolean hasTypeUnlocked ()
	{
		return getBoolean("TypeUnlocked");
	}

	public void unlockType ()
	{
		put("TypeUnlocked", true);
	}

	public boolean hasUndoUnlocked ()
	{
		return getBoolean("UndoUnlocked");
	}

	public void unlockUndo ()
	{
		put("UndoUnlocked", true);
	}

	public boolean hasImageImportUnlocked ()
	{
		return getBoolean("ImageImportUnlocked");
	}

	public void unlockImageImport ()
	{
		put("ImageImportUnlocked", true);
	}

	//--------------------------Achievements------------------------

	public int getPuzzleCreationAchievedStep ()
	{
		return getInt("CreationAchievedStep");
	}

	public int getPuzzleSolvingAchievedStep ()
	{
		return getInt("SolvingAchievedStep");
	}

	public int getUsedToolsAchievedStep(){
		return getInt("UsedToolsAchievedStep");
	}

	public int getPuzzleLikesAchievedStep ()
	{
		return getInt("LikesAchievedStep");
	}

	public int getTopSolversAchieveStep ()
	{
		return getInt("TopSolversAchieveStep");
	}

	public int getLeagueAchieveStep ()
	{
		return getInt("LeagueAchieveStep");
	}

	// --------------------------------------------------------

	public int getPuzzleCreationGotStep ()
	{
		return getInt("CreationGotStep");
	}

	public int getPuzzleSolvingGotStep ()
	{
		return getInt("SolvingGotStep");
	}

	public int getUsedToolsGotStep(){
		return getInt("UsedToolsGotStep");
	}

	public int getPuzzleLikesGotStep ()
	{
		return getInt("LikesGotStep");
	}

	public int getTopSolversGotStep ()
	{
		return getInt("TopSolversGotStep");
	}

	public int getLeagueGotStep ()
	{
		return getInt("LeagueGotStep");
	}



	public void incPuzzleCreationGotStep ()
	{
		increment("CreationGotStep");
	}

	public void incPuzzleSolvingGotStep ()
	{
		increment("SolvingGotStep");
	}

	public void incUsedToolsGotStep(){
		increment("UsedToolsGotStep");
	}

	public void incPuzzleLikesGotStep ()
	{
		increment("LikesGotStep");
	}

	public void incTopSolversGotStep ()
	{
		increment("TopSolversGotStep");
	}

	public void incLeagueGotStep ()
	{
		increment("LeagueGotStep");
	}
}
