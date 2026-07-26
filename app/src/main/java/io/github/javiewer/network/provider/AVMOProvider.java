package io.github.javiewer.network.provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.adapter.item.MovieDetail;
import io.github.javiewer.adapter.item.Screenshot;

public class AVMOProvider {

    public static List<Movie> parseMovies(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONArray data = obj.getJSONArray("data");

        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            movies.add(
                    Movie.create(
                            item.optString("title", ""),
                            item.optString("movieFanHao", ""),
                            item.optString("releaseDate", ""),
                            item.optString("posterSmall", ""),
                            item.optString("movieId", ""),
                            false
                    )
            );
        }

        return movies;
    }

    public static List<Actress> parseActresses(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONArray data = obj.getJSONArray("data");

        List<Actress> actresses = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            actresses.add(
                    Actress.create(
                            item.optString("starName", ""),
                            item.optString("avatarUrl", ""),
                            item.optString("starId", "")
                    )
            );
        }

        return actresses;
    }

    public static MovieDetail parseMoviesDetail(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONObject data = obj.getJSONObject("data");

        MovieDetail movie = new MovieDetail();

        movie.title = data.optString("title", "");
        movie.code = data.optString("movieFanHao", "");
        movie.btsSearchUrl = data.optString("btsSearchUrl", "");
        movie.coverUrl = data.optString("posterLarge", "");

        JSONArray sampleSmall = data.optJSONArray("sampleSmall");
        if (sampleSmall != null) {
            for (int i = 0; i < sampleSmall.length(); i++) {
                String url = sampleSmall.getString(i);
                String largeUrl = data.optJSONArray("sampleLarge") != null && i < data.optJSONArray("sampleLarge").length()
                        ? data.optJSONArray("sampleLarge").getString(i) : url;
                movie.screenshots.add(
                        Screenshot.create(
                                url,
                                largeUrl
                        )
                );
            }
        }

        JSONArray actresses = data.optJSONArray("star");
        if (actresses != null) {
            for (int i = 0; i < actresses.length(); i++) {
                JSONObject a = actresses.getJSONObject(i);
                movie.actresses.add(
                        Actress.create(
                                a.optString("starName", ""),
                                a.optString("avatarUrl", ""),
                                a.optString("starId", "")
                        )
                );
            }
        }

        {
            String date = data.optString("releaseDate", "");
            if (!date.isEmpty()) {
                movie.headers.add(MovieDetail.Header.create("发行日期", date, null));
            }
            int length = data.optInt("length", 0);
            if (length > 0) {
                movie.headers.add(MovieDetail.Header.create("时长", length + " 分钟", null));
            }
            JSONObject director = data.optJSONObject("director");
            if (director != null) {
                String name = director.optString("directorName", "");
                if (!name.isEmpty()) {
                    movie.headers.add(MovieDetail.Header.create("导演", name, director.optString("directorId", "")));
                }
            }
            JSONObject studio = data.optJSONObject("studio");
            if (studio != null) {
                String name = studio.optString("studioName", "");
                if (!name.isEmpty()) {
                    movie.headers.add(MovieDetail.Header.create("制作商", name, studio.optString("studioId", "")));
                }
            }
            JSONObject label = data.optJSONObject("label");
            if (label != null) {
                String name = label.optString("labelName", "");
                if (!name.isEmpty()) {
                    movie.headers.add(MovieDetail.Header.create("发行商", name, label.optString("labelId", "")));
                }
            }
            JSONObject series = data.optJSONObject("series");
            if (series != null) {
                String seriesName = series.optString("seriesName", "");
                String seriesId = series.optString("seriesId", "");
                if (!seriesId.isEmpty()) {
                    movie.headers.add(MovieDetail.Header.create("系列", seriesName.isEmpty() ? "-" : seriesName, seriesId));
                }
            } else {
                String seriesId = data.optString("seriesId", "");
                if (!seriesId.isEmpty()) {
                    movie.headers.add(MovieDetail.Header.create("系列", "-", seriesId));
                }
            }
        }

        JSONArray genres = data.optJSONArray("genre");
        if (genres != null) {
            for (int i = 0; i < genres.length(); i++) {
                JSONObject g = genres.getJSONObject(i);
                movie.genres.add(
                        Genre.create(
                                g.optString("genreName", ""),
                                g.optString("genreId", "")
                        )
                );
            }
        }

        return movie;
    }

    public static LinkedHashMap<String, List<Genre>> parseGenres(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        LinkedHashMap<String, List<Genre>> map = new LinkedHashMap<>();
        List<Genre> others = null;

        try {
            JSONObject data = obj.getJSONObject("data");
            String[] order = {"0","1","2","3","4","5","6","7"};
            for (String key : order) {
                if (data.has(key)) {
                    JSONArray arr = data.getJSONArray(key);
                    List<Genre> genres = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject g = arr.getJSONObject(i);
                        genres.add(Genre.create(
                                g.optString("genreName", ""),
                                g.optString("genreId", "")
                        ));
                    }
                    if (!genres.isEmpty()) {
                        String label;
                        switch (key) {
                            case "0": label = "热门类型"; break;
                            case "1": label = "职业扮演"; break;
                            case "2": label = "衣着造型"; break;
                            case "3": label = "身材特征"; break;
                            case "4": label = "性爱玩法"; break;
                            case "5": label = "道具调教"; break;
                            case "6": label = "制作系列"; break;
                            case "7": label = "AV OPEN"; break;
                            default: label = key; break;
                        }
                        map.put(label, genres);
                    }
                }
            }
            if (data.has("-1")) {
                JSONArray arr = data.getJSONArray("-1");
                others = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject g = arr.getJSONObject(i);
                    others.add(Genre.create(g.optString("genreName", ""), g.optString("genreId", "")));
                }
            }
        } catch (JSONException e) {
            JSONArray data = obj.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONArray group = data.getJSONArray(i);
                if (group.length() == 0) continue;
                JSONObject first = group.getJSONObject(0);
                int type = first.optInt("type", -1);
                if (type == -1) {
                    others = new ArrayList<>();
                    for (int j = 0; j < group.length(); j++) {
                        JSONObject g = group.getJSONObject(j);
                        others.add(Genre.create(g.optString("genreName", ""), g.optString("genreId", "")));
                    }
                    continue;
                }
                if (type == 7) {
                    // merge into 其他 for javu/wav
                    if (others == null) others = new ArrayList<>();
                    for (int j = 0; j < group.length(); j++) {
                        JSONObject g = group.getJSONObject(j);
                        others.add(Genre.create(g.optString("genreName", ""), g.optString("genreId", "")));
                    }
                    continue;
                }
                String label;
                switch (String.valueOf(type)) {
                    case "0": label = "热门类型"; break;
                    case "1": label = "职业扮演"; break;
                    case "2": label = "衣着造型"; break;
                    case "3": label = "身材特征"; break;
                    case "4": label = "性爱玩法"; break;
                    case "5": label = "道具调教"; break;
                    case "6": label = "制作系列"; break;
                    case "7": label = "AV OPEN"; break;
                    default: label = String.valueOf(type); break;
                }
                List<Genre> list = new ArrayList<>();
                for (int j = 0; j < group.length(); j++) {
                    JSONObject g = group.getJSONObject(j);
                    list.add(Genre.create(g.optString("genreName", ""), g.optString("genreId", "")));
                }
                map.put(label, list);
            }
        }

        if (others != null && !others.isEmpty()) {
            map.put("其他", others);
        }

        return map;
    }
}
