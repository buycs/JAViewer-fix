package io.github.javiewer.network.provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.ActressDetail;
import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.adapter.item.MovieDetail;
import io.github.javiewer.adapter.item.Screenshot;
import io.github.javiewer.util.GenreLabels;
import io.github.javiewer.util.JsonText;

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
                            item.optString("starId", ""),
                            item.optInt("movieCount", 0)
                    )
            );
        }

        return actresses;
    }

    /**
     * 解析 {@code getStar} 的响应，得到单个女优的完整资料。
     *
     * <p>三个数据源的字段丰俭不同：骑兵有生日 / 血型 / 三围，步兵与欧美没有，
     * 且它们的 {@code size} 是空数组而非对象（{@code optJSONObject} 会返回 null，已做保护）。
     * 可选字段还可能是显式 null，必须走 {@link JsonText#clean}，否则界面上会出现「出生地 null」。
     *
     * @return 解析结果；响应里没有 data（例如 starId 不存在）时返回 null
     */
    public static ActressDetail parseStarDetail(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONObject data = obj.optJSONObject("data");
        if (data == null) {
            return null;
        }

        ActressDetail detail = new ActressDetail();
        detail.name = optText(data, "starName");
        detail.avatarUrl = optText(data, "avatarUrl");
        detail.movieCount = data.optInt("movieCount", 0);
        detail.downloadMovieCount = data.optInt("downloadMovieCount", 0);
        detail.birthday = optText(data, "birthday");
        detail.constellation = data.optInt("constellation", 0);
        detail.bloodType = optText(data, "bloodType");
        detail.hometown = optText(data, "hometown");
        detail.hobby = optText(data, "hobby");
        detail.lastReleaseDate = optText(data, "lastReleaseDate");

        JSONObject size = data.optJSONObject("size");
        if (size != null) {
            detail.height = optText(size, "T");
            detail.bust = optText(size, "B");
            detail.cup = optText(size, "C");
            detail.waist = optText(size, "W");
            detail.hip = optText(size, "H");
        }

        return detail;
    }

    /** 读文本字段，并把显式 null 归一成空串。 */
    private static String optText(JSONObject obj, String key) {
        return JsonText.clean(obj.optString(key, ""));
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
                movie.headers.add(MovieDetail.Header.create("影片时长", length + " 分钟", null));
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
                if (!seriesId.isEmpty() && !seriesName.isEmpty()) {
                    movie.headers.add(MovieDetail.Header.create("系列", seriesName, seriesId));
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

    /**
     * 解析类别列表。
     *
     * <p>两个站点族的响应结构不同：骑兵的 {@code data} 是 dict（key 就是 {@code type}），
     * 步兵与欧美是 list of list（下标即 {@code type}）；两者元素里都带 {@code type} 字段，
     * 且与 key / 下标一致。
     *
     * <p>{@code type} 0~6 各自成组，{@code 7} 与骑兵的 {@code -1} 都是站点定义的「其他」，
     * 合并到最后一组，避免同一个 {@code type} 在三个数据源上落到不同分组。
     *
     * @param groupLabels 分组名，由 {@link GenreLabels} 按当前数据源给出，长度需覆盖 0~7
     */
    public static LinkedHashMap<String, List<Genre>> parseGenres(String json, String[] groupLabels)
            throws Exception {
        JSONObject obj = new JSONObject(json);
        LinkedHashMap<String, List<Genre>> map = new LinkedHashMap<>();
        List<Genre> others = null;

        Object raw = obj.get("data");
        if (raw instanceof JSONObject) {
            JSONObject data = (JSONObject) raw;
            for (int type = 0; type < GenreLabels.OTHER_INDEX; type++) {
                String key = String.valueOf(type);
                if (!data.has(key)) {
                    continue;
                }
                List<Genre> genres = parseGenreArray(data.getJSONArray(key));
                if (!genres.isEmpty()) {
                    map.put(GenreLabels.labelAt(groupLabels, type), genres);
                }
            }
            others = new ArrayList<>();
            for (String key : new String[]{"-1", "7"}) {
                if (data.has(key)) {
                    others.addAll(parseGenreArray(data.getJSONArray(key)));
                }
            }
        } else {
            JSONArray data = (JSONArray) raw;
            for (int i = 0; i < data.length(); i++) {
                JSONArray group = data.getJSONArray(i);
                if (group.length() == 0) {
                    continue;
                }
                int type = group.getJSONObject(0).optInt("type", GenreLabels.OTHER_INDEX);
                List<Genre> genres = parseGenreArray(group);
                if (genres.isEmpty()) {
                    continue;
                }
                if (GenreLabels.isOther(type)) {
                    if (others == null) {
                        others = new ArrayList<>();
                    }
                    others.addAll(genres);
                } else {
                    map.put(GenreLabels.labelAt(groupLabels, type), genres);
                }
            }
        }

        if (others != null && !others.isEmpty()) {
            map.put(GenreLabels.labelAt(groupLabels, GenreLabels.OTHER_INDEX), others);
        }

        return map;
    }

    private static List<Genre> parseGenreArray(JSONArray arr) throws JSONException {
        List<Genre> genres = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject g = arr.getJSONObject(i);
            genres.add(
                    Genre.create(
                            JsonText.clean(g.optString("genreName", "")),
                            g.optString("genreId", "")
                    )
            );
        }
        return genres;
    }
}
