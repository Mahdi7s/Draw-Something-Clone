package com.chocolate.puzhle2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.edmodo.cropper.CropImageView;

/**
 * Created by Ariana Gostar on 12/18/2014.
 */
public class CropDialog extends Dialog implements View.OnClickListener {
    // Static final constants
    private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
    private static final int ROTATE_NINETY_DEGREES = 90;
    private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
    private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
    private static final int ON_TOUCH = 1;
    Bitmap croppedImage;
    private Context parent = null;
    // Instance variables
    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;
    private Bitmap imageToCrop = null;
    private DrawingView drawingView = null;
    private CreatePuzzleActivity createPuzzleActivity;

    public CropDialog(Context context, Bitmap bmp, DrawingView drawView, CreatePuzzleActivity createPuzzleActivity) {

        super(context/*, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen*/);
        parent = context;
        imageToCrop = bmp;
        drawingView = drawView;
        this.createPuzzleActivity = createPuzzleActivity;
    }

    // Restores the state upon rotating the screen/restarting the activity
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
        mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_layout);


        // Initialize components of the app
        final CropImageView cropImageView = (CropImageView) findViewById(R.id.CropImageView);
        if (imageToCrop != null) {
            cropImageView.setImageBitmap(imageToCrop);
        }

        //Sets the rotate btn_solve
        final ImageButton rotateButton = (ImageButton) findViewById(R.id.Button_rotate);
        rotateButton.setOnClickListener(v -> cropImageView.rotateImage(ROTATE_NINETY_DEGREES));

        // Sets initial aspect ratio to 10/10, for demonstration purposes
        cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

        final ImageButton cropButton = (ImageButton) findViewById(R.id.Button_crop);
        cropButton.setOnClickListener(v -> {
            croppedImage = cropImageView.getCroppedImage();
            drawingView.drawImage(croppedImage, true);
            hide();

            createPuzzleActivity.UsedImageImport = true;
        });
    }

    @Override
    public void onClick(View v) {

    }
}
