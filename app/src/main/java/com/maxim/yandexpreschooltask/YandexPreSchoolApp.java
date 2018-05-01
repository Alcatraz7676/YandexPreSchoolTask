package com.maxim.yandexpreschooltask;

import android.app.Application;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

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

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder
                .memoryCache(new LruCache(60000000))
                .build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}
