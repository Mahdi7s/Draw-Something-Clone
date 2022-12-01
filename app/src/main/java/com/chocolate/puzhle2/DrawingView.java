package com.chocolate.puzhle2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.chocolate.puzhle2.Utils.BitmapUtility;
import com.chocolate.puzhle2.Utils.DialogManager;
import com.chocolate.puzhle2.events.EventMessage;
import com.chocolate.puzhle2.models.GameUser;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Mahdi7s on 12/16/2014.
 */
public class DrawingView extends View {
    private static final float TOUCH_TOLERANCE = 4;
    //canvas bitmap
    public Bitmap canvasBitmap;

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = Color.BLACK;
    //canvas
    private Canvas drawCanvas;
    private float brushSize = 13, lastBrushSize = 13;
    private boolean erase = false;
    private Bitmap imageToDraw;
    private MainActivity mainAct = null;

    private float anchorX = -1;
    private float anchorY = -1;

    // undo & redo
    private ArrayList<DrawSegment> paths = new ArrayList<DrawSegment>();
    private ArrayList<DrawSegment> undonePaths = new ArrayList<DrawSegment>();
    private String lastColor = "#FF000000";

    public DialogManager dialogManager = null;

    private boolean undoClicked = false, redoClicked = false;

    public DrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        EventBus.getDefault().register(this);
        brushSize = lastBrushSize = context.getResources().getDimension(R.dimen.brush_size2);
        setupDrawing();
    }

//    @Override
//    public Parcelable onSaveInstanceState() {
//
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("instanceState", super.onSaveInstanceState());
//        bundle.putParcelable("canvasBitmap", this.canvasBitmap);
//        bundle.putParcelable("drawPaint", this.drawPaint);
//        bundle.putParcelable("canvasBitmap", this.canvasBitmap);
//        // ... save everything
//        return bundle;
//    }
//
//    @Override
//    public void onRestoreInstanceState(Parcelable state) {
//
//        if (state instanceof Bundle) {
//            Bundle bundle = (Bundle) state;
//            this.stateToSave = bundle.getInt("stateToSave");
//            // ... load everything
//            state = bundle.getParcelable("instanceState");
//        }
//        super.onRestoreInstanceState(state);
//    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();

        if(canvasBitmap != null) {
            BitmapUtility.recycleBitmap(canvasBitmap);
        }
    }

    public void onEvent(EventMessage msg) {
        try {
            switch (msg.getMsgType()) {
                case BrushColorChange:
                    if ((int) msg.getExtraData() == R.id.pallets) {
                        lastColor = msg.getMsgData().toString();

                        setColor(lastColor);
                        setErase(false);
                        setBrushSize(getLastBrushSize());
                    }
                    break;
                case BrushSizeChange:
                    setColor(lastColor);
                    setBrushSize(((float) msg.getMsgData()));
                    setLastBrushSize(((float) msg.getMsgData()));
                    break;
                case EraserSelection:
                    setColor("#FFFFFFFF");
                    //setErase(false);
                    setBrushSize(((float) msg.getMsgData()));
                    //setLastBrushSize(((float) msg.getMsgData()));
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void undoDraw(boolean free) {
        if (paths.size() >= 1) {
            if (!undoClicked || GameUser.getGameUser().getScore().hasUndoUnlocked() || free) {
                undonePaths.add(paths.remove(paths.size() - 1));
                canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                drawCanvas = new Canvas(canvasBitmap);
                for (DrawSegment ds : paths) {
                    if (ds.Path != null) {
                        drawCanvas.drawPath(ds.Path, ds.DrawPaint);
                    } else if (ds.Bitmap != null) {
                        drawCanvas.drawBitmap(ds.Bitmap, ds.X, ds.Y, ds.DrawPaint);
                    } else if (ds.Text != null) {

                    }
                }
                invalidate();

                undoClicked = true;
                redoClicked = false;
            } else {
                dialogManager.showEnableUndoRedo();
            }
        }
    }

    public void redoDraw(boolean free) {
        if (undonePaths.size() >= 1) {
            if (!redoClicked || GameUser.getGameUser().getScore().hasUndoUnlocked() || free) {
                paths.add(undonePaths.remove(undonePaths.size() - 1));
                canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                drawCanvas = new Canvas(canvasBitmap);
                for (DrawSegment ds : paths) {
                    if (ds.Path != null) {
                        drawCanvas.drawPath(ds.Path, ds.DrawPaint);
                    } else if (ds.Bitmap != null) {
                        drawCanvas.drawBitmap(ds.Bitmap, ds.X, ds.Y, ds.DrawPaint);
                    } else if (ds.Text != null) {

                    }
                }
                invalidate();

                redoClicked = true;
                undoClicked = false;
            } else {
                dialogManager.showEnableUndoRedo();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        if (paths != null && paths.size() > 0) {
            DrawSegment ds = paths.get(paths.size() - 1);
            if (ds.Path != null) {
                canvas.drawPath(ds.Path, ds.DrawPaint);
            } else if (ds.Bitmap != null) {
                canvas.drawBitmap(ds.Bitmap, ds.X, ds.Y, ds.DrawPaint);
            } else if (ds.Text != null) {

            }
        }
    }

    public void setInsertionAnchors(float anchX, float anchY) {
        anchorX = anchX;
        anchorY = anchY;
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        paths.clear();
        undonePaths.clear();
        invalidate();
    }

    private void setupDrawing() {
        mainAct = MainActivity.instance;

        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, getResources().getDisplayMetrics());
        lastBrushSize = brushSize;

        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setBrushSize(float newSize) {
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        // TODO: here for extending image size for signature...
        drawCanvas = new Canvas(canvasBitmap);
    }

    private float lX = 0, lY = 0;
    private float lXS = 0, lYS = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        int action = event.getAction();

        if (imageToDraw != null) {
            if (action == MotionEvent.ACTION_UP) {
                drawPath.reset();
                if (anchorX != -1) {
                    touchX = anchorX * canvasBitmap.getWidth() - anchorX * imageToDraw.getWidth(); // ****** canvasBitmap is null
                    touchY = anchorY * canvasBitmap.getHeight() - anchorY * imageToDraw.getHeight();

                    anchorX = anchorY = -1;
                } else {
                    touchX = touchX - imageToDraw.getWidth() / 2;
                    touchY = touchY - imageToDraw.getHeight() / 2;
                }
                DrawSegment ds = new DrawSegment();
                ds.Bitmap = imageToDraw;
                ds.DrawPaint = new Paint();
                ds.X = touchX;
                ds.Y = touchY;
                undoClicked = redoClicked = false;
                paths.add(ds);// Todo remove the last 6 and draw that to canvas
                drawCanvas.drawBitmap(imageToDraw, ds.X, ds.Y, ds.DrawPaint);
                imageToDraw = null;
                //mainAct.goToPuzzleNormalState();
                CreatePuzzleActivity.instance.goToNormalMode();
                invalidate();
            }
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                drawPath.reset();
                undonePaths.clear();
                drawPath.moveTo(touchX, touchY);
                lX = touchX;
                lY = touchY;
                lXS = touchX;
                lYS = touchY;

                DrawSegment ds = new DrawSegment();
                ds.Path = drawPath;
                ds.DrawPaint = new Paint(drawPaint);
                paths.add(ds);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(touchX - lX);
                float dy = Math.abs(touchY - lY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    drawPath.quadTo(lX, lY, (touchX + lX) / 2, (touchY + lY) / 2);
                    lX = touchX;
                    lY = touchY;
                }
                break;
            case MotionEvent.ACTION_UP:
                float dx1 = Math.abs(touchX - lXS);
                float dy1 = Math.abs(touchY - lYS);
                if (dx1 < TOUCH_TOLERANCE && dy1 < TOUCH_TOLERANCE) {
                    drawPath.addCircle(touchX, touchY, brushSize / 2, Path.Direction.CW);
                    Paint pp = new Paint(drawPaint);
                    pp.setStyle(Paint.Style.FILL);
                    drawCanvas.drawPath(drawPath, pp);
                    paths.get(paths.size() - 1).DrawPaint = pp;
                } else {
                    drawCanvas.drawPath(drawPath, drawPaint);
                }
                undoClicked = redoClicked = false;
                drawPath = new Path();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public boolean isEmpty() {
        return paths.size() <= 0;
    }

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void drawImage(Bitmap img, boolean isPicture) {
        imageToDraw = img;
        CreatePuzzleActivity.instance.goToInsertionMode(isPicture);
        //mainAct.goToPuzzleInsertState();
    }
}
