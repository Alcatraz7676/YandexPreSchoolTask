package com.maxim.yandexpreschooltask;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.maxim.yandexpreschooltask.api.FetchRecentPhotos;
import com.maxim.yandexpreschooltask.api.SearchPhotos;
import com.maxim.yandexpreschooltask.entities.GalleryItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "mytaglol";

    private static final String API_KEY = "c8d1f3b8ba6bc609d41e3d23da4078bb";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final String FORMAT = "json";
    private static final int NOJSONCALLBACK = 1;
    private static final int SAFESEARCH = 1;
    private static final String EXTRAS = "url_n,url_o";
    private static final String SORTPHOTOS = "relevance";

    @BindView(R.id.fragment_photo_gallery_recycler_view)
    RecyclerView mPhotoRecyclerView;
    @BindView(R.id.fragment_progress_bar)
    ProgressBar mProgressBar;

    private Unbinder unbinder;
    private int lastFetchedPage = 1;
    private static final int COL_WIDTH = 300;
    private String searchQuery = null;
    private List<GalleryItem> items = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        unbinder = ButterKnife.bind(this, v);

        if(savedInstanceState != null) {
            hideProgressBar();
        }

        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!recyclerView.canScrollVertically(1)) {
                    updateItems();
                }
            }
        });
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int numColumns = mPhotoRecyclerView.getWidth() / COL_WIDTH;
                GridLayoutManager layoutManager = (GridLayoutManager)mPhotoRecyclerView.getLayoutManager();
                layoutManager.setSpanCount(numColumns);
            }
        });
        mPhotoRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        if (!items.isEmpty())
            setupAdapter();

        return v;
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
                Log.d(TAG, "QueryTextSubmit: " + query);
                searchQuery = query;
                lastFetchedPage = 1;
                searchView.onActionViewCollapsed();
                showProgrssBar();
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    searchView.onActionViewCollapsed();
                }
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(searchQuery, false);
            }
        });
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

        Log.i(TAG, String.valueOf(lastFetchedPage));
        if (searchQuery == null || searchQuery.trim().equals("")) {
            retrofit.create(FetchRecentPhotos.class).fetch(API_KEY, FORMAT, NOJSONCALLBACK, SAFESEARCH, EXTRAS,
                    FETCH_RECENT_METHOD, lastFetchedPage).enqueue(new Callback<List<GalleryItem>>() {
                @Override
                public void onResponse(Call<List<GalleryItem>> call, Response<List<GalleryItem>> response) {
                    if(lastFetchedPage > 1) {
                        int positionStart = items.size() + 1;
                        items.addAll(response.body());
                        if (items.size() - positionStart - 1 > 0)
                            mPhotoRecyclerView.getAdapter().notifyItemRangeInserted(positionStart, items.size());
                    } else {
                        items = response.body();
                        setupAdapter();
                        if(mProgressBar != null) {
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
                    SEARCH_METHOD, lastFetchedPage, SORTPHOTOS, searchQuery.trim()).enqueue(new Callback<List<GalleryItem>>() {
                @Override
                public void onResponse(Call<List<GalleryItem>> call, Response<List<GalleryItem>> response) {
                    if(lastFetchedPage > 1) {
                        int positionStart = items.size() + 1;
                        items.addAll(response.body());
                        if (items.size() - positionStart - 1 > 0)
                            mPhotoRecyclerView.getAdapter().notifyItemRangeInserted(positionStart, items.size());
                    } else {
                        items = response.body();
                        setupAdapter();
                        if(mProgressBar != null) {
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
        mProgressBar.setVisibility(View.VISIBLE);
        mPhotoRecyclerView.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mPhotoRecyclerView.setVisibility(View.VISIBLE);
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(items, getContext()));
        }
    }

}
