package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by msdis on 22/08/2015.
 */
public class CImageButton extends ImageButton implements Target
{
	public CImageButton (Context context)
	{
		super(context);
	}

	public CImageButton (Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public CImageButton (Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public void onBitmapLoaded (Bitmap bitmap, Picasso.LoadedFrom from)
	{
		setImageBitmap(Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true));
	}

	@Override
	public void onBitmapFailed (Drawable errorDrawable)
	{
		if (errorDrawable != null)
		{
			Log.e("CImageButton", errorDrawable.toString());
		}
	}

	@Override
	public void onPrepareLoad (Drawable placeHolderDrawable)
	{
	}
}
