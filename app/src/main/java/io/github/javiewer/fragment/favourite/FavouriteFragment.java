package io.github.javiewer.fragment.favourite;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.github.javiewer.adapter.ItemAdapter;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.fragment.RecyclerFragment;

/**
 * Project: JAViewer
 */

public abstract class FavouriteFragment extends RecyclerFragment<Object, LinearLayoutManager> {

    public static final int SORT_RECENT = 0;
    public static final int SORT_NAME = 1;

    private String query = "";
    private int sortMode = SORT_RECENT;

    public void update() {
        applyFilter(query, sortMode);
    }

    @SuppressWarnings("unchecked")
    public void applyFilter(String query, int sortMode) {
        this.query = query == null ? "" : query;
        this.sortMode = sortMode;

        RecyclerView.Adapter adapter = getAdapter();
        if (!(adapter instanceof ItemAdapter)) {
            return;
        }

        List<?> source = source();
        ArrayList<Object> display = new ArrayList<>();
        String needle = this.query.trim().toLowerCase(Locale.ROOT);
        if (source != null) {
            for (Object item : source) {
                if (matches(item, needle)) {
                    display.add(item);
                }
            }
        }
        if (this.sortMode == SORT_NAME) {
            Collections.sort(display, new Comparator<Object>() {
                @Override
                public int compare(Object lhs, Object rhs) {
                    return sortKey(lhs).compareToIgnoreCase(sortKey(rhs));
                }
            });
        }
        ((ItemAdapter<Object, ?>) adapter).showItems(display);
    }

    private boolean matches(Object item, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            String title = movie.getTitle() != null ? movie.getTitle() : "";
            String code = movie.getCode() != null ? movie.getCode() : "";
            return title.toLowerCase(Locale.ROOT).contains(needle)
                    || code.toLowerCase(Locale.ROOT).contains(needle);
        }
        if (item instanceof Actress) {
            Actress actress = (Actress) item;
            String name = actress.getName() != null ? actress.getName() : "";
            return name.toLowerCase(Locale.ROOT).contains(needle);
        }
        return false;
    }

    private static String sortKey(Object item) {
        if (item instanceof Movie) {
            String code = ((Movie) item).getCode();
            return code != null ? code : "";
        }
        if (item instanceof Actress) {
            String name = ((Actress) item).getName();
            return name != null ? name : "";
        }
        return "";
    }

    public abstract List<?> source();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        this.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.setAdapter(adapter());
        //this.setAdapter(adapter =
        mRefreshLayout.setEnabled(false);

        if (decoration() != null) {
            mRecyclerView.addItemDecoration(decoration());
        }

        super.onActivityCreated(savedInstanceState);
        applyFilter(query, sortMode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public abstract ItemAdapter adapter();

    public RecyclerView.ItemDecoration decoration() {
        return null;
    }
}
