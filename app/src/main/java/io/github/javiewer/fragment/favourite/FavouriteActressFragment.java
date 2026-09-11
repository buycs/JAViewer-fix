package io.github.javiewer.fragment.favourite;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.adapter.ActressAdapter;
import io.github.javiewer.adapter.ItemAdapter;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.view.decoration.ActressItemDecoration;

/**
 * Project: JAViewer
 */

public class FavouriteActressFragment extends FavouriteFragment {
    @Override
    public List<?> source() {
        if (JAViewer.CONFIGURATIONS == null) {
            return new ArrayList<>();
        }
        return JAViewer.CONFIGURATIONS.getStarredActresses();
    }

    @Override
    public ItemAdapter adapter() {
        return new ActressAdapter(new ArrayList<Actress>(), this.getActivity());
    }

    @Override
    public RecyclerView.ItemDecoration decoration() {
        return new ActressItemDecoration();
    }
}
