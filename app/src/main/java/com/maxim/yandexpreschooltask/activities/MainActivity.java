package com.maxim.yandexpreschooltask.activities;

import android.support.v4.app.Fragment;

import com.maxim.yandexpreschooltask.PhotoGalleryFragment;
import com.maxim.yandexpreschooltask.SingleFragmentActivity;
import com.maxim.yandexpreschooltask.YandexPreSchoolApp;
import com.squareup.leakcanary.RefWatcher;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return  PhotoGalleryFragment.newInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = YandexPreSchoolApp.getRefWatcher();
        refWatcher.watch(this);
    }
}
