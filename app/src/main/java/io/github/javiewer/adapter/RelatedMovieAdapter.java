package io.github.javiewer.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.github.javiewer.R;
import io.github.javiewer.activity.MovieActivity;
import io.github.javiewer.adapter.item.Movie;

public class RelatedMovieAdapter extends RecyclerView.Adapter<RelatedMovieAdapter.ViewHolder> {

    private List<Movie> movies;
    private Activity mParentActivity;

    public RelatedMovieAdapter(List<Movie> movies, Activity mParentActivity) {
        this.movies = movies;
        this.mParentActivity = mParentActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_movie_simple, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Movie movie = movies.get(position);

        holder.mTitle.setText(movie.getTitle());
        holder.mCode.setText(movie.getCode());
        Glide.with(holder.mImage.getContext().getApplicationContext())
                .load(movie.getCoverUrl())
                .into(holder.mImage);

        holder.mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParentActivity, MovieActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("movie", movie);
                intent.putExtras(bundle);
                mParentActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImage;
        public TextView mTitle;
        public TextView mCode;
        public CardView mCard;

        public ViewHolder(View view) {
            super(view);
            mImage = view.findViewById(R.id.movie_cover);
            mTitle = view.findViewById(R.id.movie_title);
            mCode = view.findViewById(R.id.movie_code);
            mCard = view.findViewById(R.id.card_movie);
        }
    }
}
