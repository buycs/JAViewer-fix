package io.github.javiewer.network.provider;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
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
        return BtSearch.INSTANCE.search(keyword, limit, offset, "", "", "", "asc", "");
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
                String infoUrl = BtSearch.BASE_URL + "/torrent/" + item.id;

                links.add(DownloadLink.create(
                        name,
                        size,
                        item.createdAt != null ? item.createdAt.substring(0, 10) : "",
                        infoUrl,
                        magnetLink
                ));
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    public Call<BtSearch.TorrentDetail> getDetail(long id, String keyword) {
        return BtSearch.INSTANCE.getDetail(id, keyword);
    }

    public List<DownloadLink> parseDetailResult(BtSearch.TorrentDetail detail) {
        ArrayList<DownloadLink> links = new ArrayList<>();
        if (detail == null) {
            return links;
        }
        String magnetLink = "magnet:?xt=urn:btih:" + detail.hash;
        String size = formatSize(Long.parseLong(detail.size));
        links.add(DownloadLink.create(
                detail.name,
                size,
                detail.createdAt != null ? detail.createdAt.substring(0, 10) : "",
                null,
                magnetLink
        ));
        return links;
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
