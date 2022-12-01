package com.chocolate.puzhle2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.chocolate.puzhle2.CustomViews.ColorPallets;
import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.events.EventMsgType;

import de.greenrobot.event.EventBus;

/**
 * Created by Mahdi7s on 12/21/2014.
 */
public class TextDialog extends Dialog
{
	private Bitmap bmp = null;
	private Canvas canvas = null;
	private Paint paint = null;
	private DrawingView drawingView = null;

	private ImageView imgv = null;
	private EditText txt = null;

	private int canvasRot = 0;

	private CreatePuzzleActivity createPuzzleActivity = null;

	public TextDialog (Context context, DrawingView dView, CreatePuzzleActivity createPuzzleActivity)
	{
		super(context);
		drawingView = dView;

		this.createPuzzleActivity = createPuzzleActivity;

		EventBus eventBus = EventBus.getDefault();
		if(!eventBus.isRegistered(this)) {
			eventBus.register(this);
		}
	}

	public void onEvent (EventMessage event) {
		if(event.getMsgType() == EventMsgType.BrushColorChange && (int)event.getExtraData() == R.id.textColorPallet) {
			paint.setColor(Color.parseColor(event.getMsgData().toString()));
			onSomethingChanged(imgv, txt);
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.text_layout);

		final float scale = getContext().getResources().getDisplayMetrics().scaledDensity;

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Yekan.ttf"));
		paint.setColor(Color.BLACK);
		paint.setShadowLayer(1, 0, 1, Color.BLACK);

		imgv = (ImageView) findViewById(R.id.imgText);
		txt = (EditText) findViewById(R.id.txtImg);
		final ColorPallets colorPallets = (ColorPallets) findViewById(R.id.textColorPallet);
		colorPallets.hide();

		txt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				onSomethingChanged(imgv, txt);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		final int minRotStep = 5;
		final SeekBar imgRotation = (SeekBar) findViewById(R.id.imgRotation);
		imgRotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				progress = ((int) Math.round(progress / minRotStep)) * minRotStep;
				seekBar.setProgress(progress);
				canvasRot = progress;
				onSomethingChanged(imgv, txt);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		final int minSizeStep = 2;
		final SeekBar fontSizeSeek = (SeekBar) findViewById(R.id.fontSizeSeek);
		fontSizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser)
			{
				progress = ((int)Math.round(progress/minRotStep))*minRotStep;
				seekBar.setProgress(progress);
				paint.setTextSize(progress * scale);
				onSomethingChanged(imgv, txt);
			}

			@Override
			public void onStartTrackingTouch (SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch (SeekBar seekBar)
			{

			}
		});
		fontSizeSeek.setProgress(14);

		ImageButton okBtn = (ImageButton) findViewById(R.id.txtDlgOkBtn);
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawingView.drawImage(bmp, false);
				bmp = null;
				dismiss();

				createPuzzleActivity.UsedTextImport = true;
			}
		});

		findViewById(R.id.btnTextColor).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				colorPallets.show();
			}
		});
	}



	private void onSomethingChanged (ImageView imgv, EditText txt)
	{
		final String str = txt.getText().toString();

		if (bmp != null) {
			bmp.recycle();
		}
		float margin = 2.5f;
		Rect textBounds = new Rect();
		paint.getTextBounds(str, 0, str.length(), textBounds);
		float px = textBounds.exactCenterX() + margin,
				py = textBounds.exactCenterY() + margin;
		int tWidth = (int) (textBounds.width() + margin*2), tHeight = (int) (textBounds.height() + margin*2);

		Matrix matrix = new Matrix();
		matrix.postRotate(canvasRot, px, py);
		Bitmap bmpTmp = Bitmap.createBitmap(tWidth <= 0 ? 5 : tWidth, tHeight <= 0 ? 5 : tHeight, Bitmap.Config.ARGB_8888);
		bmp = Bitmap.createBitmap(bmpTmp, 0, 0, bmpTmp.getWidth(), bmpTmp.getHeight(), matrix, true);
		bmpTmp.recycle();

		canvas = new Canvas(bmp);
		canvas.save();
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
		float cx = canvas.getClipBounds().exactCenterX(),
				cy = canvas.getClipBounds().exactCenterY();
		canvas.rotate(canvasRot, cx, cy);
		canvas.drawText(str, cx - px, cy - py, paint);
		canvas.restore();

		imgv.setImageBitmap(bmp);
	}
}
