package com.chocolate.puzhle2.Utils;

import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * Created by mahdi on 9/26/15.
 */
public class BatchSaver {
    private static ArrayList<ParseObject> parseObjects;

    public static void addObject(ParseObject toSave) {
        if(parseObjects == null)
            parseObjects = new ArrayList<>();
        parseObjects.add(toSave);
    }

    public static void saveAll(SaveCallback callback) {
        ParseObject.saveAllInBackground(parseObjects, e -> {
            if(e == null){
                parseObjects.clear();
            }
            callback.done(e);
        });
    }
}
