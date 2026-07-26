package io.github.javiewer.network.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.network.CiliInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Project: JAViewer
 * cili.info (无极磁链) provider
 */
public class CiliInfoLinkProvider extends DownloadLinkProvider {

    @Override
    public Call<ResponseBody> search(String keyword, int page) {
        return CiliInfo.INSTANCE.search(keyword);
    }

    @Override
    public List<DownloadLink> parseDownloadLinks(String htmlContent) {
        ArrayList<DownloadLink> links = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent);
        Elements rows = doc.select("table.file-list tbody tr");

        for (Element row : rows) {
            try {
                Element a = row.select("td a").first();
                Element sizeTd = row.select("td.td-size").first();

                if (a != null) {
                    String href = a.attr("href");
                    String title = a.text();
                    String size = sizeTd != null ? sizeTd.text() : "";

                    // href format: /!lBfm
                    links.add(DownloadLink.create(
                            title,
                            size,
                            "",
                            href,
                            null
                    ));
                }
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    @Override
    public Call<ResponseBody> get(String url) {
        // url format: /!lBfm or https://cili.info/!lBfm
        if (url.startsWith("http")) {
            return CiliInfo.INSTANCE.get(url);
        }
        return CiliInfo.INSTANCE.get(CiliInfo.BASE_URL + url);
    }

    @Override
    public MagnetLink parseMagnetLink(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);

        // magnet link is in #input-magnet input field
        Element input = doc.getElementById("input-magnet");
        if (input != null) {
            String magnet = input.attr("value");
            if (magnet != null && !magnet.isEmpty()) {
                return MagnetLink.create(magnet);
            }
        }

        return null;
    }
}
