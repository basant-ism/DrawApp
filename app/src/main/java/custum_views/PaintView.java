package custum_views;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View.OnTouchListener;

import androidx.annotation.Nullable;

import com.basant.drawapp.R;

import helper.Helper;

public class PaintView extends View  {
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mPaint;
    boolean isClear=false;

    private ArrayList<Helper> paths = new ArrayList<Helper>();
    private ArrayList<Helper> undonePaths = new ArrayList<Helper>();
    private int brushColor= Color.BLACK;
    private float brushSize=10f;

    private Bitmap bitmap;

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    public void  initialize(boolean isClear,int color,float brushSize)
    {
        this.isClear=isClear;
        brushColor= color;
        this.brushSize=brushSize;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(brushColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(brushSize);
        mCanvas = new Canvas();
        mPath = new Path();
        Log.d("TAG","contsxt");


    }
    public void setEraser(boolean isClear)
    {
        this.isClear=isClear;
        if(isClear)
        {
            mPaint.setColor(Color.WHITE);
        }
        else{

        }
    }
    public void setColor(String newColor)
    {
        invalidate();
        mPath.reset();
        brushColor=Color.parseColor(newColor);
        mPaint.setColor(brushColor);


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int i=0;
        for (Helper p : paths){

            Paint paint=p.getPaint();
            paint.setStrokeWidth(p.getBrushSize());

            if(p.isClear())
            {
                paint.setColor(Color.WHITE);
            }
            else{
                paint.setColor(p.getColor());
            }
            canvas.drawPath(p.getPath(), paint);
        }
        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touchStart(float x, float y) {
        mPaint.setColor(brushColor);
        mPaint.setStrokeWidth(brushSize);
        if(isClear)
        {
            mPaint.setColor(Color.WHITE);
        }
        undonePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touchUp() {
        mPath.lineTo(mX, mY);


        mCanvas.drawPath(mPath, mPaint);
        Helper helper=new Helper();

        helper.setPath(mPath);
        helper.setPaint(mPaint);
        helper.setBrushSize(brushSize);
        if(isClear)
        {
            helper.setClear(true);
            helper.setColor(Color.WHITE);
        }
        else
        {
            helper.setClear(false);
            helper.setColor(brushColor);
        }

        paths.add(helper);
        mPath = new Path();

    }

    public void onClickUndo () {
        if (paths.size()>0)
        {
            undonePaths.add(paths.remove(paths.size()-1));
            invalidate();
        }


    }

    public void onClickRedo (){
        if (undonePaths.size()>0)
        {
            paths.add(undonePaths.remove(undonePaths.size()-1));
            invalidate();
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("TAG","down");
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("TAG","move");
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                Log.d("TAG","up");
                touchUp();
                invalidate();
                break;
        }
        return true;

    }
public Bitmap getBitmap()
{
    return  bitmap;
}
    public int getBrushColor() {
        return brushColor;
    }
    public void setBrushSize(float newSize)
    {

        invalidate();
        float pixelAmount= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        mPaint.setStrokeWidth(brushSize);

    }


}