package io.github.javiewer.network;

import io.github.javiewer.JAViewer;
import okhttp3.OkHttpClient;
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

    static CiliInfo get() {
        return Holder.get(JAViewer.HTTP_CLIENT);
    }

    @GET("/search")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Referer: https://cili.info/",
            "Cache-Control: no-cache"
    })
    Call<ResponseBody> search(@retrofit2.http.Query("q") String keyword);

    @GET
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Referer: https://cili.info/",
            "Cache-Control: no-cache"
    })
    Call<ResponseBody> get(@Url String url);

    final class Holder {
        private static OkHttpClient client;
        private static CiliInfo instance;

        private Holder() {
        }

        static synchronized CiliInfo get(OkHttpClient httpClient) {
            if (instance == null || client != httpClient) {
                client = httpClient;
                instance = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(httpClient)
                        .build()
                        .create(CiliInfo.class);
            }
            return instance;
        }
    }
}
