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

    /**
     * 取单个女优的完整资料，参数为 {@code [starId]}。
     * 与 {@link #getStars}（列表）不同，这个接口返回生日、三围、出生地等详情字段。
     */
    @POST("getStar")
    Call<ResponseBody> getStar(@Body List<Object> params);

    @POST("getGenres")
    Call<ResponseBody> getGenres(@Body List<Object> params);

    @POST("getFilterMovies")
    Call<ResponseBody> getFilterMovies(@Body List<Object> params);

    @POST("search")
    Call<ResponseBody> search(@Body List<Object> params);

    @POST("getRelatedMovies")
    Call<ResponseBody> getRelatedMovies(@Body List<Object> params);

}
