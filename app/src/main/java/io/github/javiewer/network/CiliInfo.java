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

    static String currentBaseUrl() {
        if (JAViewer.CONFIGURATIONS == null) {
            return io.github.javiewer.Configurations.DEFAULT_MAGNET_SOURCE_CILI;
        }
        return JAViewer.CONFIGURATIONS.getMagnetSourceCili();
    }

    static CiliInfo get() {
        return Holder.get(JAViewer.HTTP_CLIENT, currentBaseUrl());
    }

    @GET("/search")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Cache-Control: no-cache"
    })
    Call<ResponseBody> search(@retrofit2.http.Query("q") String keyword);

    @GET
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Cache-Control: no-cache"
    })
    Call<ResponseBody> get(@Url String url);

    final class Holder {
        private static OkHttpClient client;
        private static String baseUrl;
        private static CiliInfo instance;

        private Holder() {
        }

        static synchronized CiliInfo get(OkHttpClient httpClient, String url) {
            if (instance == null || client != httpClient || !url.equals(baseUrl)) {
                client = httpClient;
                baseUrl = url;
                OkHttpClient ciliClient = httpClient.newBuilder()
                        .addInterceptor(chain -> chain.proceed(
                                chain.request().newBuilder()
                                        .header("Referer", url + "/")
                                        .build()))
                        .build();
                instance = new Retrofit.Builder()
                        .baseUrl(url)
                        .client(ciliClient)
                        .build()
                        .create(CiliInfo.class);
            }
            return instance;
        }
    }
}
