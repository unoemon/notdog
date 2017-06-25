package com.unoemon.notdog;


import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;


import com.unoemon.notdog.log.ExtDebugTree;
import com.unoemon.notdog.log.Logger;

import timber.log.Timber;

/**
 * Main application
 */
public class MainApplication extends Application {

    private static MainApplication instance = null;
    public static synchronized MainApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setupLogs();
    }

    @Override
    public void onTerminate() {
        Logger.d("");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        Logger.d("");
        super.onLowMemory();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    private void setupLogs() {
        Logger.setLogVisible(BuildConfig.DEBUG);

        if(BuildConfig.DEBUG){
        Timber.plant(new ExtDebugTree());
        }
    }

}
