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
        return CiliInfo.get().search(keyword);
    }

    @Override
    public List<DownloadLink> parseDownloadLinks(String htmlContent) {
        ArrayList<DownloadLink> links = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent);
        Elements rows = doc.select("table.table-hover.file-list tbody tr");

        for (Element row : rows) {
            try {
                Element a = row.select("td a").first();
                Element sizeTd = row.select("td.td-size").first();

                if (a != null) {
                    String href = a.attr("href");
                    String title = a.text();
                    String size = sizeTd != null ? sizeTd.text() : "";

                    DownloadLink link = DownloadLink.create(
                            title,
                            size,
                            "",
                            href,
                            null
                    );
                    // Initialize empty files list to show expand indicator
                    link.setFiles(new ArrayList<>());
                    links.add(link);
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
            return CiliInfo.get().get(url);
        }
        return CiliInfo.get().get(CiliInfo.BASE_URL + url);
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

    public List<MagnetFile> parseFileList(String htmlContent) {
        ArrayList<MagnetFile> files = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent);
        // Only select the main file list table (with hover class), not the related resources table
        Elements rows = doc.select("table.table-hover.file-list tbody tr");

        for (Element row : rows) {
            try {
                MagnetFile file = new MagnetFile();
                file.filename = row.select("td").first().text();
                String sizeText = row.select("td.td-size").text();
                file.size = parseSize(sizeText);
                files.add(file);
            } catch (Exception ignored) {
            }
        }
        return files;
    }

    private long parseSize(String sizeText) {
        try {
            String text = sizeText.trim().toUpperCase();
            if (text.endsWith("GB")) {
                return (long) (Double.parseDouble(text.replace("GB", "").trim()) * 1073741824);
            } else if (text.endsWith("MB")) {
                return (long) (Double.parseDouble(text.replace("MB", "").trim()) * 1048576);
            } else if (text.endsWith("KB")) {
                return (long) (Double.parseDouble(text.replace("KB", "").trim()) * 1024);
            }
            return Long.parseLong(text.replace(",", "").replace(" B", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String parseDate(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        Elements dts = doc.select("dt");
        for (Element dt : dts) {
            if (dt.text().contains("发布日期")) {
                Element dd = dt.nextElementSibling();
                if (dd != null) {
                    String dateText = dd.text().trim();
                    // Extract just the date part (YYYY-MM-DD)
                    if (dateText.length() >= 10) {
                        return dateText.substring(0, 10);
                    }
                    return dateText;
                }
            }
        }
        return null;
    }
}
