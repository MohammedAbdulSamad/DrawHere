package sam.newdrawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

/**
 * Created by Mohammed Abdul Samad on 11/20/2017.
 */

public class PreviewStrokeView extends View {

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint canvasPaint;
    private Path previewPath;
    private Paint previewPaint;

    float brushSize;

    float heightM = 0;
    float widthM = 0;

    private int cHeight;
    private int cWidth;

    private SeekBar slider;

    public PreviewStrokeView(Context con) {
        super(con);
        setupPreviewStroke();
    }

    public PreviewStrokeView(Context con, AttributeSet attri) {
        super(con,attri);
        setupPreviewStroke();
    }

    public PreviewStrokeView(Context con, AttributeSet attri, int defStyle) {
        super(con,attri, defStyle);
        setupPreviewStroke();
    }

    public void setupPreviewStroke() {

        previewPaint = new Paint();
        previewPath = new Path();

        brushSize = 40;
        previewPaint.setStrokeWidth(brushSize);
        previewPaint.setColor(Color.BLACK);
        previewPaint.setAntiAlias(true);
        previewPaint.setStyle(Paint.Style.STROKE);
        previewPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWeight, int oldHeight) {
        super.onSizeChanged(width,height,oldWeight,oldHeight);
        bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        cWidth = width;
        cHeight = height;
        //canvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas c) {
        c.drawBitmap(bitmap,0,0,canvasPaint);
        c.drawPath(previewPath,previewPaint);
    }

    public void setPathPreview() {
        int middle = cHeight/2;
        int lineStart = cWidth/4;
        int lineEnd = lineStart*3;
        previewPath.moveTo(lineStart,middle);
        previewPath.lineTo(lineEnd,middle);
        invalidate();
    }

    public void setBrushColor(String color) {
        previewPaint.setColor(Color.parseColor(color));
        invalidate();
    }

    public void setBrushSize(float newBrushSize) {
        brushSize = newBrushSize;
        previewPaint.setStrokeWidth(brushSize);
        invalidate();
    }
}
