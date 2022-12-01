package com.chocolate.puzhle2.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chocolate.puzhle2.R;
import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;

import de.greenrobot.event.EventBus;

/**
 * Created by mahdi on 6/7/15.
 */
public class BrushSizeSelector extends LinearLayout implements View.OnClickListener
{
	public float SIZE1 = 8;
	public float SIZE2 = 13;
	public float SIZE3 = 18;
	public float SIZE4 = 28;

	private float selectedSize = 13;
	public EventMsgType messageType;

	public BrushSizeSelector (Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER_HORIZONTAL);
		setBackgroundResource(R.drawable.brush_size_panel);

		SIZE1 = context.getResources().getDimension(R.dimen.brush_size1);
		SIZE2 = context.getResources().getDimension(R.dimen.brush_size2);
		SIZE3 = context.getResources().getDimension(R.dimen.brush_size3);
		SIZE4 = context.getResources().getDimension(R.dimen.brush_size4);

		selectedSize = SIZE2;

		addSize(R.drawable.brush_size_4, R.drawable.brush_size_4_glow, SIZE4);
		addSize(R.drawable.brush_size_3, R.drawable.brush_size_3_glow, SIZE3);
		addSize(R.drawable.brush_size_2, R.drawable.brush_size_2_glow, SIZE2);
		addSize(R.drawable.brush_size_1, R.drawable.brush_size_1_glow, SIZE1);

		setVisibility(INVISIBLE);
	}

	private void addSize (int iconResId, int glowResId, float size)
	{
		Context context = getContext();

		final ImageView imgIcon = new ImageView(context);
		imgIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imgIcon.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		imgIcon.setImageResource(iconResId);
		imgIcon.setTag(R.id.size_icon, iconResId);
		imgIcon.setTag(R.id.size_icon_glow, glowResId);
		imgIcon.setTag(R.id.size_icon_sel, false);
		imgIcon.setTag(R.id.size_icon_size, size);
		addView(imgIcon);

		imgIcon.setOnClickListener(this);

		if(size == SIZE2){
			onClick(imgIcon);
		}
	}

	@Override
	public void onClick (View view)
	{
		final ImageView img = (ImageView) view;
		int iconId = (int) img.getTag(R.id.size_icon);
		int glowId = (int) img.getTag(R.id.size_icon_glow);
		boolean selected = (boolean) img.getTag(R.id.size_icon_sel);
		if (!selected)
		{
			for (int i = 0; i < getChildCount(); i++)
			{
				final ImageView imgT = (ImageView) getChildAt(i);
				if (imgT != img)
				{
					int icnRes = (int) imgT.getTag(R.id.size_icon);
					imgT.setImageResource(icnRes);
					imgT.setTag(R.id.size_icon_sel, false);
				}
			}

			img.setImageResource(glowId);
			img.setTag(R.id.size_icon_sel, true);
			selectedSize = (float) img.getTag(R.id.size_icon_size);
			EventBus.getDefault().post(new EventMessage(messageType, img.getTag(R.id.size_icon_size), R.id.size_icon_size));
		}
		hide();
	}

	public void show ()
	{
		setVisibility(VISIBLE);
		EventBus.getDefault().post(new EventMessage(messageType, selectedSize, R.id.size_icon_size));
	}

	public void hide ()
	{
		setVisibility(INVISIBLE);
	}
}
