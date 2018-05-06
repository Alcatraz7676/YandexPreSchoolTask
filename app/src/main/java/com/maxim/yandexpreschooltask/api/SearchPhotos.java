package com.maxim.yandexpreschooltask.api;

import com.maxim.yandexpreschooltask.entities.GalleryItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchPhotos {
    @GET("/services/rest")
    Call<List<GalleryItem>> search(@Query("api_key") String apiKey,
                                   @Query("format") String format,
                                   @Query("nojsoncallback") int nojsoncallback,
                                   @Query("safe_search") int safeSearch,
                                   @Query("extras") String extras,
                                   @Query("method") String methodName,
                                   @Query("page") int pageNum,
                                   @Query("per_page") int perPage,
                                   @Query("sort") String sort,
                                   @Query("text") String text);
}
