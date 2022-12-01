package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by System 1 on 06/09/2015.
 */
@ParseClassName("DrawingWord")
public class DrawingWord extends ParseObject
{
	public static final int EASY_WORD = 1;
	public static final int NORMAL_WORD = 2;
	public static final int HARD_WORD = 3;

	public String getWord ()
	{
		return getString("Word").replace("ى", "ی");
	}

	public void setWord (String word)
	{
		put("Word", word);
	}

	public int getMode ()
	{
		return getInt("Mode");
	}

	public void setMode (int mode)
	{
		put("Mode", mode);
	}

	public void incrementViews(){ increment("Views");}

	public String getDescription ()
	{
		return getString("Description");
	}

	public void setDescription (String description)
	{
		put("Description", description);
	}

	public ParseRelation<GameUser> getCreators ()
	{
		return getRelation("Creators");
	} // todo we don't need this
}
