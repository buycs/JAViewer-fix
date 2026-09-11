package io.github.javiewer.util;

import org.junit.Test;

import java.util.ArrayList;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.Movie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FavouriteBackupTest {

    @Test
    public void exportThenMergeAddsOnlyNewItems() {
        ArrayList<Movie> movies = new ArrayList<>();
        ArrayList<Actress> actresses = new ArrayList<>();
        movies.add(Movie.create("已有", "ABC-001", "2020-01-01", null, "http://m1", false));
        actresses.add(Actress.create("已有女优", null, "http://a1"));

        ArrayList<Movie> incomingMovies = new ArrayList<>();
        ArrayList<Actress> incomingActresses = new ArrayList<>();
        incomingMovies.add(Movie.create("已有", "ABC-001", "2020-01-01", null, "http://m1", false));
        incomingMovies.add(Movie.create("新增", "ABC-002", "2021-01-01", null, "http://m2", false));
        incomingActresses.add(Actress.create("已有女优", null, "http://a1"));
        incomingActresses.add(Actress.create("新女优", null, "http://a2"));

        String json = FavouriteBackup.toJson(incomingMovies, incomingActresses);
        assertTrue(json.contains("\"movies\""));
        assertTrue(json.contains("\"actresses\""));

        FavouriteBackup.ImportResult result = FavouriteBackup.mergeJson(json, movies, actresses);
        assertEquals(1, result.moviesAdded);
        assertEquals(1, result.actressesAdded);
        assertEquals(2, movies.size());
        assertEquals(2, actresses.size());
        assertEquals("ABC-001", movies.get(0).getCode());
        assertEquals("ABC-002", movies.get(1).getCode());
        assertEquals("已有女优", actresses.get(0).getName());
        assertEquals("新女优", actresses.get(1).getName());
    }

    @Test
    public void mergeDoesNotRemoveExisting() {
        ArrayList<Movie> movies = new ArrayList<>();
        ArrayList<Actress> actresses = new ArrayList<>();
        movies.add(Movie.create("本地", "LOCAL-1", null, null, "http://local", false));
        actresses.add(Actress.create("本地女优", null, "http://local-a"));

        String json = FavouriteBackup.toJson(
                java.util.Collections.singletonList(Movie.create("导入", "IMP-1", null, null, "http://imp", false)),
                java.util.Collections.singletonList(Actress.create("导入女优", null, "http://imp-a")));

        FavouriteBackup.ImportResult result = FavouriteBackup.mergeJson(json, movies, actresses);
        assertEquals(1, result.moviesAdded);
        assertEquals(1, result.actressesAdded);
        assertEquals(2, movies.size());
        assertEquals("LOCAL-1", movies.get(0).getCode());
        assertEquals("IMP-1", movies.get(1).getCode());
        assertEquals(2, actresses.size());
        assertEquals("本地女优", actresses.get(0).getName());
    }

    @Test
    public void mergeEmptyListsAddsNothing() {
        ArrayList<Movie> movies = new ArrayList<>();
        ArrayList<Actress> actresses = new ArrayList<>();
        movies.add(Movie.create("本地", "LOCAL-1", null, null, "http://local", false));

        FavouriteBackup.ImportResult result = FavouriteBackup.mergeJson("{\"movies\":[],\"actresses\":[]}", movies, actresses);
        assertEquals(0, result.moviesAdded);
        assertEquals(0, result.actressesAdded);
        assertEquals(1, movies.size());
    }
}
