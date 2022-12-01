package com.chocolate.puzhle2;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Mahdi7s on 12/22/2014.
 */
public class BrushDialog extends Dialog implements View.OnClickListener
{
	private ColorsView colorsView = null;
	private DrawingView drawingView = null;

	public BrushDialog (Context context, DrawingView dView)
	{
		super(context);
		drawingView = dView;
	}

	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brush_layout);

		colorsView = (ColorsView) findViewById(R.id.colors);

		// initialize buttons
		findViewById(R.id.ok).setOnClickListener(this);
	}

	@Override
	public void onClick (View v)
	{
		if (v.getId() == R.id.ok)
		{
			dismiss();
		}
	}
}
