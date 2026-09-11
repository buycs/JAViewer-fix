package io.github.javiewer.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.Movie;

public class FavouriteBackup {

    public List<Movie> movies;
    public List<Actress> actresses;

    public static class ImportResult {
        public final int moviesAdded;
        public final int actressesAdded;

        public ImportResult(int moviesAdded, int actressesAdded) {
            this.moviesAdded = moviesAdded;
            this.actressesAdded = actressesAdded;
        }
    }

    public static String toJson(List<Movie> movies, List<Actress> actresses) {
        FavouriteBackup backup = new FavouriteBackup();
        backup.movies = movies != null ? movies : new ArrayList<Movie>();
        backup.actresses = actresses != null ? actresses : new ArrayList<Actress>();
        return new Gson().toJson(backup);
    }

    public static ImportResult mergeJson(String json, List<Movie> destMovies, List<Actress> destActresses) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("empty backup");
        }
        FavouriteBackup backup;
        try {
            backup = new Gson().fromJson(json, FavouriteBackup.class);
        } catch (JsonSyntaxException e) {
            throw e;
        }
        if (backup == null) {
            throw new IllegalArgumentException("invalid backup");
        }
        if (destMovies == null || destActresses == null) {
            throw new IllegalArgumentException("destination is null");
        }

        int moviesAdded = 0;
        if (backup.movies != null) {
            for (Movie movie : backup.movies) {
                if (movie == null) {
                    continue;
                }
                try {
                    if (!destMovies.contains(movie)) {
                        destMovies.add(movie);
                        moviesAdded++;
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }

        int actressesAdded = 0;
        if (backup.actresses != null) {
            for (Actress actress : backup.actresses) {
                if (actress == null) {
                    continue;
                }
                try {
                    if (!destActresses.contains(actress)) {
                        destActresses.add(actress);
                        actressesAdded++;
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }

        return new ImportResult(moviesAdded, actressesAdded);
    }
}
