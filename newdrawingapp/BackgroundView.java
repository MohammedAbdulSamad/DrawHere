package sam.newdrawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Mohammed Abdul Samad on 11/20/2017.
 */

public class BackgroundView extends View {

    private Canvas canvas;
    private Bitmap bitmap;
    private int bgColor = Color.WHITE; //Default

    public BackgroundView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWeight, int oldHeight) {
        super.onSizeChanged(width, height, oldWeight, oldHeight);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(bgColor);
    }
    public void setBackgroundCol(String color) {
        bgColor = Color.parseColor(color);

        int red = Integer.valueOf(color.substring(1,3), 16);
        int green = Integer.valueOf(color.substring(3,5), 16);
        int blue = Integer.valueOf(color.substring(5,7), 16);

        canvas.drawRGB(red,green,blue);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        c.drawBitmap(bitmap, 0, 0, null);
    }
}
