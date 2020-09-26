package custum_views;

import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class PaintView
        extends View {
    private Path path=new Path();
    private Paint drawPaint=new Paint(),canvasPaint=new Paint();

    private boolean isClear=false;

    private int brushColor= Color.BLACK;
    private float brushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10,getResources().getDisplayMetrics());;
    private Bitmap mbitmap;
    private Canvas canvas;

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d("TAG","paint");
        setUpDrawing();
    }

    public void initailize(float brushSize,int color)
    {
        this.brushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,brushSize,getResources().getDisplayMetrics());;
        brushColor=color;
    }
    public void setEraser(boolean isClear)
    {
        this.isClear=isClear;
        if(isClear)
        {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else{
            drawPaint.setXfermode(null);
        }
    }
    public void setBrushSize(float newSize)
    {
        float pixelAmount= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public int getBrushColor()
    {
        return brushColor;
    }
    public float getBrushSize()
    {
        return  brushSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mbitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        canvas=new Canvas(mbitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mbitmap,0,0,canvasPaint);
        canvas.drawPath(path,drawPaint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float pointX=event.getX();
        float pointY=event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(pointX,pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(pointX,pointY);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path,drawPaint);
                path.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public Bitmap getBitmap()
    {
        return mbitmap;
    }
    public void setUpDrawing()
    {
        path=new Path();
        drawPaint=new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setColor(brushColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(brushSize);
    }
    public void setColor(String newColor)
    {
        invalidate();
        brushColor=Color.parseColor(newColor);
        drawPaint.setColor(brushColor);
    }
}

