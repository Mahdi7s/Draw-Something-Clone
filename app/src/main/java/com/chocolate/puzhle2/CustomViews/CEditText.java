package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.chocolate.puzhle2.R;

/**
 * Created by choc01ate on 5/11/2015.
 */
public class CEditText extends EditText
{
	private final String fontsFolder = "fonts/";
	private final String defaultFont = "BYekan.ttf";

	public CEditText (Context context, AttributeSet attrs)
	{
		super(context, attrs);

		boolean isTypefaceSet = false;
		TypedArray tAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CEditText, 0, 0);
		if (attrs != null)
		{
			try
			{
				String typefacePath = tAttrs.getString(R.styleable.CEditText_typeface);
				if (typefacePath != null)
				{
					Typeface tf = Typeface.createFromAsset(context.getAssets(), fontsFolder + typefacePath);
					setTypeface(tf, getTypeface().getStyle());
					isTypefaceSet = true;
				}
			} finally
			{
				tAttrs.recycle();
			}
		}
		if (!isTypefaceSet)
		{
			Typeface tf = Typeface.createFromAsset(context.getAssets(), fontsFolder + defaultFont);
			setTypeface(tf);
		}
	}
}
