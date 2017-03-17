package com.xysk.library.util;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

/**
 * Created by Administrator on 2017/3/16.
 */
public class UnitConvertUtil {

    public static int Dp2Px(Context context, float dp) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }

    public static int Sp2Px(Context context, float sp) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, r.getDisplayMetrics());
        return px;
    }

}
