package io.github.javiewer.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.javiewer.JAViewer;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class MovieListFragment extends MovieFragment {

    public String keyword;
    public String action;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        this.keyword = bundle.getString("link");
        this.action = bundle.getString("action", "search");
    }

    @Override
    public Call<ResponseBody> newCall(int page) {
        if ("genre".equals(action) || "star".equals(action) || "studio".equals(action) || "director".equals(action) || "label".equals(action) || "series".equals(action)) {
            return JAViewer.SERVICE.getFilterMovies(Arrays.asList(action, keyword, "cn", 60, page));
        }
        Map<String, String> params = new LinkedHashMap<>();
        params.put("search", keyword);
        params.put("lang", "cn");
        return JAViewer.SERVICE.search(Arrays.<Object>asList(params, 60, page));
    }
}
