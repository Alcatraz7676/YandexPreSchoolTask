package com.maxim.yandexpreschooltask.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.piasy.biv.indicator.ProgressIndicator;
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
    @BindView(R.id.photo_page_progress_bar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage);
        ButterKnife.bind(this);
        // Получаем url оригинала фото(в полном размере)
        String url = getIntent().getStringExtra(PhotoGalleryFragment.URL);
        // Показываем его, а во время загрузки
        fullImageView.showImage(Uri.parse(url));
        fullImageView.setProgressIndicator(new ProgressIndicator() {
            @Override
            public View getView(BigImageView parent) {
                progressBar.setVisibility(View.VISIBLE);
                return progressBar;
            }

            @Override
            public void onStart() {
                progressBar.setProgress(0);
            }

            @Override
            public void onProgress(int progress) {
                if (progress < 0 || progress > 100 || progressBar == null) {
                    return;
                }
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
            }
        });
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
