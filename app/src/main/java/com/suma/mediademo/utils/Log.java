package com.suma.mediademo.utils;

/**
 * Log信息输出
 */
public final class Log {

    private static final String APP_TAG = Constant.POJECT_NAME;


    private static final String TAG = "LogUtil";

    public static boolean isDebug = true;

    private Log() {

    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static void showTestInfo(String tag, String msg) {

        if (isDebug) {
            android.util.Log.d(APP_TAG, formatMsg(tag, msg));
        }

    }

    public static void e(Object object, String msg) {
        e(object.getClass().getSimpleName(), msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            android.util.Log.e(APP_TAG, formatMsg(tag, msg));
        }
    }

    public static void e(Object object, String msg, Throwable tr) {
        e(object.getClass().getSimpleName(), msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug) {
            android.util.Log.e(APP_TAG, formatMsg(tag, msg), tr);
        }
    }

    public static void e(Object object, Throwable tr) {
        e(object.getClass().getSimpleName(), tr);
    }

    public static void e(String tag, Throwable tr) {
        if (isDebug) {
            android.util.Log.e(APP_TAG, formatMsg(tag, null), tr);
        }
    }


    public static void w(Object object, String msg) {
        w(object.getClass().getSimpleName(), msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            android.util.Log.w(APP_TAG, formatMsg(tag, msg));
        }
    }

    public static void w(Object object, Throwable tr) {
        w(object.getClass().getSimpleName(), tr);
    }

    public static void w(String tag, Throwable tr) {
        if (isDebug) {
            android.util.Log.w(APP_TAG, formatMsg(tag, null), tr);
        }
    }

    public static void w(Object object, String msg, Throwable tr) {
        w(object.getClass().getSimpleName(), msg, tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (isDebug) {
            android.util.Log.w(APP_TAG, formatMsg(tag, msg), tr);
        }
    }

    public static void i(Object object, String msg) {
        i(object.getClass().getSimpleName(), msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            android.util.Log.i(APP_TAG, formatMsg(tag, msg));
        }
    }

    public static void i(Object object, String msg, Throwable tr) {
        i(object.getClass().getSimpleName(), msg, tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (isDebug) {
            android.util.Log.i(APP_TAG, formatMsg(tag, msg), tr);
        }
    }

    public static void d(Object object, String msg) {
        d(object.getClass().getSimpleName(), msg);
    }

    public static void d(String tag, String msg) {

        if (isDebug) {
            android.util.Log.d(APP_TAG, formatMsg(tag, msg));
        }
    }

    public static void d(Object object, String msg, Throwable tr) {
        d(object.getClass().getSimpleName(), msg, tr);
    }

    public static void d(String tag, String msg, Throwable tr) {

        if (isDebug) {
            android.util.Log.d(APP_TAG, formatMsg(tag, msg), tr);
        }
    }

    public static void v(Object object, String msg) {
        v(object.getClass().getSimpleName(), msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            android.util.Log.v(APP_TAG, formatMsg(tag, msg));
        }
    }

    public static void v(Object object, String msg, Throwable tr) {
        v(object.getClass().getSimpleName(), msg, tr);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (isDebug) {
            android.util.Log.v(APP_TAG, formatMsg(tag, msg), tr);
        }
    }

    private static String formatMsg(String tag, String msg) {
        return "[" + tag + "]:" + msg;
    }


}
