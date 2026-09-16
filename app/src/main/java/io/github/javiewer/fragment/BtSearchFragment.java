package io.github.javiewer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    private TextView emptyText;
    private DownloadLinkAdapter adapter;
    private List<DownloadLink> items = new ArrayList<>();
    private BtSearchLinkProvider provider = new BtSearchLinkProvider();
    private int currentPage = 1;
    private boolean loading = false;
    private boolean ended = false;
    private Call<BtSearch.SearchResult> searchCall;

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
        emptyText = view.findViewById(R.id.empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DownloadLinkAdapter(items, getActivity(), provider, keyword);
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (searchCall != null) {
                    searchCall.cancel();
                    searchCall = null;
                }
                loading = false;
                ended = false;
                currentPage = 1;
                items.clear();
                adapter.notifyDataSetChanged();
                showEmptyMessage(false);
                loadData();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!loading && !ended && !refreshLayout.isRefreshing()
                        && !items.isEmpty()
                        && lm.findLastVisibleItemPosition() >= items.size() - 3) {
                    loadData();
                }
            }
        });

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                refreshLayout.setRefreshing(true);
                loadData();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        if (searchCall != null) {
            searchCall.cancel();
            searchCall = null;
        }
        loading = false;
        super.onDestroyView();
    }

    private void loadData() {
        if (loading || ended) {
            return;
        }
        loading = true;

        Call<BtSearch.SearchResult> previous = searchCall;
        Call<BtSearch.SearchResult> call = provider.searchApi(keyword, currentPage);
        searchCall = call;
        if (previous != null) {
            previous.cancel();
        }
        if (call == null) {
            loading = false;
            refreshLayout.setRefreshing(false);
            return;
        }

        call.enqueue(new Callback<BtSearch.SearchResult>() {
            @Override
            public void onResponse(Call<BtSearch.SearchResult> call, Response<BtSearch.SearchResult> response) {
                if (searchCall != call) {
                    return;
                }
                searchCall = null;
                loading = false;
                if (!isAdded()) {
                    return;
                }
                refreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<DownloadLink> newItems = provider.parseSearchResult(response.body());
                    if (newItems == null || newItems.isEmpty()) {
                        ended = true;
                        if (items.isEmpty()) {
                            showEmptyMessage(true);
                        }
                        return;
                    }
                    int pos = items.size();
                    items.addAll(newItems);
                    adapter.notifyItemRangeInserted(pos, newItems.size());
                    currentPage++;
                    showEmptyMessage(false);
                } else if (getContext() != null) {
                    if (items.isEmpty()) {
                        showEmptyMessage(true);
                    }
                    Toast.makeText(getContext(), "搜索失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BtSearch.SearchResult> call, Throwable t) {
                if (searchCall != call) {
                    return;
                }
                searchCall = null;
                loading = false;
                if (call.isCanceled() || !isAdded()) {
                    return;
                }
                refreshLayout.setRefreshing(false);
                if (items.isEmpty()) {
                    showEmptyMessage(true);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showEmptyMessage(boolean empty) {
        if (emptyText != null) {
            emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
