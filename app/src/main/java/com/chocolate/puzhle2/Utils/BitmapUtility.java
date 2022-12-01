package com.chocolate.puzhle2.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by Mahdi7s on 3/22/2015.
 */
public class BitmapUtility
{
	public static void resizeInto(Context context, File file, int w, int h, ImageView imgv){
		Picasso.with(context).load(file).transform(new CropSquareTransformation(w, h))/*.networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)*/.config(Bitmap.Config.RGB_565).into(imgv);
	}

	public static void decodeBitmapAsync (Context context, File file, boolean isForSolve, Target target) {
		RequestCreator requestCreator = Picasso.with(context).load(file)/*.networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)*/.config(Bitmap.Config.RGB_565);
		decodeBitmapAsyncWithReq(context, isForSolve, target, requestCreator);
	}

	public static void decodeBitmapAsync (Context context, Uri filePath, boolean isForSolve, Target target)
	{
		RequestCreator requestCreator = Picasso.with(context).load(filePath)/*.networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)*/.config(Bitmap.Config.RGB_565);
		decodeBitmapAsyncWithReq(context, isForSolve, target, requestCreator);
	}

	private static void decodeBitmapAsyncWithReq(Context context, boolean isForSolve, Target target, RequestCreator requestCreator) {
		if (!isForSolve)
		{ // resize to max size that screen supports
			Point maxSize = getMaxBitmapSize(context, false);
			requestCreator = requestCreator.resize(maxSize.x, maxSize.y).centerInside();
		}
		requestCreator.into(target);
	}

	public static Bitmap decodeBitmapOnlyForSolve (String filePath)
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		if (Build.VERSION.SDK_INT >= 11)
		{
			options.inMutable = true;
		}
		return BitmapFactory.decodeFile(filePath, options);
	}

	// this methods only used for loading create puzzle images
	public static Bitmap decodeScaledBitmapFromSdCard (String filePath, Context context)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		if (Build.VERSION.SDK_INT >= 11)
		{
			options.inMutable = true;
		}
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		BitmapFactory.decodeFile(filePath, options);

		Point p = getMaxBitmapSize(context, options.outWidth > options.outHeight);

		int reqWidth = clamp(options.outWidth, 300, p.x),
				reqHeight = clamp(options.outHeight, 300, p.y);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		Log.d("decodeScaledBitmap", "inSampleSize: " + options.inSampleSize);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		Bitmap retval = BitmapFactory.decodeFile(filePath, options);
		if (retval.getWidth() > reqWidth || retval.getHeight() > reqHeight)
		{
			Point newSize = BitmapUtility.getNewAspectSize(new Point(retval.getWidth(), retval.getHeight()), new Point(reqWidth, reqHeight));
			Bitmap ret2 = Bitmap.createScaledBitmap(retval, newSize.x, newSize.y, true);
			retval.recycle();
			retval = ret2;
		}
		return retval;
	}

	public static Point getNewAspectSize (Point oldSize, Point newSize)
	{
		int width = oldSize.x;
		int height = oldSize.y;
		float ratioBitmap = (float) width / (float) height;
		float ratioMax = (float) newSize.x / (float) newSize.y;

		int finalWidth = newSize.x;
		int finalHeight = newSize.y;
		if (ratioMax > 1)
		{
			finalWidth = (int) ((float) newSize.y * ratioBitmap);
		} else
		{
			finalHeight = (int) ((float) newSize.x / ratioBitmap);
		}

		return new Point(finalWidth, finalHeight);
	}

//    public static Bitmap scaleToScreenSize(Context ctx, Bitmap bmp) {
//        int minWidth = 300, minHeight = 300;
//        Point p = getMaxBitmapSize(ctx, bmp.getWidth() > bmp.getHeight());
//        p = getNewAspectSize(new Point(bmp.getWidth(), bmp.getHeight()), new Point(clamp(bmp.getWidth(), minWidth, p.x), clamp(bmp.getHeight(), minHeight, p.y)));
//        return bmp;//Bitmap.createScaledBitmap(bmp, p.x, p.y, true);
//    }

	public static Point getMaxBitmapSize (Context mContext, boolean isImageLandscape)
	{ // use landscaped for landscaped images
		Point p = new Point();
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT > 12)
		{
			Point size = new Point();
			display.getSize(size);
			p.x = size.x;
			p.y = size.y;
		} else
		{
			p.x = display.getWidth();  // Deprecated
			p.y = display.getHeight();
		}

		if (!isImageLandscape)
		{
			return new Point(Math.min(p.x, 800 - QrCodeUtility.QrCodeSize), Math.min(p.y, 800));
		} else
		{
			return new Point(Math.min(p.y, 800 - QrCodeUtility.QrCodeSize), Math.min(p.x, 800));
		}
	}

	public static Bitmap decodeScaledBitmapFromResource (Resources resources, int resId, int reqWidth, int reqHeight)
	{
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		if (Build.VERSION.SDK_INT >= 11)
		{
			options.inMutable = true;
		}
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		BitmapFactory.decodeResource(resources, resId, options);

		reqWidth = clamp(options.outWidth, 300, reqWidth);
		reqHeight = clamp(options.outHeight, 300, reqHeight);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		Bitmap retval = BitmapFactory.decodeResource(resources, resId, options);
		if (retval.getWidth() > reqWidth || retval.getHeight() > reqHeight)
		{
			Bitmap ret2 = Bitmap.createScaledBitmap(retval, reqWidth, reqHeight, true);
			retval.recycle();
			retval = ret2;
		}
		return retval;
	}

	public static int calculateInSampleSize (BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth)
		{

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static int clamp (int val, int min, int max)
	{
		if (min > max)
		{
			int tMax = max;
			max = min;
			min = tMax;
		}
		return Math.max(min, Math.min(max, val));
	}

	public static void recycleImageViews (ImageView[] imgvs)
	{
		for (ImageView imgv : imgvs)
		{
			recycleImageView(imgv);
		}
	}

	public static void recycleImageView (ImageView imgv)
	{
		try
		{
			((BitmapDrawable) imgv.getDrawable()).getBitmap().recycle();
		} catch (Exception ex)
		{

		}
	}

	public static void recycleBitmap (Bitmap bmp)
	{
		if (bmp != null)
		{
			bmp.recycle();
			//bmp = null;
		}
	}

	public static void freeSomeMemory ()
	{
		System.gc();
		System.runFinalization();
		System.gc();
	}

	// http://www.codedisqus.com/0HHqXkjVVg/android-maximum-allowed-width-height-of-bitmap.html
	public static int getMaxTextureSize ()
	{
		// Safe minimum default size
		final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

		// Get EGL Display
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		// Initialise
		int[] version = new int[2];
		egl.eglInitialize(display, version);

		// Query total number of configurations
		int[] totalConfigurations = new int[1];
		egl.eglGetConfigs(display, null, 0, totalConfigurations);

		// Query actual list configurations
		EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
		egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

		int[] textureSize = new int[1];
		int maximumTextureSize = 0;

		// Iterate through all the configurations to located the maximum texture size
		for (int i = 0; i < totalConfigurations[0]; i++)
		{
			// Only need to check for width since opengl textures are always squared
			egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

			// Keep track of the maximum texture size
			if (maximumTextureSize < textureSize[0])
				maximumTextureSize = textureSize[0];
		}

		// Release
		egl.eglTerminate(display);

		// Return largest texture size found, or default
		return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
	}

	public static byte[] toArray(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		return byteArray;
	}

	public static Bitmap toBitmap(byte[] arr){
		return BitmapFactory.decodeByteArray(arr, 0, arr.length);
	}


	public interface BitmapLoad
	{
		void loaded (Bitmap bmp);
	}
}
