package com.xysk.library.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.WindowManager;


/**
 * Created by Administrator on 2017/3/7.
 */
public class SystemInfoUtil {

    //获取进程名
    public static String getProcessName(Context context) {
        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process:activityManager.getRunningAppProcesses()) {
            if(process.pid == pid) {
                return process.processName;
            }
        }
        return null;
    }

    public static int[] getScreenSize(Context context) {
        int[] screenSize = {0, 0};
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenSize[0] = displayMetrics.widthPixels;
        screenSize[1] = displayMetrics.heightPixels;
        return screenSize;
    }
}
