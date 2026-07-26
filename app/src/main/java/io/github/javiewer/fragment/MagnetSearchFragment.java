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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.MagnetFileAdapter;
import io.github.javiewer.adapter.item.TorrentGroup;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MagnetSearchFragment extends Fragment {

    private String keyword;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private SwipeRefreshLayout refreshLayout;
    private MagnetFileAdapter adapter;
    private List<TorrentGroup> groups = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyword = getArguments().getString("keyword");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                refreshLayout.setRefreshing(true);
                searchMagnets(keyword);
            }
        });

        return view;
    }

    private void searchMagnets(final String code) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = "[{\"search\":\"" + code + "\"},30,1]";
                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                    Request request = new Request.Builder()
                            .url("https://btsow.pics/bts/data/api/search")
                            .post(body)
                            .addHeader("content-type", "application/json")
                            .build();
                    okhttp3.Response response = JAViewer.HTTP_CLIENT.newCall(request).execute();
                    String resultStr = response.body().string();

                    JSONObject obj = new JSONObject(resultStr);
                    JSONArray data = obj.optJSONArray("data");

                    if (data == null || data.length() == 0) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
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
                    final int total = data.length();
                    final AtomicInteger completed = new AtomicInteger(0);

                    for (int i = 0; i < total; i++) {
                        JSONObject item = data.getJSONObject(i);
                        final String hash = item.getString("hash");
                        final String torrentName = item.getString("name").replaceAll("<[^>]+>", "");

                        final TorrentGroup group = new TorrentGroup();
                        group.hash = hash;
                        group.torrentName = torrentName;
                        group.totalSize = item.getLong("size");
                        if (item.has("lastUpdateTime")) {
                            long timestamp = item.getLong("lastUpdateTime");
                            if (timestamp > 0) {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                                group.date = sdf.format(new java.util.Date(timestamp * 1000));
                            }
                        }

                        try {
                            String json2 = "[\"" + hash + "\"]";
                            RequestBody body2 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json2);
                            Request request2 = new Request.Builder()
                                    .url("https://btsow.pics/bts/data/api/magnet")
                                    .post(body2)
                                    .addHeader("content-type", "application/json")
                                    .build();
                            okhttp3.Response response2 = JAViewer.HTTP_CLIENT.newCall(request2).execute();
                            String resultStr2 = response2.body().string();

                            JSONObject obj2 = new JSONObject(resultStr2);
                            JSONObject data2 = obj2.optJSONObject("data");
                            if (data2 != null) {
                                JSONArray filesArr = data2.optJSONArray("files");
                                if (filesArr != null && filesArr.length() > 0) {
                                    for (int j = 0; j < filesArr.length(); j++) {
                                        JSONObject f = filesArr.getJSONObject(j);
                                        io.github.javiewer.adapter.item.MagnetFile mf = new io.github.javiewer.adapter.item.MagnetFile();
                                        mf.hash = hash;
                                        mf.torrentName = torrentName;
                                        mf.filename = f.getString("filename");
                                        mf.size = f.getLong("size");
                                        group.files.add(mf);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // ignore
                        }

                        if (group.files.isEmpty()) {
                            io.github.javiewer.adapter.item.MagnetFile mf = new io.github.javiewer.adapter.item.MagnetFile();
                            mf.hash = hash;
                            mf.torrentName = torrentName;
                            mf.filename = torrentName;
                            mf.size = group.totalSize;
                            group.files.add(mf);
                        }

                        allGroups.add(group);

                        int done = completed.incrementAndGet();
                        if (done == total) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
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
                        }
                    }
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            refreshLayout.setRefreshing(false);
                            emptyText.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
