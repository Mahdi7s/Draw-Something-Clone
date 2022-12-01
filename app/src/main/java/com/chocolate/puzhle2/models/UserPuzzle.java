package com.chocolate.puzhle2.models;

import com.chocolate.puzhle2.Utils.AnalyzeUtil;
import com.chocolate.puzhle2.Utils.Mapper;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * Created by choc01ate on 5/10/2015.
 */
@ParseClassName ("UserPuzzle")
public class UserPuzzle extends ParseObject
{
	public String getPuzzleMessage ()
	{
		return getString("PuzzleMessage");
	}

	public void setPuzzleMessage (String message)
	{
		put("PuzzleMessage", message);
	}

	public String getPuzzleAnswer ()
	{
		if(getPuzzleVersion() < 110) {
			return Mapper.mapToFarsi(getString("PuzzleAnswer"));
		}else{
			return getString("PuzzleAnswer").replace("ى", "ی");
		}
	}

	public void setPuzzleAnswer (String answer)
	{
		put("PuzzleAnswer", answer);
	}

	public void incAllDifficulties(int val){
		increment("AllDifficulties", val);
	}

	public float getAvgDifficulty(){
		return (float) getDouble("AvgDifficulty");
	}

	public DrawingWord getPuzzleWord(){
		return (DrawingWord) getParseObject("PuzzleWord");
	}

	public void setPuzzleWord(DrawingWord puzzleWord){
		put("PuzzleWord", puzzleWord);
	}

	public boolean isFreePuzzle() {
		return getPuzzleWord() == null;
	}

	public int getComplaints(){return getInt("Complaints");}
	public void incrementComplaints(){ increment("Complaints");}

	public boolean getIsActive(){
		return getBoolean("IssActive");
	} // set with reports & Complaints on server

	public int getLamps(){
		return getInt("Lamps");
	}
	public void incrementLamps(int amount){
		increment("Lamps", amount);
	}


	public int getSortOrder() {return getInt("SortOrder");} // Views+Lamps-Solves-Likes

	public String getKeyboard ()
	{
		if(getPuzzleVersion() < 110) {
			return Mapper.mapToFarsi(getString("Keyboard"));
		}else{
			return getString("Keyboard").replace("ى", "ی");
		}
	}

	public void setKeyboard (String keyboard)
	{
		put("PuzzleVersion", AnalyzeUtil.ANALYZE_VERSION);
		put("Keyboard", keyboard);
	}
	public int getPuzzleVersion(){return getInt("PuzzleVersion");}

	public String getIndependentAnswer(){
		if(isFreePuzzle()){
			return getPuzzleAnswer();
		}
		return getPuzzleWord().getWord();
	}

	public UserScore getCreatorScore ()
	{
		return (UserScore) getParseObject("CreatorScore");
	}

	public void setCreatorScore (UserScore score)
	{
		put("CreatorScore", score);
	}

	public String getCreatorId ()
	{
		return getString("CreatorId");
	}

	public void setCreatorId (String creatorId)
	{
		put("CreatorId", creatorId);
	}

	public void incrementSolves ()
	{
		increment("Solves");

//		refreshStars();
	}

	public int getSolves ()
	{
		return getInt("Solves");
	}

	public ParseFile getPuzzlePicture ()
	{
		return getParseFile("PuzzlePicture");
	}

	public void setPuzzlePicture (ParseFile puzzlePicture)
	{
		put("PuzzlePicture", puzzlePicture);
	}

	public int getViews(){ return getInt("Views"); }
	public void incrementViews(){
		increment("Views");
	}

	public int getLikes ()
	{
		return getInt("Likes");
	}

	public void incrementLikes ()
	{
		increment("Likes");

//		refreshStars();
	}

	private void refreshStars() {
		int likes = getLikes() + getSolves();
		int likeStars = likes < UserStatistics.PuzzleLikeCountToStar1 ? 0 :
				likes < UserStatistics.PuzzleLikeCountToStar2 ? 1 : likes < UserStatistics.PuzzleLikeCountToStar3 ? 2 : 3;
		put("LikeStars", likeStars);
	}

	public int getLikeStars ()
	{
		return getInt("LikeStars");
	}

	public boolean getSentToPublic ()
	{
		return getBoolean("SentToPublic");
	}

	public void setSentToPublic (boolean sentToPublic)
	{
		put("SentToPublic", sentToPublic);
		getPuzzleCoinGift();
	}

	public int getPuzzleCoinGift() {
		int coinGift = 0;
		if (isFreePuzzle()) {
			coinGift = UserStatistics.NormalWordCreatorCoinGift;
		} else {
			int difficulty = getPuzzleWord().getMode();
			coinGift = difficulty == 1 ? UserStatistics.EasyWordCreatorCoinGift :
					difficulty == 2 ? UserStatistics.NormalWordCreatorCoinGift : UserStatistics.HardWordCreatorCoinGift;
		}
		if(getSentToPublic()) ++ coinGift;

		coinGift *= 10;

		put("PuzzleCoinGift", coinGift);

		return coinGift;
	}

	public int GetSolveScore(){
		int coins = getPuzzleCoinGift() - (getSentToPublic() ? 10 : 0);
		return (60 + coins) + (getSentToPublic() ? 5 : 0);
	}

	public boolean usedColorPallet ()
	{
		return getBoolean("UsedColorPallet");
	}

	public void setUsedColorPallet ()
	{
		put("UsedColorPallet", true);
	}

	public boolean usedImageImport ()
	{
		return getBoolean("UsedImageImport");
	}

	public void setUsedImageImport ()
	{
		put("UsedImageImport", true);
	}

	public boolean usedTextImport ()
	{
		return getBoolean("UsedTextImport");
	}

	public void setUsedTextImport ()
	{
		put("UsedTextImport", true);
	}

	public boolean usedUndo ()
	{
		return getBoolean("UsedUndo");
	}

	public void setUsedUndo ()
	{
		put("UsedUndo", true);
	}

	public void setPromote() { put("IsPromote", true); }
	public DateTime getPromoteDate() {
		Date date = getDate("PromoteDate");
		return new DateTime((date == null ? getCreatedAt() : date).getTime());
	}

	public void setMode(String mode) {
		put("Mode", mode);
	}


	public ParseRelation<UserScore> getSolverScores ()
	{
		return getRelation("SolverScores");
	}

	public void addSolverUser (UserScore userScore)
	{
		ParseRelation<UserScore> solvers = getSolverScores();
		solvers.add(userScore);
	}

	public ParseRelation<UserScore> getLikerScores(){
		return getRelation("LikerScores");
	}

	public void  setIsSolve(){
		put("IsSolve", true);
	}

	public boolean canAddReport() {
		final List<String> reporters = getList("Reports");
		return reporters == null || !reporters.contains(GameUser.getGameUser().getObjectId());
	}

	public void addReport() {
		addUnique("Reports", GameUser.getGameUser().getObjectId());
	}
}
