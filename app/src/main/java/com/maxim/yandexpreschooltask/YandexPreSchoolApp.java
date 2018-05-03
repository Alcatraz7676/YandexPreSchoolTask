package com.maxim.yandexpreschooltask;

import android.app.Application;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class YandexPreSchoolApp extends Application {

    private static YandexPreSchoolApp instance;
    private RefWatcher refWatcher;

    public static YandexPreSchoolApp get() {
        return instance;
    }

    public static RefWatcher getRefWatcher() {
        return YandexPreSchoolApp.get().refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        instance = (YandexPreSchoolApp) getApplicationContext();
        refWatcher = LeakCanary.install(this);

        BigImageViewer.initialize(GlideImageLoader.with(this));
    }
}
