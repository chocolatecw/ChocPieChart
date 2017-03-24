package com.xysk.library.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.icu.util.Measure;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.xysk.library.R;
import com.xysk.library.bean.PieData;
import com.xysk.library.util.MeasureUtil;
import com.xysk.library.util.UnitConvertUtil;

import java.text.NumberFormat;
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
    private Paint mShadowCirclePaint;
    private List<PieData> datas;
    private float proportionSum;
    private float innerRadius;
    private int mBackgroundColor;
    private float curSweepAngle;
    private int sectorIndex = 0;
    private float totalDegree;
    private RectF rectF;
    private int tapRegionIndex = -1;
    private boolean isTap = false;
    private int animationRadius;
    private int pieLeft;
    private int pieTop;
    private float animShelterRadius;
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
            mBackgroundColor = a.getColor(R.styleable.ChocPieChart_android_background, Color.BLACK);
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
        smallRectHeight = MeasureUtil.getTextHeight(mTextPaint, "测高");

        mSectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectorPaint.setColor(Color.BLUE);

        mNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNamePaint.setColor(Color.BLUE);

        mShadowCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.shadow_color));
    }

    private float getSweepAngle(PieData pd) {
        return (pd.getProportion()*totalDegree)/proportionSum;
    }

    private float getStartAngle(int index) {
        float startAngle = 0;
        for (int i = 0; i < index; i++) {
            startAngle += getSweepAngle(getItem(i)) + gapDegree;
        }
        return startAngle;
    }

    private String getPercentage(int index) {
        NumberFormat nt = NumberFormat.getPercentInstance();
        //设置百分数精确度2即保留两位小数
        nt.setMinimumFractionDigits(1);
        return nt.format(getItem(index).getProportion()/proportionSum);
    }

    private PieData getItem(int i) {
        return datas.get(i);
    }

    public void setDatas(List<PieData> datas) {
        if(datas == null || datas.isEmpty()) {
            return;
        }
        this.datas = datas;
        for (PieData pd:datas) {
            this.proportionSum += pd.getProportion();
        }
        totalDegree = 360 - gapDegree*datas.size();
        startSectorAnimator();
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
        int tapExtensionRadius;

        if(widthMode == MeasureSpec.EXACTLY) {
            radius = Math.min(radius, (widthSize+getPaddingLeft()+getPaddingRight())/2);
//            Log.i("TAG", "widthSize-getPaddingLeft(): " + (widthSize-getPaddingLeft()));
        }
        if(heightMode == MeasureSpec.EXACTLY) {
            radius = Math.min(radius, (heightSize+getPaddingTop()+getPaddingBottom())/2);
        }
        innerRadius = radius*3f/7;
        tapExtensionRadius = (int) (radius*2f/9);
        animShelterRadius = innerRadius + tapExtensionRadius;
        animationRadius = radius+tapExtensionRadius;
        pieLeft = getPaddingLeft()+tapExtensionRadius;
        pieTop = getPaddingTop()+tapExtensionRadius;
        width = animationRadius*2 + getPaddingLeft() + getPaddingRight();
        height = animationRadius*2 + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width*2, height);
//        i++;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        rectF = new RectF(pieLeft, pieTop, pieLeft+radius*2, pieTop+radius*2);
        if(isTap) {
            drawDueTap(canvas);
            isTap = false;
            return;
        }

        if(sectorIndex < datas.size()) {
            int index = sectorIndex;
            for (int i = 0; i < index; i++) { //这里sectorIndex要同步
                mSectorPaint.setColor(getItem(i).getColor());
                canvas.drawArc(rectF, getStartAngle(i), getSweepAngle(getItem(i)), true, mSectorPaint);
                drawText(canvas, getPercentage(i), getStartAngle(i)+getSweepAngle(getItem(i))/2);
            }
        }

        mSectorPaint.setColor(getItem(sectorIndex).getColor());
        float sweepAngle = curSweepAngle-getStartAngle(sectorIndex) <= getSweepAngle(getItem(sectorIndex))
                ? curSweepAngle-getStartAngle(sectorIndex) : getSweepAngle(getItem(sectorIndex));
        canvas.drawArc(rectF, getStartAngle(sectorIndex), sweepAngle, true, mSectorPaint);
        if(sweepAngle == getSweepAngle(getItem(sectorIndex))) {
            drawText(canvas, getPercentage(sectorIndex), getStartAngle(sectorIndex)+sweepAngle/2);
        }

//        canvas.drawCircle(getPaddingLeft()+radius, getPaddingTop()+radius, radius*3.8f/7, mShadowCirclePaint);
        canvas.drawCircle(pieLeft+radius, pieTop+radius, innerRadius, mInnerCirclePaint);

        drawNameRegion(canvas);
    }

    private void drawDueTap(Canvas canvas) {
        float startAngle = -gapDegree;
        float sweepAngle = 0;
        for (int i = 0; i < datas.size(); i++) {
            PieData pd = getItem(i);
            startAngle += sweepAngle + gapDegree;
            sweepAngle = (pd.getProportion()*totalDegree)/proportionSum;
            mSectorPaint.setColor(pd.getColor());
            if(i == tapRegionIndex) {
                RectF animationRectF = new RectF(getPaddingLeft(), getPaddingTop(),
                        getPaddingLeft()+animationRadius*2, getPaddingTop()+animationRadius*2);
                RectF animShelter = new RectF(pieLeft+radius-animShelterRadius, pieTop+radius-animShelterRadius,
                        pieLeft+radius+animShelterRadius, pieTop+radius+animShelterRadius);
                drawSector(canvas, animationRectF, startAngle, sweepAngle);
                drawText(canvas, getPercentage(i), startAngle+sweepAngle/2, animationRadius, animShelterRadius);
                canvas.drawArc(animShelter, startAngle-gapDegree, sweepAngle+gapDegree*2, true, mInnerCirclePaint);
            }else {
                drawSector(canvas, rectF, startAngle, sweepAngle);
                drawText(canvas, getPercentage(i), startAngle+sweepAngle/2);
            }
            drawNameRegion(canvas);
        }
        canvas.drawCircle(pieLeft+radius, pieTop+radius, innerRadius, mInnerCirclePaint);
    }

    private void drawSector(Canvas canvas, RectF oval, float startAngle, float sweepAngle) {
//        Log.i("TAG", "startAngle: " + startAngle + "    " + "sweepAngle: " + sweepAngle);
        canvas.drawArc(oval, startAngle, sweepAngle, true, mSectorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.i("TAG", "calAngleByPoint: " + calAngleByPoint(event.getX(), event.getY()));
                tapRegionIndex = judgeRegion(event.getX(), calAngleByPoint(event.getX(), event.getY()));
                if(tapRegionIndex != -1) {
                    isTap = true;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(tapRegionIndex != -1) {
                    tapRegionIndex = -1;
                    isTap = true;
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private float calAngleByPoint(float x, float y) {
//        Log.i("TAG", "x: " + x + "   y: " + y);
//        Log.i("TAG", "tanValue: " + (y-getPaddingTop()-radius)/(x-getPaddingLeft()-radius));
        float angleTemp = (float) Math.toDegrees(Math.atan((y-pieTop-radius)/(x-pieLeft-radius)));
        if(angleTemp < 0) {
            angleTemp += 180;
        }
        if(y == pieTop+radius) {
            if(x > pieLeft+radius) {
                return 0;
            }else {
                return 180;
            }
        }else if(y > pieTop+radius) {
            return angleTemp;
        }else {
            return angleTemp + 180;
        }
    }

    private int judgeRegion(float x, float angle) {
        for (int i = 0; i < datas.size(); i++) {
            if(angle >= getStartAngle(i) && angle <= getStartAngle(i+1)-gapDegree) {
                if(getDistanceByPoint(x, angle) >= innerRadius) {
//                    Log.i("TAG", "点击了：" + getItem(i).getName());
                    return i;
                }else {
//                    Log.i("TAG", "点击了：无效区域");
                    return -1;
                }
            }
        }
        return -1;
    }

    private float getDistanceByPoint(float x, float angle) {
        return (float) ((x-pieLeft-radius)/Math.cos(Math.toRadians(angle)));
    }

    private void startSectorAnimator() {
        mSectorPaint.setColor(getItem(0).getColor());
        ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
        animator.setDuration(1000);
//        TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
//        animator.setInterpolator(timeInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                curSweepAngle = (float)animation.getAnimatedValue();
                //sectorIndex < datas.size()防止可能的数组越界
                if(sectorIndex < datas.size() && curSweepAngle >= getStartAngle(sectorIndex+1)) {
                    sectorIndex++;
                }
                if(sectorIndex < datas.size()) {
                    invalidate();
                }
            }
        });
        animator.start();
    }

    private void drawText(Canvas canvas, String text, float angle) {
//        float textX = ((float) (Math.cos(Math.toRadians(angle)) * (radius+innerRadius)/2)) + radius + pieLeft
//                - MeasureUtil.getTextWidth(mTextPaint, text)/2f;
//        float textY = ((float) (Math.sin(Math.toRadians(angle)) * (radius+innerRadius)/2)) + radius + pieTop;
////        Log.i("TAG", "textX: " + textX + "   " + "textY: " + textY + "   " + "angle: " + angle);
//        canvas.drawText(text, textX, textY, mTextPaint);
        drawText(canvas, text, angle, radius, innerRadius);
    }

    private void drawText(Canvas canvas, String text, float angle, float r, float innerR) {
        float textX = ((float) (Math.cos(Math.toRadians(angle)) * (r+innerR)/2)) + radius + pieLeft
                - MeasureUtil.getTextWidth(mTextPaint, text)/2f;
        float textY = ((float) (Math.sin(Math.toRadians(angle)) * (r+innerR)/2)) + radius + pieTop;
//        Log.i("TAG", "textX: " + textX + "   " + "textY: " + textY + "   " + "angle: " + angle);
        canvas.drawText(text, textX, textY, mTextPaint);
    }

    private void drawNameRegionUnit(Canvas canvas, int top, String name) {
        int left  = (int)(getWidth()/2f) + 15;
        Rect rect = new Rect(left, top, left+smallRectHeight, top+smallRectHeight);
        canvas.drawRect(rect, mNamePaint);
        canvas.drawText(name, left+smallRectHeight+10, top+smallRectHeight*5f/6, mTextPaint);
    }

    private void drawNameRegion(Canvas canvas) {
        int nameRegionTop = 0;
        for (PieData pd:datas) {
            nameRegionTop += smallRectHeight + 6;
            mNamePaint.setColor(pd.getColor());
            drawNameRegionUnit(canvas, nameRegionTop, pd.getName());
        }
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
