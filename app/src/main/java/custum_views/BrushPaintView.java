package custum_views;

import android.content.Context;
import android.graphics.Canvas;

import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;


public class BrushPaintView extends View {
    private Paint paint=new Paint();
    private int paintColor;
    private float brushSize;
    public BrushPaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d("TAG","brush");
        setDrawing();
    }
    public void setDrawing()
    {
        paint=new Paint();
        paint.setColor(paintColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(brushSize);
        invalidate();
    }
    public void setColor(int newColor)
    {
        invalidate();
        paintColor= newColor;
        paint.setColor(paintColor);
    }
    public void setBrushSize(float newSize)
    {
        invalidate();
        float pixelAmount= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        paint.setStrokeWidth(brushSize);

    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0,26,90,26,paint);
    }
    public void initailize(float brushSize,int color)
    {
        this.brushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,brushSize,getResources().getDisplayMetrics());;
        paintColor=color;
    }
}
