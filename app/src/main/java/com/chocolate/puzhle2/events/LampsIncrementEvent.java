package com.chocolate.puzhle2.events;

/**
 * Created by choc01ate on 12/08/2015.
 */
public class LampsIncrementEvent
{
	private int lampsCount = 0;

	public LampsIncrementEvent (int lampsCount)
	{
		this.lampsCount = lampsCount;
	}

	public int getLampsCount ()
	{
		return lampsCount;
	}
}
