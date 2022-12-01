package com.chocolate.puzhle2.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Created by mahdi on 6/24/15.
 */
public class ProgressImage extends Drawable
{

	private final Bitmap mBitmap;
	private final float progress;
	private final Paint mPaint;
	private final RectF mRectF;
	private final int mBitmapWidth;
	private final int mBitmapHeight;

	public ProgressImage (Bitmap bitmap, float progress)
	{
		this.mBitmap = bitmap;
		this.progress = progress;

		mRectF = new RectF();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mPaint.setShader(shader);

		mBitmapWidth = mBitmap.getWidth();
		mBitmapHeight = mBitmap.getHeight();
	}

	@Override
	protected void onBoundsChange (Rect bounds)
	{
		super.onBoundsChange(bounds);
		int pWidth = bounds.right - bounds.left;
		float newWidth = pWidth * progress;
		mRectF.set(bounds.left, bounds.top, newWidth, bounds.bottom);
	}

	@Override
	public void draw (Canvas canvas)
	{
		canvas.drawRect(mRectF, mPaint);
	}

	@Override
	public void setAlpha (int alpha)
	{
		if (mPaint.getAlpha() != alpha)
		{
			mPaint.setAlpha(alpha);
			invalidateSelf();
		}
	}

	@Override
	public void setColorFilter (ColorFilter colorFilter)
	{
		mPaint.setColorFilter(colorFilter);
	}

	@Override
	public int getOpacity ()
	{
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public int getIntrinsicWidth ()
	{
		return mBitmapWidth;
	}

	@Override
	public int getIntrinsicHeight ()
	{
		return mBitmapHeight;
	}

	public void setAntiAlias (boolean aa)
	{
		mPaint.setAntiAlias(aa);
		invalidateSelf();
	}

	@Override
	public void setFilterBitmap (boolean filter)
	{
		mPaint.setFilterBitmap(filter);
		invalidateSelf();
	}

	@Override
	public void setDither (boolean dither)
	{
		mPaint.setDither(dither);
		invalidateSelf();
	}

	public Bitmap getBitmap ()
	{
		return mBitmap;
	}

}
