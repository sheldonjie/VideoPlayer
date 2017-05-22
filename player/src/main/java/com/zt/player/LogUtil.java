package com.zt.player;

import android.util.Log;

/**
 * Created by qq121 on 2017/3/8.
 */

public class LogUtil {

    private static final String TAG = "OpenCmstop";

    public static void d(String tag, String msg) {
        Log.d(TAG, tag + " " + msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + " " + msg);
    }

    public static void w(String tag, String msg, Throwable throwable) {
        Log.w(TAG, tag + " " + msg, throwable);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + " " + msg);
    }

    public static void e(String tag, String msg, Throwable throwable) {
        Log.e(TAG, tag + " " + msg, throwable);
    }

}
