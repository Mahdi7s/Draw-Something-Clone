package com.chocolate.puzhle2.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by System 1 on 05/09/2015.
 */
@ParseClassName ("StoreDiscount")
public class StoreDiscount extends ParseObject
{
	public Date getFromDate ()
	{
		return getDate("FromDate");
	}

	public Date getToDate ()
	{
		return getDate("ToDate");
	}

	public String getTitle ()
	{
		return getString("Title");
	}

	public String getMessage ()
	{
		return getString("Message");
	}
}
