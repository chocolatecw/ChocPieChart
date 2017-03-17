package com.xysk.library.util;

import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Administrator on 2017/3/17.
 */
public class MeasureUtil {

    public static int getTextWidth(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    public static int getTextHeight(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }
}
