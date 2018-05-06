package com.maxim.yandexpreschooltask.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.maxim.yandexpreschooltask.R;
import com.maxim.yandexpreschooltask.YandexPreSchoolApp;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.authorIV)
    ImageView authorImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        Glide.with(this)
                .load(R.drawable.author)
                .apply(RequestOptions.circleCropTransform())
                .into(authorImageView);
    }

    // Переопределяем кнопку назад для того чтобы при возвращении не пересоздавалась активити
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
