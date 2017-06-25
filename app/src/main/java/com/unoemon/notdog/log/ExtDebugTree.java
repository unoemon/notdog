package com.unoemon.notdog.log;

import android.text.TextUtils;
import android.util.Log;
import timber.log.Timber;

/**
 * Ext debug tree
 *
 */

public class ExtDebugTree extends Timber.DebugTree {
    private static final int MAX_LOG_LENGTH = 4000;
    private static final String CALLER_INFO_FORMAT = " at %s(%s:%s)";
    private boolean mShowLink = true;
    private String mCallerInfo;

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if(mShowLink) {
            mCallerInfo = getCallerInfo(new Throwable().getStackTrace());
        }

        if (message.length() < MAX_LOG_LENGTH) {
            printSingleLine(priority, tag, message + mCallerInfo);
        } else {
            printMultipleLines(priority, tag, message);
        }

    }

    private void printMultipleLines(int priority, String tag, String message) {
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                String part = message.substring(i, end);
                printSingleLine(priority, tag, part);
                i = end;
            } while (i < newline);
        }

        if(mShowLink && !TextUtils.isEmpty(mCallerInfo)) {
            printSingleLine(priority, tag, mCallerInfo);
        }
    }

    private void printSingleLine(int priority, String tag, String message) {
        if (priority == Log.ASSERT) {
            Log.wtf(tag, message);
        } else {
            Log.println(priority, tag, message);
        }
    }


    private String getCallerInfo(StackTraceElement[] stacks) {
        if (stacks == null || stacks.length < 6) {
            // are you using proguard???
            return "";
        }
        return formatForLogCat(stacks[6]);
    }

    private static String formatForLogCat(StackTraceElement stack) {
        String className = stack.getClassName();
        String packageName = className.substring(0, className.lastIndexOf("."));
        return String.format(CALLER_INFO_FORMAT, packageName,
                stack.getFileName(), stack.getLineNumber());
    }

}