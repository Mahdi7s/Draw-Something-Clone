package com.chocolate.puzhle2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by msdis on 19/08/2015.
 */

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
{
	private static int NUM_PAGES;

	public ScreenSlidePagerAdapter (FragmentManager supportFragmentManager)
	{
		super(supportFragmentManager);
	}

	@Override
	public Fragment getItem (int position)
	{
		ScreenSlidePageFragment sspf = new ScreenSlidePageFragment();
		sspf.setNumber(position);
		return sspf;
	}

	@Override
	public int getCount ()
	{
		return NUM_PAGES;
	}

	public static void setCount (int count)
	{
		NUM_PAGES = count;
	}
}
