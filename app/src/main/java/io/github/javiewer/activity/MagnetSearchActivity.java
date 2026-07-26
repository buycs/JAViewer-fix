package io.github.javiewer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.MagnetFileAdapter;
import io.github.javiewer.adapter.item.MagnetFile;
import io.github.javiewer.adapter.item.TorrentGroup;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MagnetSearchActivity extends SecureActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private MagnetFileAdapter adapter;
    private List<TorrentGroup> groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnet_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyText = findViewById(R.id.empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MagnetFileAdapter(groups, this);
        recyclerView.setAdapter(adapter);

        final String code = getIntent().getStringExtra("code");
        if (code == null || code.isEmpty()) {
            finish();
            return;
        }
        getSupportActionBar().setTitle(code + " 磁力搜索");

        searchMagnets(code);
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
                        // Extract date from lastUpdateTime (Unix timestamp)
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
                                        MagnetFile mf = new MagnetFile();
                                        mf.hash = hash;
                                        mf.torrentName = torrentName;
                                        mf.filename = f.getString("filename");
                                        mf.size = f.getLong("size");
                                        group.files.add(mf);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // ignore, show empty files
                        }

                        if (group.files.isEmpty()) {
                            MagnetFile mf = new MagnetFile();
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
                            emptyText.setVisibility(View.VISIBLE);
                            Toast.makeText(MagnetSearchActivity.this, "搜索失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
