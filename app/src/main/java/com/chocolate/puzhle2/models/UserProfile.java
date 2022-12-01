package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by mahdi on 11/30/15.
 */
@ParseClassName("DrawingWord")
public class UserProfile extends ParseObject {
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
}
