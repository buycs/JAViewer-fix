package io.github.javiewer.fragment;

import java.util.Arrays;

import io.github.javiewer.JAViewer;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class PopularFragment extends MovieFragment {
    @Override
    public Call<ResponseBody> newCall(int page) {
        return JAViewer.SERVICE.getFilterMovies(Arrays.asList("popular", "", "cn", 60, page));
    }
}
