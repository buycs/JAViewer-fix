package io.github.javiewer.network.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.network.TorrentKitty;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Project: JAViewer
 */
public class TorrentKittyLinkProvider extends DownloadLinkProvider {

    @Override
    public Call<ResponseBody> search(String keyword, int page) {
        return TorrentKitty.INSTANCE.search(keyword, page);
    }

    @Override
    public List<DownloadLink> parseDownloadLinks(String htmlContent) {
        ArrayList<DownloadLink> links = new ArrayList<>();
        Element table = Jsoup.parse(htmlContent).getElementById("archiveResult");
        if (table == null) {
            return links;
        }
        for (Element tr : table.getElementsByTag("tr")) {
            try {
                Element nameTd = tr.getElementsByClass("name").first();
                Element sizeTd = tr.getElementsByClass("size").first();
                Element dateTd = tr.getElementsByClass("date").first();
                Element infoLink = tr.getElementsByAttributeValue("rel", "information").first();

                if (nameTd != null && sizeTd != null && dateTd != null && infoLink != null) {
                    String infoUrl = infoLink.attr("href");
                    if (infoUrl.startsWith("/")) {
                        infoUrl = TorrentKitty.BASE_URL + infoUrl;
                    }
                    links.add(DownloadLink.create(
                            nameTd.text(),
                            sizeTd.text(),
                            dateTd.text(),
                            infoUrl,
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
        return TorrentKitty.INSTANCE.get(url);
    }

    @Override
    public MagnetLink parseMagnetLink(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        Element magnetLink = doc.getElementsByClass("magnet-link").first();
        if (magnetLink != null) {
            return MagnetLink.create(magnetLink.text());
        }
        return null;
    }
}
