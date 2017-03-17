package com.xysk.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.xysk.library.R;
import com.xysk.library.bean.PieData;
import com.xysk.library.util.MeasureUtil;
import com.xysk.library.util.UnitConvertUtil;

import java.util.List;

/**
 * Created by Administrator on 2017/3/15.
 */
public class ChocPieChart extends View {

    private static final int default_text_size = 16;
    private static final int default_radius = 85;
    private static final float gapDegree = 2.5f;  //每个间隙所占度数
    private int smallRectHeight = 15;

    private int textColor;
    private int textSize;
    private int radius;
    private Paint mTextPaint;
    private Paint mSectorPaint;
    private Paint mInnerCirclePaint;
    private Paint mNamePaint;
    private List<PieData> datas;
    private float proportionSum;
    private float innerRadius;
    private int mBackgroundColor;
//    int i = 1;

    public ChocPieChart(Context context) {
        this(context, null);
    }

    public ChocPieChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChocPieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChocPieChart,
                0, 0);
        try {
            textColor = a.getColor(R.styleable.ChocPieChart_textColor, ContextCompat.getColor(context, R.color.default_text_color));
            textSize = a.getDimensionPixelOffset(R.styleable.ChocPieChart_textSize, UnitConvertUtil.Sp2Px(context, default_text_size));
            radius = a.getDimensionPixelOffset(R.styleable.ChocPieChart_radius, UnitConvertUtil.Dp2Px(context, default_radius));
            mBackgroundColor = a.getColor(R.styleable.ChocPieChart_android_background, ContextCompat.getColor(context, R.color.default_background_color));
        }finally {
            a.recycle();
        }
        init();
    }

    private void init() {
//        Log.i("TAG", "init");
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setBackgroundColor(mBackgroundColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        smallRectHeight = MeasureUtil.getTextHeight(mTextPaint, "测试");

        mSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorPaint.setColor(Color.BLUE);

        mNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNamePaint.setColor(Color.BLUE);

    }

    public void setDatas(List<PieData> datas) {
        this.datas = datas;
        for (PieData pd:datas) {
            this.proportionSum += pd.getProportion();
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        mInnerCirclePaint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //onMeasure方法会被调用多次

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

//        Log.i("TAG", "i: " + i);
//        switch(i) {
//            case 1:
//                Log.i("TAG", "widthMode1: " + widthMode);
//                Log.i("TAG", "widthSize1: " + widthSize);
//                Log.i("TAG", "heightMode1: " + heightMode);
//                Log.i("TAG", "heightSize1: " + heightSize);
//                Log.i("TAG", "\n");
//                break;
//            case 2:
//                Log.i("TAG", "widthMode2: " + widthMode);
//                Log.i("TAG", "widthSize2: " + widthSize);
//                Log.i("TAG", "heightMode2: " + heightMode);
//                Log.i("TAG", "heightSize2: " + heightSize);
//                Log.i("TAG", "\n");
//                break;
//            case 3:
//                Log.i("TAG", "widthMode3: " + widthMode);
//                Log.i("TAG", "widthSize3: " + widthSize);
//                Log.i("TAG", "heightMode3: " + heightMode);
//                Log.i("TAG", "heightSize3: " + heightSize);
//                Log.i("TAG", "\n");
//                break;
//            case 4:
//                Log.i("TAG", "widthMode4: " + widthMode);
//                Log.i("TAG", "widthSize4: " + widthSize);
//                Log.i("TAG", "heightMode4: " + heightMode);
//                Log.i("TAG", "heightSize4: " + heightSize);
//                Log.i("TAG", "\n");
//                break;
//        }

        if(widthMode == MeasureSpec.EXACTLY) {
            radius = Math.min(radius, (widthSize+getPaddingLeft()+getPaddingRight())/2);
//            Log.i("TAG", "widthSize-getPaddingLeft(): " + (widthSize-getPaddingLeft()));
        }
        if(heightMode == MeasureSpec.EXACTLY) {
            radius = Math.min(radius, (heightSize+getPaddingTop()+getPaddingBottom())/2);
        }
        width = radius*2 + getPaddingLeft() + getPaddingRight();
        height = radius*2 + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width*2, height);
//        i++;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(datas == null || datas.size() == 0) {
            return;
        }
        innerRadius = radius*3f/7;
        Log.i("TAG", "radius: " + radius);
        //减去间隙之后的总度数
        float totalDegree = 360 - gapDegree*datas.size();
        RectF rectF = new RectF(getPaddingLeft(), getPaddingTop(), getPaddingLeft()+radius*2, getPaddingTop()+radius*2);
        float startAngle = 0;
        float sweepAngle = 0;
        int nameRegionTop = 0;
        for (PieData pd:datas) {
            startAngle += sweepAngle + gapDegree;
            sweepAngle = (pd.getProportion()*totalDegree)/proportionSum;
            nameRegionTop += smallRectHeight + 6;
            mSectorPaint.setColor(pd.getColor());
            mNamePaint.setColor(pd.getColor());
            drawSector(canvas, rectF, startAngle, sweepAngle);
            drawText(canvas, pd.getProportion()/proportionSum*100+"%", startAngle+sweepAngle/2);
            drawNameRegion(canvas, nameRegionTop, pd.getName());
        }
        canvas.drawCircle(getPaddingLeft()+radius, getPaddingTop()+radius, innerRadius, mInnerCirclePaint);

    }

    private void drawSector(Canvas canvas, RectF oval, float startAngle, float sweepAngle) {
//        Log.i("TAG", "startAngle: " + startAngle + "    " + "sweepAngle: " + sweepAngle);
        canvas.drawArc(oval, startAngle, sweepAngle, true, mSectorPaint);
    }

    private void drawText(Canvas canvas, String text, float angle) {
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        float textX = ((float) (Math.cos(Math.toRadians(angle)) * (radius+innerRadius)/2)) + radius + getPaddingLeft() - rect.width()/2f;
        float textY = ((float) (Math.sin(Math.toRadians(angle)) * (radius+innerRadius)/2)) + radius + getPaddingTop();
//        Log.i("TAG", "textX: " + textX + "   " + "textY: " + textY + "   " + "angle: " + angle);
        canvas.drawText(text, textX, textY, mTextPaint);
    }

    private void drawNameRegion(Canvas canvas, int top, String name) {
        int left  = (int)(getWidth()/2f) + 15;
        Rect rect = new Rect(left, top, left+smallRectHeight, top+smallRectHeight);
        canvas.drawRect(rect, mNamePaint);
        canvas.drawText(name, left+smallRectHeight+10, top+smallRectHeight*5f/6, mTextPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        redraw();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        redraw();
    }

    private void redraw() {
        init();
        invalidate();
        requestLayout();
    }

}
