package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by choc01ate on 14/07/2015.
 */
@ParseClassName ("GalleryPuzzle")
public class GalleryPuzzle extends ParseObject
{
	public String getName ()
	{
		return getString("Name");
	}

	public int getPackageID ()
	{
		return getInt("PackageID");
	}

	public String getBaseAddress ()
	{
		return getString("BaseAddress");
	}
}
