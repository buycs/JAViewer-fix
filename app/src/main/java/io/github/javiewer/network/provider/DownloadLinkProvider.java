package io.github.javiewer.network.provider;

import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetFile;
import io.github.javiewer.adapter.item.MagnetLink;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Project: JAViewer
 */
public abstract class DownloadLinkProvider {
    public static DownloadLinkProvider getProvider(String name) {
        switch (name.toLowerCase().trim()) {
            case "btsearch":
                return new BtSearchLinkProvider();
            case "ciliinfo":
            case "cili":
                return new CiliInfoLinkProvider();
            default:
                return null;
        }
    }

    public abstract Call<ResponseBody> search(String keyword, int page);

    public abstract List<DownloadLink> parseDownloadLinks(String htmlContent);

    public abstract Call<ResponseBody> get(String url);

    public abstract MagnetLink parseMagnetLink(String htmlContent);

    public List<MagnetFile> parseFileList(String htmlContent) {
        return null;
    }

    public String parseDate(String htmlContent) {
        return null;
    }
}
