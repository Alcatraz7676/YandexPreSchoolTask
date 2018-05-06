package com.maxim.yandexpreschooltask.api;

import com.maxim.yandexpreschooltask.entities.GalleryItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FetchRecentPhotos {
    @GET("/services/rest")
    Call<List<GalleryItem>> fetch(@Query("api_key") String apiKey,
                                  @Query("format") String format,
                                  @Query("nojsoncallback") int nojsoncallback,
                                  @Query("safe_search") int safeSearch,
                                  @Query("extras") String extras,
                                  @Query("method") String methodName,
                                  @Query("page") int pageNum,
                                  @Query("per_page") int perPage);
}
