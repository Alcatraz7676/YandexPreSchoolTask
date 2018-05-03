package com.maxim.yandexpreschooltask.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.piasy.biv.view.BigImageView;
import com.maxim.yandexpreschooltask.PhotoGalleryFragment;
import com.maxim.yandexpreschooltask.R;
import com.maxim.yandexpreschooltask.YandexPreSchoolApp;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FullImageActivity extends AppCompatActivity {

    @BindView(R.id.fullImage)
    BigImageView fullImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage);
        ButterKnife.bind(this);
        String url = getIntent().getStringExtra(PhotoGalleryFragment.URL);
        fullImageView.showImage(Uri.parse(url));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = YandexPreSchoolApp.getRefWatcher();
        refWatcher.watch(this);
    }
}
