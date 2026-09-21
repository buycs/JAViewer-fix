package io.github.javiewer.fragment.genre;


import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.ViewPagerAdapter;
import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.fragment.ExtendedAppBarFragment;
import io.github.javiewer.network.provider.AVMOProvider;
import io.github.javiewer.util.GenreLabels;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenreTabsFragment extends ExtendedAppBarFragment {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public TabLayout mTabLayout;
    public ViewPager mViewPager;
    public ProgressBar mProgressBar;
    public ViewPagerAdapter mAdapter;

    private Call<ResponseBody> genresCall;
    private volatile boolean cancelled;
    private final android.os.Handler retryHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public GenreTabsFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        android.util.Log.d("GenreTabs", "onActivityCreated");

        cancelled = false;
        mAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        loadGenres(0);
    }

    private void loadGenres(final int attempt) {
        if (cancelled || !isAdded() || getActivity() == null) {
            return;
        }
        if (genresCall != null) {
            genresCall.cancel();
        }
        // 站点按 lang 返回类别名，这里固定 cn，与分组标签用同一套字典（见 groupLabels()）。
        Call<ResponseBody> call = JAViewer.SERVICE.getGenres(Arrays.asList("cn"));
        genresCall = call;
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (cancelled || !isAdded() || getActivity() == null) {
                    return;
                }
                try {
                    if (response.body() == null) {
                        retry(attempt);
                        return;
                    }
                    String body = response.body().string();
                    android.util.Log.d("GenreTabs", "Response body: " + body.substring(0, Math.min(500, body.length())));
                    LinkedHashMap<String, List<Genre>> genres = AVMOProvider.parseGenres(body, groupLabels());

                    if (genres.isEmpty()) {
                        retry(attempt);
                        return;
                    }

                    mProgressBar.setVisibility(View.GONE);
                    GenreFragment fragment;
                    for (String title : genres.keySet()) {
                        fragment = new GenreFragment();
                        fragment.getGenres().addAll(genres.get(title));
                        mAdapter.addFragment(fragment, title);
                    }

                    mAdapter.notifyDataSetChanged();

                    mTabLayout.setVisibility(View.VISIBLE);
                } catch (Throwable e) {
                    retry(attempt);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled() || cancelled || !isAdded()) {
                    return;
                }
                t.printStackTrace();
                retry(attempt);
            }
        });
    }

    /**
     * 分组标签跟随当前数据源。站点按 cn 字典给出分组名，骑兵的 type 7 是 AV OPEN 专题，
     * 其余两源按站点语义叫「其他」。
     */
    private GenreLabels groupLabels() {
        try {
            return GenreLabels.forApiPath(JAViewer.getDataSource().apiPath);
        } catch (Throwable e) {
            return GenreLabels.JAV;
        }
    }

    private void retry(final int attempt) {
        if (cancelled || !isAdded() || getActivity() == null) {
            return;
        }
        if (attempt < MAX_RETRY) {
            retryHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (cancelled || !isAdded()) {
                        return;
                    }
                    loadGenres(attempt + 1);
                }
            }, RETRY_DELAY_MS);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        cancelled = true;
        retryHandler.removeCallbacksAndMessages(null);
        if (genresCall != null) {
            genresCall.cancel();
            genresCall = null;
        }
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genre, container, false);
        mTabLayout = view.findViewById(R.id.genre_tabs);
        mViewPager = view.findViewById(R.id.genre_view_pager);
        mProgressBar = view.findViewById(R.id.genre_progress_bar);
        return view;
    }
}
