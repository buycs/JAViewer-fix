package io.github.javiewer.view.listener;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.activity.FavouriteActivity;
import io.github.javiewer.adapter.item.Movie;

public class MovieLongClickListener implements View.OnLongClickListener {

    private Activity mActivity;
    private Movie movie;

    public MovieLongClickListener(Movie movie, Activity mActivity) {
        this.movie = movie;
        this.mActivity = mActivity;
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        String[] items;
        final List<Movie> movies = JAViewer.CONFIGURATIONS.getStarredMovies();
        final boolean contain = movies.contains(movie);
        if (contain) {
            items = new String[]{"复制番号", "取消收藏"};
        } else {
            items = new String[]{"复制番号", "收藏"};
        }
        builder.setTitle(movie.getTitle())
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                ClipboardManager clip = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                                clip.setPrimaryClip(ClipData.newPlainText("movie_code", movie.getCode()));
                                Toast.makeText(mActivity, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            case 1: {
                                if (contain) {
                                    movies.remove(movie);
                                    Toast.makeText(mActivity, "已取消收藏", Toast.LENGTH_SHORT).show();
                                } else {
                                    Collections.reverse(movies);
                                    movies.add(movie);
                                    Collections.reverse(movies);
                                    Toast.makeText(mActivity, "已收藏", Toast.LENGTH_SHORT).show();
                                }
                                JAViewer.CONFIGURATIONS.save();
                                FavouriteActivity.update();
                                break;
                            }
                        }
                    }
                });
        builder.create().show();
        return true;
    }
}
