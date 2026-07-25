package io.github.javiewer.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.github.javiewer.JAViewer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BtSearch {

    String BASE_URL = "https://www.btsearch.love";
    BtSearch INSTANCE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(JAViewer.HTTP_CLIENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BtSearch.class);

    @GET("/api/search")
    @Headers({
            "Accept: application/json",
            "Accept-Language: zh-CN,zh;q=0.9",
            "Referer: https://www.btsearch.love/search"
    })
    Call<SearchResult> search(
            @Query("keyword") String keyword,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("/torrent/{id}")
    @Headers({
            "Accept: application/json",
            "Accept-Language: zh-CN,zh;q=0.9"
    })
    Call<TorrentDetail> getDetail(@Path("id") long id, @Query("keyword") String keyword);

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
