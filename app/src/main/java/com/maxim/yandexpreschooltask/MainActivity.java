package com.maxim.yandexpreschooltask;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return  PhotoGalleryFragment.newInstance();
    }
}
