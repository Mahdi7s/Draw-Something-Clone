package com.chocolate.puzhle2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;

import de.greenrobot.event.EventBus;

/**
 * Created by Mahdi7s on 12/23/2014.
 */
public class BrushSizeView extends LinearLayout implements View.OnClickListener
{
	private ImageButton currSize = null;
	private float smallBrush = 10,
			mediumBrush = 20,
			largeBrush = 30;

	public BrushSizeView (Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.setClickable(true);
		this.setEnabled(true);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);

		View layout = LayoutInflater.from(context).inflate(R.layout.brush_size_layout, this, true);
		setupButtons((LinearLayout) findViewById(R.id.size_layout));
	}

	private void setupButtons (LinearLayout sizesRoot)
	{
		for (int i = 0; i < sizesRoot.getChildCount(); i++)
		{
			View v = sizesRoot.getChildAt(i);
			if (v instanceof ImageButton)
			{
				v.setOnClickListener(this);
			} else if (v instanceof LinearLayout)
			{
				setupButtons((LinearLayout) v);
			}
		}
	}

	@Override
	public void onClick (View view)
	{
		int id = view.getId();
		switch (id)
		{
			case R.id.small_brush:
				EventBus.getDefault().post(new EventMessage(EventMsgType.BrushSizeChange, smallBrush, R.id.small_brush));
				break;
			case R.id.medium_brush:
				EventBus.getDefault().post(new EventMessage(EventMsgType.BrushSizeChange, mediumBrush, R.id.medium_brush));
				break;
			case R.id.large_brush:
				EventBus.getDefault().post(new EventMessage(EventMsgType.BrushSizeChange, largeBrush, R.id.medium_brush));
				break;
		}
	}
}
