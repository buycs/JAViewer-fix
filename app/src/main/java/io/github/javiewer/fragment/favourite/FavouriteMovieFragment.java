package io.github.javiewer.fragment.favourite;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.adapter.ItemAdapter;
import io.github.javiewer.adapter.MovieAdapter;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.view.decoration.MovieItemDecoration;

/**
 * Project: JAViewer
 */

public class FavouriteMovieFragment extends FavouriteFragment {
    @Override
    public List<?> source() {
        if (JAViewer.CONFIGURATIONS == null) {
            return new ArrayList<>();
        }
        return JAViewer.CONFIGURATIONS.getStarredMovies();
    }

    @Override
    public ItemAdapter adapter() {
        return new MovieAdapter(new ArrayList<Movie>(), this.getActivity()) {{
            showIfHot = false;
        }};
    }

    @Override
    public RecyclerView.ItemDecoration decoration() {
        return new MovieItemDecoration();
    }
}
