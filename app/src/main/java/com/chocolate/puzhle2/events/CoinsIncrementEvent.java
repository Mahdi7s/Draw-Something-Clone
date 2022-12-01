package com.chocolate.puzhle2.events;

/**
 * Created by Mahdi7s on 11/08/2015.
 */
public class CoinsIncrementEvent
{
	private int coinsCount = 0;

	public CoinsIncrementEvent (int coinsCount)
	{
		this.coinsCount = coinsCount;
	}

	public int getCoinsCount ()
	{
		return coinsCount;
	}
}
