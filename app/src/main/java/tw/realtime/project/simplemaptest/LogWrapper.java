package tw.realtime.project.simplemaptest;

import android.util.Log;



/**
 * 控制 Log 訊息是否要顯示或關閉之用
 * <p>
 * Created by vexonelite on 2017/5/25.
 */
public class LogWrapper {

    public static final boolean HAS_ENABLE = true;

    /**
     * 呼叫 Log.mode(tag, message) 方法
     * @param mode      DEBUG, INFO, WARN, ERROR 其中一個
     * @param tag       For Class
     * @param message   要顯示的訊息字串
     */
    public static void showLog(int mode, String tag, String message) {

        if ( (!HAS_ENABLE) || (null == tag) || (null == message) ) {
            return;
        }

        switch (mode) {
            case Log.DEBUG: {
                Log.d(tag, message);
                break;
            }
            case Log.INFO: {
                Log.i(tag, message);
                break;
            }
            case Log.WARN: {
                Log.w(tag, message);
                break;
            }
            case Log.ERROR: {
                Log.e(tag, message);
                break;
            }

        }
    }

    /**
     * 呼叫 Log.mode(tag, message, throwable) 方法
     * @param mode      DEBUG, INFO, WARN, ERROR 其中一個
     * @param tag       For Class
     * @param message   要顯示的訊息字串
     * @param throwable 例外物件
     */
    public static void showLog(int mode, String tag, String message, Throwable throwable) {
        if ( (!HAS_ENABLE) || (null == tag) || (null == message) || (null == throwable) ) {
            return;
        }

        switch (mode) {
            case Log.DEBUG: {
                Log.d(tag, message, throwable);
                break;
            }
            case Log.INFO: {
                Log.i(tag, message, throwable);
                break;
            }
            case Log.WARN: {
                Log.w(tag, message, throwable);
                break;
            }
            case Log.ERROR: {
                Log.e(tag, message, throwable);
                throwable.printStackTrace();
                break;
            }
        }
    }

    /**
     * 將字串長度超過 1000 的訊息，分段呼叫 showLog(mode, tag, message) 方法來顯示
     * @param mode      DEBUG, INFO, WARN, ERROR 其中一個
     * @param tag       For Class
     * @param message   要顯示的訊息字串
     */
    public static void showLongLog(int mode, String tag, String message) {
        if ( (!HAS_ENABLE) || (null == tag) || (null == message) ) {
            return;
        }
        int maxLogSize = 1000;
        for(int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            LogWrapper.showLog(mode, tag, message.substring(start, end));
        }
    }
}
