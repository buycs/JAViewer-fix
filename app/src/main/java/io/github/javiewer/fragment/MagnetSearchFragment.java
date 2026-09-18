package io.github.javiewer.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.MagnetFileAdapter;
import io.github.javiewer.adapter.item.MagnetFile;
import io.github.javiewer.adapter.item.TorrentGroup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MagnetSearchFragment extends Fragment {

    private String keyword;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private SwipeRefreshLayout refreshLayout;
    private MagnetFileAdapter adapter;
    private List<TorrentGroup> groups = new ArrayList<>();

    private volatile boolean cancelled;
    private int searchGeneration;
    private Call searchCall;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyword = getArguments().getString("keyword");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cancelled = false;
        View view = inflater.inflate(R.layout.fragment_magnet_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyText = view.findViewById(R.id.empty_text);
        refreshLayout = view.findViewById(R.id.refresh_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MagnetFileAdapter(groups, getActivity());
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                groups.clear();
                adapter.notifyDataSetChanged();
                searchMagnets(keyword);
            }
        });

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (cancelled || !isAdded()) {
                    return;
                }
                refreshLayout.setRefreshing(true);
                searchMagnets(keyword);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        cancelled = true;
        searchGeneration++;
        if (searchCall != null) {
            searchCall.cancel();
            searchCall = null;
        }
        super.onDestroyView();
    }

    private void searchMagnets(final String code) {
        if (cancelled || !isAdded()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        emptyText.setVisibility(View.GONE);

        final int generation = ++searchGeneration;
        if (searchCall != null) {
            searchCall.cancel();
            searchCall = null;
        }

        try {
            JSONObject searchObj = new JSONObject();
            searchObj.put("search", code == null ? "" : code);
            JSONArray payload = new JSONArray();
            payload.put(searchObj);
            payload.put(30);
            payload.put(1);

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
            Request request = new Request.Builder()
                    .url(JAViewer.CONFIGURATIONS.getMagnetSourceBtsow() + "/bts/data/api/search")
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .build();
            searchCall = JAViewer.HTTP_CLIENT.newCall(request);
            searchCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (call.isCanceled()) {
                        return;
                    }
                    postSearchUi(generation, new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            refreshLayout.setRefreshing(false);
                            emptyText.setVisibility(View.VISIBLE);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.body() == null) {
                            throw new IOException("empty body");
                        }
                        if (!response.isSuccessful()) {
                            throw new IOException("http " + response.code());
                        }
                        String resultStr = response.body().string();
                        JSONObject obj = new JSONObject(resultStr);
                        JSONArray data = obj.optJSONArray("data");
                        if (data == null) {
                            data = obj.optJSONArray("list");
                        }
                        if (data == null) {
                            data = obj.optJSONArray("result");
                        }

                        if (data == null || data.length() == 0) {
                            postSearchUi(generation, new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    refreshLayout.setRefreshing(false);
                                    emptyText.setVisibility(View.VISIBLE);
                                }
                            });
                            return;
                        }

                        final List<TorrentGroup> allGroups = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            try {
                                JSONObject item = data.getJSONObject(i);
                                String hash = item.optString("hash", item.optString("infoHash", ""));
                                String torrentName = item.optString("name", item.optString("title", ""));
                                torrentName = torrentName.replaceAll("<[^>]+>", "").trim();
                                if (hash.isEmpty() || torrentName.isEmpty()) {
                                    continue;
                                }

                                TorrentGroup group = new TorrentGroup();
                                group.hash = hash;
                                group.torrentName = torrentName;
                                group.totalSize = item.optLong("size", item.optLong("fileSize", 0));
                                long timestamp = item.optLong("lastUpdateTime", item.optLong("time", 0));
                                if (timestamp > 0) {
                                    if (timestamp < 100000000000L) {
                                        timestamp *= 1000;
                                    }
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                                    group.date = sdf.format(new java.util.Date(timestamp));
                                }

                                MagnetFile mf = new MagnetFile();
                                mf.hash = hash;
                                mf.torrentName = torrentName;
                                mf.filename = torrentName;
                                mf.size = group.totalSize;
                                group.files.add(mf);
                                allGroups.add(group);
                            } catch (Exception ignored) {
                            }
                        }

                        postSearchUi(generation, new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                refreshLayout.setRefreshing(false);
                                if (allGroups.isEmpty()) {
                                    emptyText.setVisibility(View.VISIBLE);
                                } else {
                                    groups.addAll(allGroups);
                                    adapter.notifyDataSetChanged();
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    } catch (Exception e) {
                        android.util.Log.e("JAViewer", "btsow parse error", e);
                        postSearchUi(generation, new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                refreshLayout.setRefreshing(false);
                                emptyText.setVisibility(View.VISIBLE);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            postSearchUi(generation, new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    refreshLayout.setRefreshing(false);
                    emptyText.setVisibility(View.VISIBLE);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void postSearchUi(final int generation, final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (cancelled || generation != searchGeneration || !isAdded()) {
                    return;
                }
                runnable.run();
            }
        });
    }
}
