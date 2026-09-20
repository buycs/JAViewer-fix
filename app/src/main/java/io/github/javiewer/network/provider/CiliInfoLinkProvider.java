package io.github.javiewer.network.provider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /** 匹配 {@code 1.76GB} / {@code 700 MB} 这类大小文本。 */
    private static final Pattern SIZE_PATTERN =
            Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)\\s*([KMGT]i?B)$", Pattern.CASE_INSENSITIVE);

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
                if (a == null) {
                    continue;
                }
                String href = a.attr("href");
                String title = a.text();
                String size = parseListSize(row);
                String date = parseListDate(row);

                DownloadLink link = DownloadLink.create(
                        title,
                        size,
                        date,
                        href,
                        null
                );
                // Initialize empty files list to show expand indicator
                link.setFiles(new ArrayList<>());
                links.add(link);
            } catch (Exception ignored) {
            }
        }
        return links;
    }

    /**
     * 列表页的日期。
     *
     * <p>站点改版后日期在 {@code td.result-meta .result-date} 里，直接就能拿到，
     * 不必像以前那样等展开时才去详情页抓。
     */
    private String parseListDate(Element row) {
        Element dateEl = row.select("td.result-meta .result-date").first();
        if (dateEl == null) {
            dateEl = row.select(".result-date").first();
        }
        return dateEl != null ? dateEl.text().trim() : "";
    }

    /**
     * 列表页的大小。
     *
     * <p>站点改版后大小不再是 {@code td.td-size}，而是 {@code td.result-meta} 下第一个
     * 非日期的 {@code div}（形如 {@code 1.76GB}）。旧标记仍作兜底，防止站点回退。
     */
    private String parseListSize(Element row) {
        Element meta = row.select("td.result-meta").first();
        if (meta != null) {
            for (Element div : meta.select("div")) {
                if (!div.hasClass("result-date")) {
                    return normalizeSize(div.text());
                }
            }
            // 没有子 div 时退回整个单元格，但要把日期摘掉
            String text = meta.text().trim();
            String date = parseListDate(row);
            if (!date.isEmpty()) {
                text = text.replace(date, "").trim();
            }
            return normalizeSize(text);
        }
        Element sizeTd = row.select("td.td-size").first();
        return sizeTd != null ? normalizeSize(sizeTd.text()) : "";
    }

    /**
     * 把 {@code 1.76GB} 规整成 {@code 1.76 GB}。
     *
     * <p>另外两个源的大小都带空格（如 {@code 6.96 GB}），这里统一一下，避免同一列表里
     * 三列文字的间距观感不一致。
     */
    private static String normalizeSize(String size) {
        if (size == null) {
            return "";
        }
        String text = size.trim();
        if (text.isEmpty()) {
            return "";
        }
        Matcher matcher = SIZE_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return text;
        }
        String unit = matcher.group(2);
        String normalizedUnit = unit.substring(0, 1).toUpperCase(Locale.US)
                + (unit.length() == 3 ? "iB" : "B");
        return matcher.group(1) + " " + normalizedUnit;
    }

    @Override
    public Call<ResponseBody> get(String url) {
        // url format: /!lBfm or https://cili.info/!lBfm
        if (url.startsWith("http")) {
            return CiliInfo.get().get(url);
        }
        return CiliInfo.get().get(CiliInfo.currentBaseUrl() + url);
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
