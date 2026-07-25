package io.github.javiewer.network;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BasicService {

    @POST("getMovies")
    Call<ResponseBody> getMovies(@Body List<Object> params);

    @POST("getMovie")
    Call<ResponseBody> getMovie(@Body List<Object> params);

    @POST("getStars")
    Call<ResponseBody> getStars(@Body List<Object> params);

    @POST("getGenres")
    Call<ResponseBody> getGenres(@Body List<Object> params);

    @POST("getFilterMovies")
    Call<ResponseBody> getFilterMovies(@Body List<Object> params);

    @POST("search")
    Call<ResponseBody> search(@Body List<Object> params);

    @POST("getRelatedMovies")
    Call<ResponseBody> getRelatedMovies(@Body List<Object> params);

}
