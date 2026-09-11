package io.github.javiewer.view.listener;

import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Project: JAViewer
 */

public abstract class BasicOnScrollListener<I> extends RecyclerView.OnScrollListener {

    private boolean loading = false;

    private int loadThreshold = 5;
    private int currentPage = 0;

    private long token;
    private boolean end = false;
    private Call<ResponseBody> currentCall;


    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        token = System.currentTimeMillis();
    }

    public void reset() {
        cancel();
        loading = false;
        loadThreshold = 5;
        currentPage = 0;
        end = false;
        int oldSize = getItems().size();
        if (oldSize > 0) {
            getItems().clear();
            getAdapter().notifyItemRangeRemoved(0, oldSize);
        }
    }

    public Bundle saveState() {
        Bundle bundle = new Bundle();
        bundle.putInt("CurrentPage", currentPage);
        bundle.putBoolean("End", end);
        return bundle;
    }

    public void restoreState(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        currentPage = bundle.getInt("CurrentPage");
        end = bundle.getBoolean("End");
    }

    public abstract RecyclerView.LayoutManager getLayoutManager();

    public abstract SwipeRefreshLayout getRefreshLayout();

    public abstract List<I> getItems();

    public abstract RecyclerView.Adapter getAdapter();

    public abstract Call<ResponseBody> newCall(int page);

    public void refresh() {
        reset();
        setLoading(true);
        onLoad(token = System.currentTimeMillis());
    }

    private void onLoad(final long token) {
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }

        final int page = currentPage;
        Call<ResponseBody> call = newCall(page + 1);
        currentCall = call;

        if (call == null) {
            setLoading(false);
            getRefreshLayout().setRefreshing(false);
            return;
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                android.util.Log.d("JAViewer", "onResponse code: " + response.code() + " url: " + call.request().url());
                if (token != BasicOnScrollListener.this.token || page != currentPage) {
                    return;
                }
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        onResult(response.body());
                        currentPage++;
                    } else {
                        android.util.Log.w("JAViewer", "Response not successful: " + response.code());
                        setEnd(true);
                    }
                } catch (Throwable e) {
                    android.util.Log.e("JAViewer", "onResult error: " + e.getMessage(), e);
                    onFailure(call, e);
                    return;
                }

                setLoading(false);
                SwipeRefreshLayout refreshLayout = getRefreshLayout();
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled() || token != BasicOnScrollListener.this.token) {
                    return;
                }
                android.util.Log.e("JAViewer", "onFailure: " + t.getMessage() + " url: " + call.request().url());
                setLoading(false);
                SwipeRefreshLayout refreshLayout = getRefreshLayout();
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                onExceptionCaught(t);
            }
        });
    }

    public void onExceptionCaught(Throwable t) {

    }

    public void onResult(ResponseBody response) throws Exception {

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (!isLoading() && canLoadMore(recyclerView)) {
            onLoad(token = System.currentTimeMillis());
            loading = true;
        }
    }

    public boolean canLoadMore(RecyclerView recyclerView) {
        RecyclerView.LayoutManager mLayoutManager = getLayoutManager();
        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();
        int firstVisibleItem = 0;
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            firstVisibleItem = ((StaggeredGridLayoutManager) mLayoutManager).findFirstVisibleItemPositions(null)[0];
        } else if (mLayoutManager instanceof GridLayoutManager) {
            firstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        }

        return !isEnd() && (totalItemCount - visibleItemCount) <= (firstVisibleItem + this.loadThreshold);
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
