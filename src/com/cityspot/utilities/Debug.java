package com.cityspot.utilities;

import android.os.Looper;
import android.util.Log;

/**
 * Created by emir on 29/03/14.
 */
public class Debug {
    private static final String TAG = "GoogleGlass";
    private static final String DEBUG_LINE = "%s - [%s] %s";
    private static final String BACKGROUND_THREAD = "BG";
    private static final String UI_THREAD = "UI";
    private static final int CURRENT_LINE_OF_CODE_OFFSET = 4;
    private static boolean sIsDebug;

    public static void setIsDebug(final boolean isDebug) {
    	Log.d(TAG, "isDebug: " + isDebug);
        sIsDebug = isDebug;
    }

    public static void log(final String string) {
        if (!sIsDebug) {
            return;
        }

        final String currentCodeLine = getCurrentLineOfCode();
        final String thread = getThread();
        final String log = String.format(DEBUG_LINE, thread, currentCodeLine, string);

        Log.d(TAG, log);
    }

    private static final String getCurrentLineOfCode(){
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length < CURRENT_LINE_OF_CODE_OFFSET) {
            return null;
        }

        final StackTraceElement stackTraceElement = stackTraceElements[CURRENT_LINE_OF_CODE_OFFSET];
        if (stackTraceElement == null) {
            return null;
        }

        return stackTraceElement.toString();
    }

    public static String getThread() {
        final Thread uiThread = Looper.getMainLooper().getThread();
        final Thread currentThread = Thread.currentThread();

        if (currentThread == uiThread) {
            return UI_THREAD;
        }

        return BACKGROUND_THREAD;
    }

}