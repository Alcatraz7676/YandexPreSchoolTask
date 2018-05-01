package com.maxim.yandexpreschooltask;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.maxim.yandexpreschooltask.entities.GalleryItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

    private List<GalleryItem> mGalleryItems;
    private Context context;

    public PhotoAdapter(List<GalleryItem> items, Context context) {
        mGalleryItems = items;
        this.context = context;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
        return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoHolder photoHolder, int position) {
        GalleryItem galleryItem = mGalleryItems.get(position);
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
        return mGalleryItems.size();
    }

    public class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_photo_gallery_image_view)
        ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindGalleryItem(final GalleryItem galleryItem) {
            Picasso.with(context)
                    .load(galleryItem.getUrl())
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {
                            int size = Math.min(source.getWidth(), source.getHeight());
                            int x = (source.getWidth() - size) / 2;
                            int y = (source.getHeight() - size) / 2;
                            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
                            if (result != source) {
                                source.recycle();
                            }
                            return result;
                        }

                        @Override
                        public String key() {
                            return "square()";
                        }
                    })
                    .placeholder(R.drawable.placeholder)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mItemImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(context)
                                    .load(galleryItem.getUrl())
                                    .transform(new Transformation() {
                                        @Override
                                        public Bitmap transform(Bitmap source) {
                                            int size = Math.min(source.getWidth(), source.getHeight());
                                            int x = (source.getWidth() - size) / 2;
                                            int y = (source.getHeight() - size) / 2;
                                            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
                                            if (result != source) {
                                                source.recycle();
                                            }
                                            return result;
                                        }

                                        @Override
                                        public String key() {
                                            return "square()";
                                        }
                                    })
                                    .placeholder(R.drawable.placeholder)
                                    .into(mItemImageView);
                        }
                    });
        }
    }
}