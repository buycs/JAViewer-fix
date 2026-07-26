package io.github.javiewer.network;

import io.github.javiewer.JAViewer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

/**
 * Project: JAViewer
 */
public interface CiliInfo {

    String BASE_URL = "https://cili.info";
    CiliInfo INSTANCE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(JAViewer.HTTP_CLIENT)
            .build()
            .create(CiliInfo.class);

    @GET("/search")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Referer: https://cili.info/"
    })
    Call<ResponseBody> search(@retrofit2.http.Query("q") String keyword);

    @GET
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Referer: https://cili.info/"
    })
    Call<ResponseBody> get(@Url String url);
}
