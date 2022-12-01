package com.chocolate.puzhle2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chocolate.puzhle2.CustomViews.CImageButton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ScreenSlidePageFragment extends Fragment
{
	private static String dataDirectoryAdrress;

	private File privateDirectory;
	private int number;

	// This method should be called first before any other call.
	public static void setArguments (String dataDirectoryAdrress)
	{
		ScreenSlidePageFragment.dataDirectoryAdrress = dataDirectoryAdrress;

	}

	public static ScreenSlidePageFragment create (int pageNumber)
	{
		ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();

		fragment.setNumber(pageNumber);
		return fragment;
	}

	public void setNumber (int number)
	{
		this.number = number;
		File f = new File(dataDirectoryAdrress);
		if (f.exists())
		{
			File[] files = f.listFiles();
			privateDirectory = files[number];
		}

	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page,
				container, false);

		CImageButton buttons[] = {(CImageButton) rootView.findViewById(R.id.button1),
				(CImageButton) rootView.findViewById(R.id.button2),
				(CImageButton) rootView.findViewById(R.id.button3),
				(CImageButton) rootView.findViewById(R.id.button4),
				(CImageButton) rootView.findViewById(R.id.button5),
				(CImageButton) rootView.findViewById(R.id.button6),
				(CImageButton) rootView.findViewById(R.id.button7),
				(CImageButton) rootView.findViewById(R.id.button8),
				(CImageButton) rootView.findViewById(R.id.button9)};

		TextView texts[] = {(TextView) rootView.findViewById(R.id.pager_text1),
				(TextView) rootView.findViewById(R.id.pager_text2),
				(TextView) rootView.findViewById(R.id.pager_text3),
				(TextView) rootView.findViewById(R.id.pager_text4),
				(TextView) rootView.findViewById(R.id.pager_text5),
				(TextView) rootView.findViewById(R.id.pager_text6),
				(TextView) rootView.findViewById(R.id.pager_text7),
				(TextView) rootView.findViewById(R.id.pager_text8),
				(TextView) rootView.findViewById(R.id.pager_text9)};

		ImageView imageView = (ImageView) rootView.findViewById(R.id.frame1);

		File[] files = privateDirectory.listFiles();

		for (int counter = 0; counter < 9; counter++)
		{
			int width = imageView.getWidth();
			int height = imageView.getHeight();
			buttons[counter].setMaxWidth(width);
			buttons[counter].setMaxHeight(height);
			Picasso.with(getActivity()).load(files[counter])
					.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
					.config(Bitmap.Config.RGB_565)
					.into((com.squareup.picasso.Target) buttons[counter]);
			texts[counter].setText(number + ", " + counter);

		}

		return rootView;
	}
}
