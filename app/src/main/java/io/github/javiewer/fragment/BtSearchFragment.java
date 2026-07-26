package io.github.javiewer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.R;
import io.github.javiewer.adapter.DownloadLinkAdapter;
import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.network.BtSearch;
import io.github.javiewer.network.provider.BtSearchLinkProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BtSearchFragment extends Fragment {

    private String keyword;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private DownloadLinkAdapter adapter;
    private List<DownloadLink> items = new ArrayList<>();
    private BtSearchLinkProvider provider = new BtSearchLinkProvider();
    private int currentPage = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyword = getArguments().getString("keyword");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        refreshLayout = view.findViewById(R.id.refresh_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DownloadLinkAdapter(items, getActivity(), provider, keyword);
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                items.clear();
                adapter.notifyDataSetChanged();
                loadData();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!refreshLayout.isRefreshing() && lm.findLastVisibleItemPosition() >= items.size() - 3) {
                    loadData();
                }
            }
        });

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                loadData();
            }
        });

        return view;
    }

    private void loadData() {
        Call<BtSearch.SearchResult> call = provider.searchApi(keyword, currentPage);
        if (call == null) {
            refreshLayout.setRefreshing(false);
            return;
        }

        call.enqueue(new Callback<BtSearch.SearchResult>() {
            @Override
            public void onResponse(Call<BtSearch.SearchResult> call, Response<BtSearch.SearchResult> response) {
                refreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<DownloadLink> newItems = provider.parseSearchResult(response.body());
                    int pos = items.size();
                    items.addAll(newItems);
                    adapter.notifyItemRangeInserted(pos, newItems.size());
                    currentPage++;
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BtSearch.SearchResult> call, Throwable t) {
                refreshLayout.setRefreshing(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
