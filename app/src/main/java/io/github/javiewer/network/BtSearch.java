package io.github.javiewer.network;

import com.google.gson.annotations.SerializedName;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.javiewer.JAViewer;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BtSearch {

    String BASE_URL = "https://www.btsearch.love";
    String SECRET_KEY = "long2ice";

    OkHttpClient BTSEARCH_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(chain -> {
                Request original = chain.request();
                HttpUrl url = original.url();

                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

                Map<String, String> params = new HashMap<>();
                for (int i = 0; i < url.querySize(); i++) {
                    params.put(url.queryParameterName(i), url.queryParameterValue(i));
                }
                params.put("timestamp", timestamp);
                params.put("nonce", nonce);

                String sign = generateSign(params);

                Request.Builder builder = original.newBuilder()
                        .header("x-timestamp", timestamp)
                        .header("x-nonce", nonce)
                        .header("x-sign", sign)
                        .header("Accept", "application/json")
                        .header("User-Agent", JAViewer.USER_AGENT)
                        .header("Referer", BASE_URL + "/search");

                Request finalRequest = builder.build();
                if (io.github.javiewer.BuildConfig.DEBUG) {
                    android.util.Log.d("JAViewer", "BtSearch request: " + finalRequest.url());
                }

                return chain.proceed(finalRequest);
            })
            .build();

    BtSearch INSTANCE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(BTSEARCH_CLIENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BtSearch.class);

    @GET("/api/search")
    Call<SearchResult> search(
            @Query("keyword") String keyword,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("mode") String mode,
            @Query("time") String time,
            @Query("sort") String sort,
            @Query("sort_type") String sortType,
            @Query("size") String size
    );

    @GET("/api/torrent/{id}")
    Call<ResponseBody> getDetail(@Path("id") long id, @Query("keyword") String keyword);

    static String generateSign(Map<String, String> params) {
        List<String> sorted = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sorted.add(entry.getKey() + "=" + entry.getValue());
        }
        Collections.sort(sorted);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) sb.append("&");
            sb.append(sorted.get(i));
        }
        sb.append("&key=").append(SECRET_KEY);

        return md5(sb.toString()).toUpperCase();
    }

    static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    class SearchResult {
        public int total;
        public List<SearchItem> data;
        public long time_ms;
    }

    class SearchItem {
        public long id;
        public String name;
        public String size;
        @SerializedName("created_at")
        public String createdAt;
        public String hash;
        public int count;
        public int hot;
    }

    class TorrentDetail {
        public long id;
        public String name;
        public String size;
        public String hash;
        @SerializedName("created_at")
        public String createdAt;
        public List<TorrentFile> torrentfile;
    }

    class TorrentFile {
        public long id;
        public String name;
        public String size;
    }
}
