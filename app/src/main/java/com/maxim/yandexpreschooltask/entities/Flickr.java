package com.maxim.yandexpreschooltask.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Flickr {
    public Photos photos;

    public class Photos {
        @SerializedName("photo")
        public List<Photo> photos;

        public class Photo {
            private String id;
            public String getId() {
                return id;
            }
            public void setId(String id) {
                this.id = id;
            }
            private String owner;
            private String title;

            public String getOwner() {
                return owner;
            }

            public String getCaption() {
                return title;
            }

            public String getUrl() {
                return url;
            }

            @SerializedName("url_s")
            private String url;
        }
    }
}