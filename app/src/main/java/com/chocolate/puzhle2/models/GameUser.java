package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseUser;

/**
 * Created by choc01ate on 5/10/2015.
 */
@ParseClassName("_User")
public class GameUser extends ParseUser {

    public static GameUser getGameUser() {
        return (GameUser) getCurrentUser();
    }

    public static UserScore getCurrScore() {
        return getGameUser().getScore();
    }

    public String getGPlusToken() {
        return getString("GPlusToken");
    }

    public void setGPlusToken(String token) {
        put("GPlusToken", token);
    }

    public void setStatistics(AppStatistics statistics) {
        put("Statistics", statistics);
    }

    public AppStatistics getStatistics() {
        try {
            AppStatistics statistics = (AppStatistics) getParseObject("Statistics");
            if (!statistics.has("DiscountOn") && getObjectId().equals(GameUser.getCurrentUser().getObjectId())) {
                statistics.fetchFromLocalDatastore();
            }
            return statistics;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserScore getScore() {
        try {
            UserScore score = (UserScore) getParseObject("Score");
            if (!score.has("Premium") && getObjectId().equals(GameUser.getCurrentUser().getObjectId())) {
                score.fetchFromLocalDatastore();
            }
            return score;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setScore(UserScore score) {
        put("Score", score);
    }
}
