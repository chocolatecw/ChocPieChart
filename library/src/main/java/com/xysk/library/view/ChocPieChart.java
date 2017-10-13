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
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.xysk.library.R;
import com.xysk.library.bean.PieData;
import com.xysk.library.util.MeasureUtil;
import com.xysk.library.util.SystemInfoUtil;
import com.xysk.library.util.UnitConvertUtil;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by Administrator on 2017/3/15.
 */
public class ChocPieChart extends View {

    private static final int default_text_size = 16;
//    private static final int default_radius = 85;
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

    private float totalDegree;
    private RectF rectF;
    private int tapRegionIndex = -1;    //被点击区域

    private int animationRadius;
    private int tapExtensionRadius;

    private float animShelterRadius;

    // 画弧的起始角度
    private float startAngle;
    // 画弧单次扫过的角度，等于弧段的长除以画弧的次数
    private float singleSweepAngle;
    // 画某段弧的累计长
    private float singleTotalSweepAngle;
    // 已经画了几部分，即几段弧
    private int sectorIndex = 0;
    // 保存上次的 sectorIndex 值
    private int lastSectorIndex = sectorIndex-1;
    // 一段弧分几次画，固定值
    private static final int size = 30;
    // 一段弧分几次画，递减
    private int count = size;
    // 延时间隔
    private static final int DelaySecond = 1;

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
//            radius = a.getDimensionPixelOffset(R.styleable.ChocPieChart_radius, UnitConvertUtil.Dp2Px(context, default_radius));
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
//        startSectorAnimator();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        mInnerCirclePaint.setColor(color);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        //onMeasure方法会被调用多次
//
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        int width;
//        int height;
//        int tapExtensionRadius;
//
//        if(widthMode == MeasureSpec.EXACTLY) {
//            radius = Math.min(radius, (widthSize+getPaddingLeft()+getPaddingRight())/2);
////            Log.i("TAG", "widthSize-getPaddingLeft(): " + (widthSize-getPaddingLeft()));
//        }
//        if(heightMode == MeasureSpec.EXACTLY) {
//            radius = Math.min(radius, (heightSize+getPaddingTop()+getPaddingBottom())/2);
//        }
//        innerRadius = radius*3f/7;
//        tapExtensionRadius = (int) (radius*2f/9);
//        animShelterRadius = innerRadius + tapExtensionRadius;
//        animationRadius = radius+tapExtensionRadius;
//        pieLeft = getPaddingLeft()+tapExtensionRadius;
//        pieTop = getPaddingTop()+tapExtensionRadius;
//        width = animationRadius*2 + getPaddingLeft() + getPaddingRight();
//        height = animationRadius*2 + getPaddingTop() + getPaddingBottom();
//
//        setMeasuredDimension(width*2, height);
////        i++;
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(UnitConvertUtil.Dp2Px(getContext(), 180), UnitConvertUtil.Dp2Px(getContext(), 180));
        }else if(widthMode == MeasureSpec.AT_MOST) {
            int sideSize = Math.min(heightSize, SystemInfoUtil.getScreenSize(getContext())[0]);
            setMeasuredDimension(sideSize, sideSize);
        }else if(heightMode == MeasureSpec.AT_MOST) {   //这里只考虑竖屏情况，宽一定小于高
            setMeasuredDimension(widthSize, widthSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        直径 这里只考虑上下左右 padding 都相等的情况
        int diameter = Math.min(getMeasuredWidth(), getMeasuredHeight()) - getPaddingLeft()*2;
        radius = (int) (diameter/2f);
        innerRadius = radius*3f/7;
        tapExtensionRadius = (int) (radius*1f/9);
        animShelterRadius = innerRadius + tapExtensionRadius;
        animationRadius = radius+tapExtensionRadius;

        rectF = new RectF(getPaddingLeft(), getPaddingTop(), getPaddingLeft()+diameter, getPaddingTop()+diameter);

        drawOldSector(canvas);
        // count == size 说明 是画弧段的最后一次，所以不需要，因为已经通过 drawOldSector 画出
        if(singleTotalSweepAngle > 0 && count != size) {
            canvas.drawArc(rectF, startAngle, singleTotalSweepAngle, true, mSectorPaint);
        }

        if(sectorIndex < datas.size()) {
            triggerDraw();
        }

        if(tapRegionIndex != -1) {
            drawClickedArc(canvas);
        }

        canvas.drawCircle(getPaddingLeft()+radius, getPaddingTop()+radius, innerRadius, mInnerCirclePaint);

//        drawNameRegion(canvas);
    }

    /*
    * 触发onDraw 的方法
    * */
    void triggerDraw() {
        if(datas == null) {
            return;
        }

        if(sectorIndex > lastSectorIndex) {
            singleSweepAngle = getSweepAngle(getItem(sectorIndex)) / size;
            mSectorPaint.setColor(getItem(sectorIndex).getColor());
            lastSectorIndex = sectorIndex;
        }

        if(count > 0) {
            if(count == size) {
                startAngle = getStartAngle(sectorIndex);
                singleTotalSweepAngle = singleSweepAngle;
            }else {
                singleTotalSweepAngle += singleSweepAngle;
            }
            count--;
        }else {
            // 这句其实没有用，因为最后一次已经不需要画（没有画）
            singleTotalSweepAngle = getSweepAngle(getItem(sectorIndex));
            count = size;
            sectorIndex++;
        }

        if(count == size) { // 如果是开始画下一个弧段，则不需要延时
            invalidate();
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            }, DelaySecond);
        }

    }

    /*
    * 画已经画过的弧段*/
    void drawOldSector(Canvas canvas) {
//        Log.i("TAG", "sectorIndex: " + sectorIndex);
        Paint paint = new Paint(mSectorPaint);
        for (int i = 0; i < sectorIndex; i++) {
            paint.setColor(getItem(i).getColor());
            canvas.drawArc(rectF, getStartAngle(i), getSweepAngle(getItem(i)), true, paint);
            // 画百分比文字
            // 最后一段弧的文字会被覆盖，已处理此Bug
            drawText(canvas, getPercentage(i), getStartAngle(i)+getSweepAngle(getItem(i))/2);
        }
    }

    /*
    * 通过覆盖的方式实现点击效果
    * */
    void drawClickedArc(Canvas canvas) {
        float startAngle = getStartAngle(tapRegionIndex);
        float sweepAngle = getSweepAngle(getItem(tapRegionIndex));

        Paint paint = new Paint(mSectorPaint);
        paint.setColor(mBackgroundColor);
        canvas.drawArc(rectF, startAngle, sweepAngle,
                true, paint);

        RectF animationRectF = new RectF(getPaddingLeft()-tapExtensionRadius, getPaddingTop()-tapExtensionRadius,
                getPaddingLeft()-tapExtensionRadius+animationRadius*2, getPaddingTop()-tapExtensionRadius+animationRadius*2);
        RectF animShelter = new RectF(getPaddingLeft()+radius-animShelterRadius, getPaddingTop()+radius-animShelterRadius,
                getPaddingLeft()+radius+animShelterRadius, getPaddingTop()+radius+animShelterRadius);
        paint.setColor(getItem(tapRegionIndex).getColor());
        canvas.drawArc(animationRectF, startAngle, sweepAngle, true, paint);
        drawText(canvas, getPercentage(tapRegionIndex), startAngle+sweepAngle/2, animationRadius, animShelterRadius);
        canvas.drawArc(animShelter, startAngle, sweepAngle, true, mInnerCirclePaint);

    }

    /* 延时画弧
    * sweepAngle: 总角度
    * size： 分几次画
    * count： 还需要画几次
    * */
//    void delayDrawArc(final Canvas canvas, final RectF rectf, final float startAngle, final float sweepAngle,
//                      final int size, final int count, final Paint paint) {
//        Log.i("TAG", "error: " + "找Bug");
//        final float singleSweepAngle = sweepAngle/size;
//        if(count > 0) {
//            canvas.drawArc(rectf, startAngle, singleSweepAngle, true, paint);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    delayDrawArc(canvas, rectf, startAngle+singleSweepAngle, sweepAngle, size, count-1, paint);
//                }
//            }, 300);
//        }else {
//            canvas.drawArc(rectf, startAngle, sweepAngle-singleSweepAngle*size, true, paint);
//        }
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.i("TAG", "calAngleByPoint: " + calAngleByPoint(event.getX(), event.getY()));
                tapRegionIndex = judgeRegion(event.getX(), calAngleByPoint(event.getX(), event.getY()));
                if(tapRegionIndex != -1) {
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(tapRegionIndex != -1) {
                    // 松手的时候，tapRegionIndex 置为 -1，表示当前没有弧段被点击
                    tapRegionIndex = -1;
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private float calAngleByPoint(float x, float y) {
//        Log.i("TAG", "x: " + x + "   y: " + y);
//        Log.i("TAG", "tanValue: " + (y-getPaddingTop()-radius)/(x-getPaddingLeft()-radius));
        float angleTemp = (float) Math.toDegrees(Math.atan((y-getPaddingTop()-radius)/(x-getPaddingLeft()-radius)));
        if(angleTemp < 0) {
            angleTemp += 180;
        }
        if(y == getPaddingTop()+radius) {
            if(x > getPaddingLeft()+radius) {
                return 0;
            }else {
                return 180;
            }
        }else if(y > getPaddingTop()+radius) {
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
        return (float) ((x-getPaddingLeft()-radius)/Math.cos(Math.toRadians(angle)));
    }

    /*
    * 废弃方法 原本用动画实现时，使用的方法
    * */
//    private void startSectorAnimator() {
//        mSectorPaint.setColor(getItem(0).getColor());
//        ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
//        animator.setDuration(1000);
////        TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
////        animator.setInterpolator(timeInterpolator);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                curSweepAngle = (float)animation.getAnimatedValue();
//                //sectorIndex < datas.size()防止可能的数组越界
//                if(sectorIndex < datas.size() && curSweepAngle >= getStartAngle(sectorIndex+1)) {
//                    sectorIndex++;
//                }
//                if(sectorIndex < datas.size()) {
//                    invalidate();
//                }else {
////                    sectorIndex = 0;
//                }
//            }
//        });
//        animator.start();
//    }

    private void drawText(Canvas canvas, String text, float angle) {
        drawText(canvas, text, angle, radius, innerRadius);
    }

    private void drawText(Canvas canvas, String text, float angle, float r, float innerR) {
        float textX = ((float) (Math.cos(Math.toRadians(angle)) * (r+innerR)/2)) + radius + getPaddingLeft()
                - MeasureUtil.getTextWidth(mTextPaint, text)/2f;
        float textY = ((float) (Math.sin(Math.toRadians(angle)) * (r+innerR)/2)) + radius + getPaddingTop();
//        Log.i("TAG", "textX: " + textX + "   " + "textY: " + textY + "   " + "angle: " + angle);
        canvas.drawText(text, textX, textY, mTextPaint);
    }

    private void drawNameRegionUnit(Canvas canvas, int top, String name) {
        int left  = (int)(getWidth()/2f) + 15;
        Rect rect = new Rect(left, top, left+smallRectHeight, top+smallRectHeight);
        canvas.drawRect(rect, mNamePaint);
        canvas.drawText(name, left+smallRectHeight+10, top+smallRectHeight*5f/6, mTextPaint);
    }

//    画标识区
//    private void drawNameRegion(Canvas canvas) {
//        int nameRegionTop = 0;
//        for (PieData pd:datas) {
//            nameRegionTop += smallRectHeight + 6;
//            mNamePaint.setColor(pd.getColor());
//            drawNameRegionUnit(canvas, nameRegionTop, pd.getName());
//        }
//    }

//    public int getTextColor() {
//        return textColor;
//    }
//
//    public void setTextColor(int textColor) {
//        this.textColor = textColor;
//        redraw();
//    }
//
//    public int getTextSize() {
//        return textSize;
//    }
//
//    public void setTextSize(int textSize) {
//        this.textSize = textSize;
//        redraw();
//    }

//    private void redraw() {
//        init();
//        invalidate();
//        requestLayout();
//    }

}
