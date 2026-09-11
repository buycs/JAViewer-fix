package io.github.javiewer.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.javiewer.JAViewer;
import io.github.javiewer.adapter.item.Movie;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class MovieListFragment extends MovieFragment {

    public String keyword;
    public String original;
    public String action;
    private boolean retriedOriginal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        this.keyword = bundle.getString("link");
        this.original = bundle.getString("original");
        this.action = bundle.getString("action", "search");
        if (savedInstanceState != null) {
            this.retriedOriginal = savedInstanceState.getBoolean("retriedOriginal", false);
            String savedKeyword = savedInstanceState.getString("keyword");
            if (savedKeyword != null) {
                this.keyword = savedKeyword;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("retriedOriginal", retriedOriginal);
        outState.putString("keyword", keyword);
    }

    @Override
    protected void onMoviesParsed(List<Movie> movies, boolean firstPage) {
        if (!firstPage || retriedOriginal) {
            return;
        }
        if (!"search".equals(action)) {
            return;
        }
        if (original == null || original.isEmpty()) {
            return;
        }
        if (keyword == null || original.equals(keyword)) {
            return;
        }
        if (movies != null && !movies.isEmpty()) {
            return;
        }
        retriedOriginal = true;
        keyword = original;
        if (getOnRefreshListener() != null && mRefreshLayout != null) {
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (getOnRefreshListener() == null || mRefreshLayout == null) {
                        return;
                    }
                    mRefreshLayout.setRefreshing(true);
                    getOnRefreshListener().onRefresh();
                }
            });
        }
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
