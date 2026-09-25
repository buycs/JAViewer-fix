package io.github.javiewer.view.listener;

import android.app.Activity;
import android.view.View;

import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.util.CopyStarDialog;

public class MovieLongClickListener implements View.OnLongClickListener {

    private Activity mActivity;
    private Movie movie;

    public MovieLongClickListener(Movie movie, Activity mActivity) {
        this.movie = movie;
        this.mActivity = mActivity;
    }

    @Override
    public boolean onLongClick(View v) {
        final List<Movie> movies = JAViewer.CONFIGURATIONS.getStarredMovies();
        CopyStarDialog.show(mActivity, movie.getTitle(), movie, movies,
                new CopyStarDialog.TextProvider<Movie>() {
                    @Override
                    public String text(Movie item) {
                        return item.getCode();
                    }
                }, "movie_code");
        return true;
    }
}
