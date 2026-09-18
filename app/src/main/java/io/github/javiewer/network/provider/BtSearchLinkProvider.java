package io.github.javiewer.network.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetFile;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.network.BtSearch;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class BtSearchLinkProvider extends DownloadLinkProvider {

    private int currentPage = 0;

    @Override
    public Call<ResponseBody> search(String keyword, int page) {
        currentPage = page;
        return null;
    }

    @Override
    public List<DownloadLink> parseDownloadLinks(String htmlContent) {
        return new ArrayList<>();
    }

    public Call<BtSearch.SearchResult> searchApi(String keyword, int page) {
        int limit = 10;
        int offset = (page - 1) * limit;
        return BtSearch.get().search(keyword, limit, offset, "", "", "", "asc", "");
    }

    public List<DownloadLink> parseSearchResult(BtSearch.SearchResult result) {
        ArrayList<DownloadLink> links = new ArrayList<>();
        if (result == null || result.data == null) {
            return links;
        }
        for (BtSearch.SearchItem item : result.data) {
            try {
                String name = item.name.replaceAll("<[^>]+>", "");
                String size = formatSize(Long.parseLong(item.size));
                String magnetLink = "magnet:?xt=urn:btih:" + item.hash;
                String infoUrl = BtSearch.currentBaseUrl() + "/torrent/" + item.id;

                DownloadLink link = DownloadLink.create(
                        name,
                        size,
                        item.createdAt != null ? item.createdAt.substring(0, 10) : "",
                        infoUrl,
                        magnetLink
                );
                // Initialize empty files list to show expand indicator
                link.setFiles(new ArrayList<>());
                links.add(link);
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    public Call<ResponseBody> getDetail(long id, String keyword) {
        return BtSearch.get().getDetail(id, keyword);
    }

    public List<MagnetFile> parseFilesFromJson(String jsonContent) {
        ArrayList<MagnetFile> files = new ArrayList<>();
        try {
            org.json.JSONObject obj = new org.json.JSONObject(jsonContent);
            org.json.JSONArray arr = obj.optJSONArray("torrentfile");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject item = arr.getJSONObject(i);
                    MagnetFile file = new MagnetFile();
                    file.filename = item.getString("name");
                    file.size = item.getLong("size");
                    files.add(file);
                }
            }
        } catch (Exception e) {
            // Not JSON or parsing failed
        }
        return files;
    }

    public List<MagnetFile> parseFilesFromHtml(String htmlContent) {
        ArrayList<MagnetFile> files = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(htmlContent);

            // Log HTML length for debugging
            android.util.Log.d("JAViewer", "BtSearch HTML length: " + htmlContent.length());

            // Method 1: Find torrentfile in script tags
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String text = script.html();
                if (text.contains("torrentfile")) {
                    android.util.Log.d("JAViewer", "Found torrentfile in script tag");
                    // Try to extract JSON array
                    int start = text.indexOf("[{");
                    int end = text.indexOf("}]") + 2;
                    if (start >= 0 && end > start) {
                        String json = text.substring(start, end);
                        android.util.Log.d("JAViewer", "torrentfile JSON: " + json.substring(0, Math.min(200, json.length())));
                        org.json.JSONArray arr = new org.json.JSONArray(json);
                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject obj = arr.getJSONObject(i);
                            MagnetFile file = new MagnetFile();
                            file.filename = obj.getString("name");
                            file.size = obj.getLong("size");
                            files.add(file);
                        }
                    }
                    break;
                }
            }

            // Method 2: Look for file table
            if (files.isEmpty()) {
                android.util.Log.d("JAViewer", "Trying to find file table in HTML");
                Elements tables = doc.select("table");
                android.util.Log.d("JAViewer", "Found " + tables.size() + " tables");
                for (Element table : tables) {
                    android.util.Log.d("JAViewer", "Table classes: " + table.className());
                    Elements rows = table.select("tr");
                    if (rows.size() > 1) {
                        android.util.Log.d("JAViewer", "Table has " + rows.size() + " rows");
                        for (Element row : rows) {
                            android.util.Log.d("JAViewer", "Row: " + row.text().substring(0, Math.min(100, row.text().length())));
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("JAViewer", "parseFilesFromHtml error: " + e.getMessage());
        }
        return files;
    }

    @Override
    public Call<ResponseBody> get(String url) {
        return null;
    }

    @Override
    public MagnetLink parseMagnetLink(String htmlContent) {
        return null;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
