package io.github.javiewer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.adapter.item.Movie;

/**
 * Project: JAViewer
 */

public class Configurations {

    public static final String DEFAULT_MAGNET_SOURCE_BTSEARCH = "https://www.btsearch.love";
    public static final String DEFAULT_MAGNET_SOURCE_CILI = "https://cili.info";
    public static final String DEFAULT_MAGNET_SOURCE_BTSOW = "https://btsow.live";

    private static File file;

    private ArrayList<Movie> starred_movies;

    private ArrayList<Actress> starred_actresses;

    private ArrayList<String> search_history;

    private DataSource data_source;

    private Map<String, String> data_source_domains;

    private String magnet_source_btsearch;

    private String magnet_source_cili;

    private String magnet_source_btsow;

    private String theme_mode;

    private boolean hide_recent_preview;

    public static Configurations load(File file) {
        Configurations.file = file;
        Configurations config = parseFile(file, StandardCharsets.UTF_8);
        if (config == null) {
            Charset fallback = Charset.defaultCharset();
            if (!StandardCharsets.UTF_8.equals(fallback)) {
                config = parseFile(file, fallback);
            }
        }
        if (config == null) {
            config = new Configurations();
        }
        return config;
    }

    private static Configurations parseFile(File file, Charset charset) {
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int offset = 0;
            while (offset < bytes.length) {
                int read = in.read(bytes, offset, bytes.length - offset);
                if (read < 0) {
                    break;
                }
                offset += read;
            }
            String json = new String(bytes, 0, offset, charset);
            if (json.indexOf('\uFFFD') >= 0) {
                return null;
            }
            return JAViewer.parseJson(Configurations.class, new JsonReader(new StringReader(json)));
        } catch (Exception ignored) {
            return null;
        }
    }

    public ArrayList<Movie> getStarredMovies() {
        if (starred_movies == null) {
            starred_movies = new ArrayList<>();
        }
        return starred_movies;
    }

    public ArrayList<Actress> getStarredActresses() {
        if (starred_actresses == null) {
            starred_actresses = new ArrayList<>();
        }
        return starred_actresses;
    }

    public ArrayList<String> getSearchHistory() {
        if (search_history == null) {
            search_history = new ArrayList<>();
        }
        return search_history;
    }

    public void addSearchHistory(String q) {
        if (q == null) {
            return;
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        ArrayList<String> history = getSearchHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            String existing = history.get(i);
            if (existing != null && existing.equalsIgnoreCase(trimmed)) {
                history.remove(i);
            }
        }
        history.add(0, trimmed);
        while (history.size() > 20) {
            history.remove(history.size() - 1);
        }
    }

    public void clearSearchHistory() {
        getSearchHistory().clear();
    }

    public DataSource getDataSource() {
        if (data_source == null || data_source.domain == null) {
            if (data_source != null && data_source.domain == null && data_source.link != null) {
                for (DataSource ds : JAViewer.DATA_SOURCES) {
                    if (data_source.link.equals(ds.getLink())) {
                        data_source = ds;
                        return data_source;
                    }
                }
            }
            data_source = JAViewer.DATA_SOURCES.get(0);
        }
        return data_source;
    }

    public void setDataSource(DataSource source) {
        this.data_source = source;
    }

    public Map<String, String> getDataSourceDomains() {
        if (data_source_domains == null) {
            data_source_domains = new HashMap<>();
        }
        return data_source_domains;
    }

    public void applyDataSourceDomains(List<DataSource> sources) {
        if (data_source_domains == null) {
            return;
        }
        for (DataSource source : sources) {
            String domain = data_source_domains.get(source.getName());
            if (domain != null && !domain.trim().isEmpty()) {
                source.domain = domain;
            }
        }
    }

    public String getMagnetSourceBtsearch() {
        return normalizeMagnetSource(magnet_source_btsearch, DEFAULT_MAGNET_SOURCE_BTSEARCH);
    }

    public void setMagnetSourceBtsearch(String source) {
        this.magnet_source_btsearch = source;
    }

    public String getMagnetSourceCili() {
        return normalizeMagnetSource(magnet_source_cili, DEFAULT_MAGNET_SOURCE_CILI);
    }

    public void setMagnetSourceCili(String source) {
        this.magnet_source_cili = source;
    }

    public String getMagnetSourceBtsow() {
        return normalizeMagnetSource(magnet_source_btsow, DEFAULT_MAGNET_SOURCE_BTSOW);
    }

    public void setMagnetSourceBtsow(String source) {
        this.magnet_source_btsow = source;
    }

    private static String normalizeMagnetSource(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public String getThemeMode() {
        if (theme_mode == null) {
            return "system";
        }
        switch (theme_mode) {
            case "light":
            case "dark":
            case "system":
                return theme_mode;
            default:
                return "system";
        }
    }

    public void setThemeMode(String themeMode) {
        this.theme_mode = themeMode;
    }

    public boolean isHideRecentPreview() {
        return hide_recent_preview;
    }

    public void setHideRecentPreview(boolean hideRecentPreview) {
        this.hide_recent_preview = hideRecentPreview;
    }

    public synchronized void save() {
        if (file == null) {
            return;
        }
        File tmp = new File(file.getAbsolutePath() + ".tmp");
        File bak = new File(file.getAbsolutePath() + ".bak");
        try {
            try (FileOutputStream fos = new FileOutputStream(tmp);
                 OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                new Gson().toJson(this, writer);
                writer.flush();
                fos.getFD().sync();
            }
            if (tmp.renameTo(file)) {
                return;
            }
            if (bak.exists() && !bak.delete()) {
                tmp.delete();
                return;
            }
            if (file.exists() && !file.renameTo(bak)) {
                tmp.delete();
                return;
            }
            if (!tmp.renameTo(file)) {
                if (bak.exists()) {
                    bak.renameTo(file);
                }
                tmp.delete();
                return;
            }
            bak.delete();
        } catch (IOException e) {
            e.printStackTrace();
            if (tmp.exists()) {
                tmp.delete();
            }
        }
    }
}
