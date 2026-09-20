package io.github.javiewer.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MarkdownUtilTest {

    @Test
    public void emptyInputYieldsEmptyString() {
        assertEquals("", MarkdownUtil.toHtml(null));
        assertEquals("", MarkdownUtil.toHtml(""));
    }

    @Test
    public void convertsBoldAndListItems() {
        assertEquals("<b>v2.5.1</b><br/>•&nbsp;修复问题",
                MarkdownUtil.toHtml("**v2.5.1**\n- 修复问题"));
    }

    @Test
    public void convertsDashAndStarBullets() {
        assertEquals("•&nbsp;a<br/>•&nbsp;b", MarkdownUtil.toHtml("- a\n- b"));
        assertEquals("•&nbsp;项", MarkdownUtil.toHtml("* 项"));
        assertEquals("•&nbsp;项", MarkdownUtil.toHtml("+ 项"));
    }

    @Test
    public void blankLineBecomesExtraLineBreak() {
        assertEquals("<b>v1</b><br/><br/><b>v2</b>", MarkdownUtil.toHtml("**v1**\n\n**v2**"));
    }

    @Test
    public void normalizesCrlf() {
        assertEquals("a<br/>b", MarkdownUtil.toHtml("a\r\nb"));
        assertEquals("a<br/>b", MarkdownUtil.toHtml("a\rb"));
    }

    @Test
    public void escapesHtmlInPlainText() {
        assertEquals("&lt;b&gt;x&lt;/b&gt; &amp; &lt;script&gt;",
                MarkdownUtil.toHtml("<b>x</b> & <script>"));
    }

    @Test
    public void escapesHtmlInsideBold() {
        // 远端文本里的标签不能自己变成标签，粗体标记是唯一的标签来源
        assertEquals("<b>&lt;x&gt;</b>", MarkdownUtil.toHtml("**<x>**"));
    }

    @Test
    public void unmatchedBoldMarkerIsLeftLiteral() {
        assertEquals("a ** b", MarkdownUtil.toHtml("a ** b"));
        // 前一对是配对的，照常加粗；只有末尾孤立的 **x 保持原文
        assertEquals("<b>v1</b> 和 **x", MarkdownUtil.toHtml("**v1** 和 **x"));
    }

    @Test
    public void boldAtLineStartIsNotMistakenForBullet() {
        assertEquals("<b>v1</b><br/><b>v2</b>", MarkdownUtil.toHtml("**v1**\n**v2**"));
    }

    @Test
    public void dashWithoutFollowingSpaceIsLeftAlone() {
        assertEquals("-无空格", MarkdownUtil.toHtml("-无空格"));
        assertEquals("*斜体*", MarkdownUtil.toHtml("*斜体*"));
    }

    @Test
    public void convertsMultipleBoldsOnOneLine() {
        assertEquals("<b>a</b> 和 <b>b</b>", MarkdownUtil.toHtml("**a** 和 **b**"));
    }

    @Test
    public void handlesRealChangelogWithoutLeavingMarkers() {
        String changelog = "**v2.5.1**\n"
                + "- 修复「检查更新」始终提示「已是最新版本」的问题\n"
                + "- 检查更新改为联网比对远端版本信息，可正确发现新版本并跳转下载页\n"
                + "\n"
                + "**v2.5.0**\n"
                + "- 收藏导入导出，便于备份与迁移\n"
                + "- 搜索历史：记录、复用与一键清空";
        String html = MarkdownUtil.toHtml(changelog);
        assertTrue(html.contains("<b>v2.5.1</b>"));
        assertTrue(html.contains("<b>v2.5.0</b>"));
        assertTrue(html.contains("•&nbsp;收藏导入导出，便于备份与迁移"));
        assertFalse("不应该残留 Markdown 粗体标记", html.contains("**"));
    }
}
