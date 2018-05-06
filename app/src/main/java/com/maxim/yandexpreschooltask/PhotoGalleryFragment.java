package com.maxim.yandexpreschooltask;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.maxim.yandexpreschooltask.activities.AboutActivity;
import com.maxim.yandexpreschooltask.activities.FullImageActivity;
import com.maxim.yandexpreschooltask.api.FetchRecentPhotos;
import com.maxim.yandexpreschooltask.api.SearchPhotos;
import com.maxim.yandexpreschooltask.entities.GalleryItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhotoGalleryFragment extends Fragment implements OnPhotoClickListener{

    // Ключ доступа
    private static final String API_KEY = "c8d1f3b8ba6bc609d41e3d23da4078bb";
    // Метод для получения последних картинок
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    // Метод для поиска картинок по заданному имени
    private static final String SEARCH_METHOD = "flickr.photos.search";
    // Формат в котором возвращается ответ с сервера
    private static final String FORMAT = "json";
    // Получение чистого json
    private static final int NOJSONCALLBACK = 1;
    // Для безопасного поиска (не особо работает если честно)
    private static final int SAFESEARCH = 1;
    // Какого размера картинки получаем (url_n - 320x320, url_o - изначальный размер)
    private static final String EXTRAS = "url_n,url_o";
    // То как сортируются найденные картинки. В нашем случае по релевантности.
    private static final String SORTPHOTOS = "relevance";
    // То, сколько картинок возвращается при каждом запросе.
    private static final int PERPAGE = 50;

    public static final String URL = "url";

    @BindView(R.id.fragment_photo_gallery_recycler_view)
    RecyclerView photoRecyclerView;
    @BindView(R.id.fragment_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.disconnected_view)
    RelativeLayout disconnectedView;

    private Unbinder unbinder;
    // То какую страницу нам нужно получить, увеличивается на 1 при скролле и сбрасывается обратно при новом поиске.
    private int lastFetchedPage = 1;
    // Ширина 1 колонки
    private static final int COL_WIDTH = 300;
    // Поле хранящее, введенное пользователем слово, по которому потом идет поиск
    private String searchQuery = null;
    // Элементы хранятся во фрагменте, для того чтобы они оставались после пересоздании
    // активити(поворот экрана).
    private List<GalleryItem> items = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        unbinder = ButterKnife.bind(this, v);

        if (savedInstanceState != null) {
            hideProgressBar();
        }

        PreCachingGridLayoutManager preCachingGridLayoutManager = new PreCachingGridLayoutManager(
                getActivity().getApplicationContext(), 3,
                PreCachingGridLayoutManager.VERTICAL, false
        );

        photoRecyclerView.setLayoutManager(preCachingGridLayoutManager);
        photoRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(preCachingGridLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                updateItems();
            }
        });
        photoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int numColumns = photoRecyclerView.getWidth() / COL_WIDTH;
            GridLayoutManager layoutManager = (GridLayoutManager) photoRecyclerView.getLayoutManager();
            layoutManager.setSpanCount(numColumns);
        });

        if (!items.isEmpty())
            setupAdapter();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        showPhotosIfOnline();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                lastFetchedPage = 1;
                searchView.onActionViewCollapsed();
                showProgrssBar();
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((view, queryTextFocused) -> {
            if(!queryTextFocused) {
                searchView.onActionViewCollapsed();
            }
        });

        searchView.setOnSearchClickListener(view -> searchView.setQuery(searchQuery, false));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                searchQuery = null;
                lastFetchedPage = 1;
                updateItems();
                return true;
            case R.id.menu_item_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {

        Type galleryItemListType = new TypeToken<List<GalleryItem>>() {}.getType();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(galleryItemListType, new PhotosApiResponseDeserializer());
        Gson gson = gsonBuilder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.flickr.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        if (searchQuery == null || searchQuery.trim().equals("")) {
            retrofit.create(FetchRecentPhotos.class).fetch(API_KEY, FORMAT, NOJSONCALLBACK, SAFESEARCH, EXTRAS,
                    FETCH_RECENT_METHOD, lastFetchedPage, PERPAGE).enqueue(new Callback<List<GalleryItem>>() {
                @Override
                public void onResponse(@Nullable Call<List<GalleryItem>> call,@Nullable Response<List<GalleryItem>> response) {
                    if(lastFetchedPage > 1) {
                        int positionStart = items.size() + 1;
                        items.addAll(response.body());
                        if (items.size() - positionStart - 1 > 0)
                            photoRecyclerView.getAdapter().notifyItemRangeInserted(positionStart, items.size());
                    } else {
                        items = response.body();
                        setupAdapter();
                        if(progressBar != null) {
                            hideProgressBar();
                        }
                    }
                    lastFetchedPage++;
                }

                @Override
                public void onFailure(Call<List<GalleryItem>> call, Throwable t) {
                    Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            retrofit.create(SearchPhotos.class).search(API_KEY, FORMAT, NOJSONCALLBACK, SAFESEARCH, EXTRAS,
                    SEARCH_METHOD, lastFetchedPage, PERPAGE, SORTPHOTOS, searchQuery.trim()).enqueue(new Callback<List<GalleryItem>>() {
                @Override
                public void onResponse(Call<List<GalleryItem>> call, Response<List<GalleryItem>> response) {
                    if(lastFetchedPage > 1) {
                        int positionStart = items.size() + 1;
                        items.addAll(response.body());
                        if (items.size() - positionStart - 1 > 0)
                            photoRecyclerView.getAdapter().notifyItemRangeInserted(positionStart, items.size());
                    } else {
                        items = response.body();
                        setupAdapter();
                        if(progressBar != null) {
                            hideProgressBar();
                        }
                    }
                    lastFetchedPage++;
                }
                @Override
                public void onFailure(Call<List<GalleryItem>> call, Throwable t) {
                    Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showProgrssBar() {
        progressBar.setVisibility(View.VISIBLE);
        photoRecyclerView.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        photoRecyclerView.setVisibility(View.VISIBLE);
        disconnectedView.setVisibility(View.GONE);
    }

    private void setupAdapter() {
        if (isAdded()) {
            photoRecyclerView.setAdapter(new PhotoAdapter(items, getActivity(), this));
        }
    }

    private void showPhotosIfOnline() {
        if (isOnline()) {
            updateItems();
        } else {
            showDisconnectedView();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void showDisconnectedView() {
        disconnectedView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @OnClick(R.id.disconnected_button)
    public void onViewClicked(View view) {
        showPhotosIfOnline();
    }

    @Override
    public void onClick(String url) {
        Intent intent = new Intent(getActivity(), FullImageActivity.class);
        intent.putExtra(URL, url);
        startActivity(intent);
    }
}
