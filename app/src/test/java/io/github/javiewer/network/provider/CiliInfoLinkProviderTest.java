package io.github.javiewer.network.provider;

import org.junit.Test;

import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 无极磁链（cili.info）搜索结果解析。
 *
 * <p>这些用例是为了钉住站点的 HTML 结构：站点改版过两次，每次都会让列表页的大小/日期
 * 悄悄变成空串（页面还能显示，只是每行只剩标题，很难发现）。
 */
public class CiliInfoLinkProviderTest {

    private final CiliInfoLinkProvider provider = new CiliInfoLinkProvider();

    /** 2026-09 实测抓到的结构：大小和日期都塞在 {@code td.result-meta} 里。 */
    private static final String CURRENT_MARKUP =
            "<table class=\"table-hover file-list\"><tbody>"
                    + "<tr>"
                    + "<td class=\"result-title\"><a href=\"/!lTsa\"><mark>SNOS</mark>-<mark>337</mark>-中文字幕</a></td>"
                    + "<td class=\"result-meta\"><div>1.76GB</div><div class=\"result-date\">2026-09-19</div></td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td class=\"result-title\"><a href=\"/!lTiZ\">SNOS-337 [HD]</a></td>"
                    + "<td class=\"result-meta\"><div>1.66GB</div><div class=\"result-date\">2026-09-18</div></td>"
                    + "</tr>"
                    + "</tbody></table>";

    @Test
    public void parsesTitleSizeAndDateFromCurrentMarkup() {
        List<DownloadLink> links = provider.parseDownloadLinks(CURRENT_MARKUP);

        assertEquals(2, links.size());

        DownloadLink first = links.get(0);
        assertEquals("SNOS-337-中文字幕", first.getTitle());
        assertEquals("/!lTsa", first.getLink());
        assertEquals("1.76 GB", first.getSize());
        assertEquals("2026-09-19", first.getDate());

        DownloadLink second = links.get(1);
        assertEquals("SNOS-337 [HD]", second.getTitle());
        assertEquals("1.66 GB", second.getSize());
        assertEquals("2026-09-18", second.getDate());
    }

    @Test
    public void initializesEmptyFileListSoExpandIndicatorShows() {
        List<DownloadLink> links = provider.parseDownloadLinks(CURRENT_MARKUP);

        assertNotNull(links.get(0).getFiles());
        assertTrue(links.get(0).getFiles().isEmpty());
    }

    /** 旧结构（{@code td.td-size}）作为兜底保留，防止站点回退后列表又变空。 */
    @Test
    public void stillParsesLegacyMarkup() {
        String legacy =
                "<table class=\"table-hover file-list\"><tbody>"
                        + "<tr><td><a href=\"/!abc\">旧结构标题</a></td>"
                        + "<td class=\"td-size\">2.5 GB</td></tr>"
                        + "</tbody></table>";

        List<DownloadLink> links = provider.parseDownloadLinks(legacy);

        assertEquals(1, links.size());
        assertEquals("旧结构标题", links.get(0).getTitle());
        assertEquals("2.5 GB", links.get(0).getSize());
        assertEquals("", links.get(0).getDate());
    }

    @Test
    public void handlesRowWithoutMetaCell() {
        String html =
                "<table class=\"table-hover file-list\"><tbody>"
                        + "<tr><td><a href=\"/!x\">只有标题</a></td></tr>"
                        + "</tbody></table>";

        List<DownloadLink> links = provider.parseDownloadLinks(html);

        assertEquals(1, links.size());
        assertEquals("只有标题", links.get(0).getTitle());
        assertEquals("", links.get(0).getSize());
        assertEquals("", links.get(0).getDate());
    }

    @Test
    public void skipsRowsWithoutLink() {
        String html =
                "<table class=\"table-hover file-list\"><tbody>"
                        + "<tr><td class=\"result-meta\"><div>1.0GB</div></td></tr>"
                        + "</tbody></table>";

        assertTrue(provider.parseDownloadLinks(html).isEmpty());
    }

    /** 大小文本统一成「数字 + 空格 + 单位」，与另外两个源的显示风格对齐。 */
    @Test
    public void normalizesSizeSpacingAndUnitCase() {
        String html =
                "<table class=\"table-hover file-list\"><tbody>"
                        + row("a", "1.76GB")
                        + row("b", "700 mb")
                        + row("c", "3GiB")
                        + row("d", "已经带了空格 2.00 GB")
                        + row("e", "不是大小")
                        + "</tbody></table>";

        List<DownloadLink> links = provider.parseDownloadLinks(html);

        assertEquals("1.76 GB", links.get(0).getSize());
        assertEquals("700 MB", links.get(1).getSize());
        assertEquals("3 GiB", links.get(2).getSize());
        assertEquals("已经带了空格 2.00 GB", links.get(3).getSize());
        assertEquals("不是大小", links.get(4).getSize());
    }

    private static String row(String href, String sizeText) {
        return "<tr><td class=\"result-title\"><a href=\"/!" + href + "\">t</a></td>"
                + "<td class=\"result-meta\"><div>" + sizeText + "</div>"
                + "<div class=\"result-date\">2026-09-19</div></td></tr>";
    }
}
