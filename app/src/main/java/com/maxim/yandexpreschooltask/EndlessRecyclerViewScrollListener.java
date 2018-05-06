package com.maxim.yandexpreschooltask;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;


public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    int firstVisibleItem, visibleItemCount, totalItemCount;
    private int previousTotal = 0; // Общее число элементов в наборе данных после последней загрузки
    private boolean loading = true; // true если идет загрузка
    private int visibleThreshold = 18; // Минимальное количество элементов, которое должно быть под текущей позицией скролла, для того чтобы начать загрузку
    private int current_page = 0;

    private GridLayoutManager gridLayoutManager;

    public EndlessRecyclerViewScrollListener(GridLayoutManager gridLayoutManager) {
        this.gridLayoutManager = gridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = gridLayoutManager.getItemCount();
        firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // Достигнут конец

            current_page++;

            onLoadMore(current_page);

            loading = true;
        }
    }

    public abstract void onLoadMore(int current_page);
}
