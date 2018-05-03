package com.maxim.yandexpreschooltask;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.maxim.yandexpreschooltask.entities.GalleryItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

    private List<GalleryItem> items;
    private Context context;
    private OnPhotoClickListener listener;

    public PhotoAdapter(List<GalleryItem> items, Context context, OnPhotoClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
        return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoHolder photoHolder, int position) {
        GalleryItem galleryItem = items.get(position);
        photoHolder.bindGalleryItem(galleryItem);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_photo_gallery_image_view)
        ImageView itemImageView;

        View itemView;

        public PhotoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }

        public void bindGalleryItem(final GalleryItem galleryItem) {

            String url;

            if (galleryItem.getUrl() != null)
                url = galleryItem.getUrl();
            else if (galleryItem.getBigImageUrl() != null)
                url = galleryItem.getBigImageUrl();
            else
                return;

            GlideApp.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .fitCenter()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemImageView);

            itemView.setOnClickListener( itemView -> listener.onClick(galleryItem.getBigImageUrl()) );

        }
    }
}