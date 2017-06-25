package com.unoemon.notdog.log;

import com.unoemon.notdog.BuildConfig;
import timber.log.Timber;

/**
 * Logger
 * Timber wrapper
 */

public class Logger {

    private static boolean shouldLog = false;

    public static void setLogVisible(boolean visible) {
        shouldLog = visible;
    }

    public static final void d(String msg, Object... args) {
        if (!shouldLog) {
            return;
        }
        if (!BuildConfig.DEBUG) {
            return;
        }
        if(msg.length() == 0){
            msg = " ";
        }
        Timber.d(msg, args);
    }

    public static final void i(String msg, Object... args) {
        if (!shouldLog) {
            return;
        }
        if(msg.length() == 0){
            msg = " ";
        }
        Timber.i(msg, args);
    }

    public static final void v(String msg, Object... args) {
        if (!shouldLog) {
            return;
        }
        if(msg.length() == 0){
            msg = " ";
        }
        Timber.v(msg, args);
    }

    public static final void e(String msg, Object... args) {
        if (!shouldLog) {
            return;
        }
        if(msg.length() == 0){
            msg = " ";
        }
        Timber.e(msg, args);
    }

    public static final void w(String msg, Object... args) {
        if (!shouldLog) {
            return;
        }
        if(msg.length() == 0){
            msg = " ";
        }
        Timber.w(msg, args);
    }
}
