package sam.newdrawingapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.media.Image;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.widget.Button;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Mohammed Abdul Samad on 11/20/2017.
 */

public class DrawingView extends View {

    private Path drawingPath; //The Drawing Path
    private Paint drawingPaint; //The Paint for Drawing
    private Paint paintCanvas; //Canvas Paint
    private int colorP = 0x000000; //Default Color
    private Canvas canvas;
    private Bitmap bitmapCanvas;

    private Canvas oCanvas;
    private Bitmap oBitmap;

    private Canvas eCanvas;
    private Bitmap eBitmap;

    private ImageButton colorPickerButton;
    private boolean touchEnabled = true;

    private float brushSize;
    private float prevBrushSize;

    private Paint backgroundPaint;

    private boolean isErasing = false;
    private Paint eraserPaint; //Paint for erasing
    private Paint eraserPaint2;
    private Path eraserPath;

    private boolean isFill = false;

    private int bgColor = Color.WHITE; //Default

    private Paint drawingPaint2;
    private boolean penStyleChosen = false;

    private Path pPath;

    private int stateToSave;

    List<Integer> xx = new ArrayList<Integer>();
    List<Integer> yy = new ArrayList<Integer>();

    List<Bitmap> bitmapLayers = new ArrayList<Bitmap>();
    List<Canvas> canvasLayers = new ArrayList<Canvas>();
    private boolean isUndo = false;

    private Bitmap imageBitmap;
    private Canvas imageCanvas;
    private Drawable currentDrawableImage;

    private int undoCount = 4;
    private int saveCount = 0;
    private boolean finshDraw = false;

    private boolean isImageMove = false;
    private int imageX = 0;
    private int imageY = 0;
    private int imageBitmapX = 0;
    private int imageBitmapY = 0;
    private Matrix transform;

    private int startX = 0;
    private int startY = 0;
    boolean inRange = false;
    boolean changeSizeMode = false;
    private PointF midP = new PointF();
    private float oldDistance = 0;
    private boolean assignChangeToSize = false;

    private LruCache<String, Bitmap> memoryCache;
    private int saveAmount = 0;
    private boolean forward = true;


    public DrawingView(Context context, AttributeSet attr) {
        super(context, attr);
        setupDrawing();
        //this.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
    }

    public void setUpMemoryCache() {
        memoryCache = new LruCache<String, Bitmap>(1024) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(/*String key, Bitmap bitmap*/ Bitmap bitmap) {
        Cache.getInstance().getLru().put("Bitmap" + saveAmount, bitmap.copy(bitmap.getConfig(), true));
    }

    public Bitmap getBitmapFromMemory(String key) {
        return memoryCache.get(key);
    }

    public void setupDrawing() {
        //Initiate drawing area

        brushSize = getResources().getInteger(R.integer.medium_size);
        prevBrushSize = brushSize;

        colorPickerButton = (ImageButton) findViewById(R.id.colorButton);

        /*Initiate Path and Paint*/
        drawingPath = new Path();
        drawingPaint = new Paint();

        /*Set paint color to default*/
        drawingPaint.setColor(colorP);
        /*Initiate these to make drawing smoother*/
        drawingPaint.setAntiAlias(true);
        drawingPaint.setStrokeWidth(brushSize);
        drawingPaint.setStyle(Paint.Style.STROKE);
        drawingPaint.setStrokeCap(Paint.Cap.ROUND);
        /*****************************************/

        /*Paint Canvas*/
        paintCanvas = new Paint(Paint.DITHER_FLAG);

        /*Eraser Paint*/
        eraserPaint = new Paint();
        //eraserPaint.setAntiAlias(true);
        eraserPaint.setStrokeWidth(brushSize);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeCap(Paint.Cap.BUTT);
        eraserPaint.setColor(Color.TRANSPARENT);

        eraserPaint2 = new Paint();
        //eraserPaint.setAntiAlias(true);
        eraserPaint2.setStrokeWidth(brushSize);
        eraserPaint2.setStyle(Paint.Style.STROKE);
        eraserPaint2.setStrokeCap(Paint.Cap.BUTT);
        eraserPaint2.setColor(Color.TRANSPARENT);

        eraserPath = new Path();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWeight, int oldHeight) {
        super.onSizeChanged(width,height,oldWeight,oldHeight);
        bitmapCanvas = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmapCanvas);
        //canvas.drawARGB(1,255,255,255);
        canvas.drawColor(Color.TRANSPARENT);
        addBitmapToMemoryCache( bitmapCanvas );
    }



    /*public Bitmap drawableToBitmap (Drawable drawable, int width, int height) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                Toast tt = Toast.makeText(getContext(), "Here...", Toast.LENGTH_LONG);
                tt.show();
                //return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            //bitmap = Bitmap.createBitmap(canvas.getWidth() /*drawable.getIntrinsicWidth(), canvas.getHeight() /*drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createBitmap(width, height/2, Bitmap.Config.ARGB_8888);
        }

        //bitmap.setWidth(canvas.getWidth());
       /* Canvas canvasI = new Canvas(bitmap);
        drawable.setBounds(imageBitmapX, imageBitmapY, canvasI.getWidth(), canvasI.getHeight());
        drawable.draw(canvasI);
        return bitmap;
    }*/

    public void testSize() {
        Matrix rotation = new Matrix();
        rotation.postRotate(90f);
        imageBitmap = Bitmap.createBitmap(imageBitmap,0,0,imageBitmap.getWidth(),imageBitmap.getHeight(),rotation,true);
        //imageBitmap = drawableToBitmap(currentDrawableImage, canvas.getWidth(), canvas.getHeight()/2);
        invalidate();
    }


    public void rotateImage() {
        Matrix rotation = new Matrix();
        rotation.postRotate(90f);
        imageBitmap = Bitmap.createBitmap(imageBitmap,0,0,imageBitmap.getWidth(),imageBitmap.getHeight(),rotation,true);
        invalidate();
    }

    /*public void changeImageSize(int amount) {
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();

        Matrix scale = new Matrix();
        float scaledWidth = ((float)(width+amount)) / width;
        float scaledHeight = ((float)(height+amount)) / height;

        scale.postScale(scaledWidth,scaledHeight);
        imageBitmap = resizeBitmap(imageBitmap, scale);
        invalidate();
    }*/

    public void allowImageMove(boolean allow) {
        isImageMove = allow;
        if (isImageMove) {
            isFill = false;
            isUndo = false;
            isErasing = false;
        }
    }

    /*public void setImage(Drawable drw) {
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        imageBitmap = BitmapFactory.decodeFile(imagePath, options);*/

       /* currentDrawableImage = drw;
        imageBitmap = drawableToBitmap(drw, canvas.getWidth(), canvas.getHeight());
        invalidate();

    }*/

    public void mergeLayers() {
        eCanvas.drawBitmap(bitmapCanvas,0,0,paintCanvas);
        //oCanvas.drawBitmap(bitmapCanvas, 0, 0, paintCanvas);
    }

    public void setBackgroundCol(String color) {
        bgColor = Color.parseColor(color);

        //canvas.drawPath(eraserPath,eraserPaint);

        int red = Integer.valueOf(color.substring(1,3), 16);
        int green = Integer.valueOf(color.substring(3,5), 16);
        int blue = Integer.valueOf(color.substring(5,7), 16);


        oCanvas.drawRGB(red,green,blue);
        invalidate();
    }

    public void setPixelsM() {
        for (int i = 0; i < xx.size(); i++) {
            bitmapCanvas.setPixel(xx.get(i),yy.get(i),Color.BLUE);
        }
        canvas.drawBitmap(bitmapCanvas, 0, 0, paintCanvas);
    }

   /* public Bitmap translateBitmap(Bitmap bitmap, int x, int y) {
        int widthB = bitmap.getWidth();
        int heightB = bitmap.getHeight();

        Bitmap translatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, widthB, heightB);
        bitmap.recycle();
        return translatedBitmap;
    }

    public Bitmap resizeBitmap(Bitmap bitmap, Matrix matrix) {
        int widthB = bitmap.getWidth();
        int heightB = bitmap.getHeight();

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, widthB, heightB, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }*/


    @Override
    protected void onDraw(Canvas c) {
        //Draw on Canvas

        if (finshDraw) {

           if (saveAmount >= 3) {
               // Max Bitmap Save Amount
               Cache.getInstance().getLru().remove("Bitmap" + (saveAmount-3)); // Remove first Bitmap
            }

            saveAmount++;
            addBitmapToMemoryCache(bitmapCanvas);

            //currentB.recycle();

            finshDraw = false;

        }

        canvas.setBitmap(bitmapCanvas);
        c.drawBitmap(bitmapCanvas, 0, 0, paintCanvas);

        if (isErasing) {
            canvas.drawPath(eraserPath, eraserPaint);
            canvas.drawPath(eraserPath, eraserPaint2);
        } else {
            c.drawPath(drawingPath, drawingPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Detecting user input (Touch)
        float touchX = event.getX();
        float touchY = event.getY();


        if (!touchEnabled) {
            return false;
        }


        if (isFill) {
            // Using the fill tool
            finshDraw = true;
            invalidate();

            int pixels = bitmapCanvas.getPixel((int)touchX,(int)touchY);
            int touchcolor = Color.rgb(Color.red(pixels), Color.green(pixels), Color.blue(pixels));
            QueueLinearFloodFiller qFill = new QueueLinearFloodFiller(bitmapCanvas,touchcolor,colorP);
            qFill.floodFill((int)touchX,(int)touchY);


            invalidate();

            return true;
        }

        if (isErasing) {
            // Using eraser tool
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eraserPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    eraserPath.lineTo(touchX,touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    canvas.drawPath(eraserPath,eraserPaint);
                    canvas.drawPath(eraserPath,eraserPaint2);
                    eraserPath.reset();
                    finshDraw = true;
                    break;
                default:
                    return false;
            }
            invalidate();
            return true;
        }

        switch (event.getAction()) {
            // Using the brush tool
            case MotionEvent.ACTION_DOWN:
                drawingPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawingPath.lineTo(touchX,touchY);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(drawingPath, drawingPaint);
                drawingPath.reset();
                finshDraw = true;
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    /***************** Clear the Canvas ***********************************************************/
    /**********************************************************************************************/

    /*************** Determine Spacing between two touch points ***********************************/
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x*x + y*y);
    }

    /**************** Finds Mid Point *************************************************************/
    private void midPoint (PointF pointM, MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        pointM.set( x / 2 , y / 2);
    }

    /**************** Sets Paint Color *************************************************************/
    public void setColor(String colorNew) {
        //Set paint color
        invalidate();
        colorP = Color.parseColor(colorNew);
        drawingPaint.setColor(colorP);
    }

    /**************** Disables Touch **************************************************************/
    public void setTouch(boolean isTouch) {
        touchEnabled = isTouch;
    }

    /**************** Restarts Drawing ************************************************************/
    public void restartDraw() {
        /*canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        oCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        eraserPath.reset();
        eraserPaint.setColor(Color.WHITE);

        oCanvas.drawRGB(255,255,255); // Default White Background
        canvas.drawARGB(1,255,255,255);

        bitmapLayers.clear();
        canvasLayers.clear();

        invalidate();*/
    }

    /**************** Sets the BBrush Size to the amount specified ********************************/
    public void setBrushSize(float newBrushSize) {
        /*float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newBrushSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;*/
        brushSize = newBrushSize;
        drawingPaint.setStrokeWidth(brushSize);
        eraserPaint.setStrokeWidth(brushSize);
    }

    /**************** Enables Eraser Tool *********************************************************/
    public void setErase(boolean erase) {
        isErasing = erase;
        if (isErasing) {
            eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            //eraserPaint.setAlpha(1);

            //eraserPaint.setColor(bgColor);//

            eraserPaint2.setColor(Color.argb(1,255,255,255));
        } else {
            eraserPaint.setXfermode(null);
        }
    }

    /**************** Enables the Fill Tool *******************************************************/
    public void setFill(boolean fill) {
        isFill = fill;
    }


    public void setPenStyle(boolean style) {
        penStyleChosen = style;
    }

    /**************** Performs Undo Operation by Removing Bitmap **********************************/
    public void undo() {
        if (Cache.getInstance().getLru().size() > 1) {
            Cache.getInstance().getLru().remove("Bitmap" + saveAmount);
            saveAmount--;
            bitmapCanvas = (Bitmap) Cache.getInstance().getLru().get("Bitmap" + saveAmount);
            addBitmapToMemoryCache(bitmapCanvas);
            Toast tt = Toast.makeText(this.getContext(), "Undo Remaining: " + (Cache.getInstance().getLru().size()-1), Toast.LENGTH_SHORT);
            tt.show();
            invalidate();
        } else {
            Toast tt = Toast.makeText(this.getContext(), "Undo Unavailable", Toast.LENGTH_SHORT);
            tt.show();
        }
    }


    /************************ Saving State ********************************************************/
    /*  - Saves state of App to be restored
     **********************************************************************************************/
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState sState = new SavedState(superState);

        sState.stateToSave = this.stateToSave;

        return sState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable theState) {
        if(!(theState instanceof SavedState)) {
            super.onRestoreInstanceState((Bundle) theState);
            return;
        }

        SavedState sState = (SavedState) theState;
        super.onRestoreInstanceState((Bundle) sState.getSuperState());

        this.stateToSave = sState.stateToSave;
    }

    /******************** Save State Class ********************************************************/
    static class SavedState extends View.BaseSavedState {
        int stateToSave;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.stateToSave = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.stateToSave);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
    /**********************************************************************************************/
 }
