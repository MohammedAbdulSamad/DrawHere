package sam.newdrawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Mohammed Abdul Samad on 11/20/2017.
 */

public class PictureView extends View {
    private Canvas canvas;
    private Bitmap bitmap;
    private Drawable currentDrawableImage;

    private float imageStartX = 0;
    private float imageStartY = 0;

    private boolean isImageMove = false;
    private boolean changeSizeMode = false;
    private float oldDistance;
    private Matrix transform;
    private float startX = 0;
    private float startY = 0;
    private PointF midP = new PointF();
    private boolean first = false;

    private boolean bounce = false;
    private boolean isMovingN = false;
    private boolean isRotating = false;
    private boolean finishMove = false;
    private float scaling = 0;

    public PictureView(Context context, AttributeSet attr) {
        super(context, attr);
        setUpPicture();
    }

    public void setUpPicture() {

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWeight, int oldHeight) {
        super.onSizeChanged(width, height, oldWeight, oldHeight);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        imageStartX = (canvas.getWidth()/2) - (canvas.getWidth()/4); // Place image at specific point
        imageStartY = (canvas.getHeight()/2) - (canvas.getHeight()/4);
    }

    @Override
    protected void onDraw(Canvas c) {
       if (isMovingN || isRotating) {
           c.drawBitmap(bitmap, transform, null);
       } else {
           c.drawBitmap(bitmap, imageStartX, imageStartY, null);
       }
    }

    private boolean touchStart = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Detecting user input (Touch)
        float touchX = event.getX();
        float touchY = event.getY();

        if (isImageMove) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    /*finishMove = false;
                    first = true;
                    isMovingN = true;
                    changeSizeMode = false;*/
                    isMovingN = true;
                    startX =  event.getX() - imageStartX; // Get Starting Coordinates
                    startY =  event.getY() - imageStartY;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    finishMove = false;
                    oldDistance = spacing(event);
                    if (oldDistance > 10f) {
                        changeSizeMode = true;
                        midPoint(midP, event);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    first = false;
                    transform = new Matrix();
                    if (changeSizeMode) {
                        float newDistance = spacing(event);
                        if (newDistance > 10f) {
                            scaling = (newDistance / oldDistance);
                            transform.postScale(scaling,scaling,midP.x,midP.y);
                        }
                    } if (isMovingN) {
                        imageStartX =  event.getX() - startX;
                        imageStartY =  event.getY() - startY;
                        transform.postTranslate(imageStartX,imageStartY);
                    }
                    //transform.postScale(scaling,scaling,midP.x,midP.y);
                   // transform.postTranslate(imageStartX,imageStartY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    //bitmap = modifyBitmap(bitmap, transform);
                    if (changeSizeMode) {
                        bitmap = modifyBitmap(bitmap, transform); // Alters Bitmap to change Scaling
                        changeSizeMode = false;
                    }
                    //isMovingN = false;
                    invalidate();
                    isMovingN = false;
                    break;
                default:
                    return false;
            }
        }
        //invalidate();
        return true;
    }

    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    /**************************** Converts Drawable to Bitmap *************************************/
    public Bitmap drawableToBitmap (Drawable drawable, int width, int height) {
        Bitmap bitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        options.inSampleSize = 8;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(width, height/2, Bitmap.Config.ARGB_8888);
        }

        //bitmap.setWidth(canvas.getWidth());
        Canvas canvasI = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvasI.getWidth(), canvasI.getHeight());
        drawable.draw(canvasI);
        return bitmap;
    }

    /*************************** Alters the Properties of the Bitmap ******************************/
    public Bitmap modifyBitmap(Bitmap bitmapM, Matrix matrixM) {
        int widthB = bitmapM.getWidth();
        int heightB = bitmapM.getHeight();

        Bitmap modifiedBitmap = Bitmap.createBitmap(bitmapM, 0, 0, widthB, heightB, matrixM, true);
        bitmapM.recycle();
        return modifiedBitmap;
    }

    /********************** Sets the Image to be Converted to Bitmap ******************************/
    public void setImage(Drawable drw) {
        //currentDrawableImage = drw;
        bitmap.recycle();
        bitmap = drawableToBitmap(drw, canvas.getWidth()/2, canvas.getHeight()/2);
        invalidate();

    }

    /********************* Removes Image **********************************************************/
    public void removeImage() {
        bitmap.recycle();
        transform = null;
        isImageMove = false;
        isMovingN = false;

        onSizeChanged(canvas.getWidth(),canvas.getHeight(),canvas.getWidth(),canvas.getHeight());
        invalidate();
    }

    /********************* Rotates Image 90 degrees ***********************************************/
    public void rotateImage() {
        transform = new Matrix();
        transform.postRotate(90f);
        bitmap = modifyBitmap(bitmap, transform);
        invalidate();
    }

    /**************************** Allows Image to be Modified *************************************/
    public void allowImageMove(boolean allow) {
        isImageMove = allow;
    }
    /**********************************************************************************************/

    /*************** Determine Spacing between two touch points ***********************************/
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x*x + y*y);
    }
    /**********************************************************************************************/

    /**************** Finds Mid Point *************************************************************/
    private void midPoint (PointF pointM, MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        pointM.set( x / 2 , y / 2);
    }
}
