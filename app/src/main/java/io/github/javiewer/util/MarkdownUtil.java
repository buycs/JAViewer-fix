package io.github.javiewer.util;

/**
 * 极简 Markdown → HTML 转换。
 *
 * <p>只覆盖应用内实际会用到的一小撮语法：{@code **粗体**}、行首 {@code -} / {@code *} / {@code +}
 * 列表项，以及换行。转换结果交给 {@code HtmlCompat.fromHtml} 渲染。
 *
 * <p>changelog 来自远端 {@code properties.json}，属于不可信输入，因此文本内容一律先做 HTML
 * 转义，只有本类自己产生的标签才会进入结果，避免远端文本里混入标签影响弹窗渲染。
 *
 * <p>刻意不依赖任何 Android API，纯字符串处理，方便在 JVM 上跑单元测试。
 */
public final class MarkdownUtil {

    /** 列表项替换成的符号（用 {@code &nbsp;} 保证与正文之间有可见间隔）。 */
    private static final String BULLET = "•&nbsp;";

    private MarkdownUtil() {
    }

    /**
     * 把极简 Markdown 转成可交给 {@code HtmlCompat.fromHtml} 的 HTML 片段。
     *
     * @param markdown 原始文本，可为 {@code null}
     * @return 转义并转换后的 HTML；输入为空时返回空串
     */
    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        String text = markdown.replace("\r\n", "\n").replace('\r', '\n');
        StringBuilder out = new StringBuilder(text.length() + 32);
        boolean atLineStart = true;
        int index = 0;
        while (index < text.length()) {
            char c = text.charAt(index);

            if (c == '\n') {
                out.append("<br/>");
                index++;
                atLineStart = true;
                continue;
            }

            if (atLineStart) {
                atLineStart = false;
                int bulletLength = bulletLength(text, index);
                if (bulletLength > 0) {
                    out.append(BULLET);
                    index += bulletLength;
                    continue;
                }
            }

            if (c == '*' && index + 1 < text.length() && text.charAt(index + 1) == '*') {
                int end = text.indexOf("**", index + 2);
                if (end > index + 2) {
                    out.append("<b>");
                    appendEscaped(out, text, index + 2, end);
                    out.append("</b>");
                    index = end + 2;
                    continue;
                }
            }

            appendEscaped(out, c);
            index++;
        }
        return out.toString();
    }

    /**
     * 判断 {@code from} 处是否是一个列表项标记，是则返回「标记 + 其后空白」的总长度。
     *
     * <p>{@code *} 开头时要与 {@code **粗体**} 区分开：后跟 {@code *} 视为粗体，后跟空白才算列表项。
     */
    private static int bulletLength(String text, int from) {
        int index = from;
        while (index < text.length() && (text.charAt(index) == ' ' || text.charAt(index) == '\t')) {
            index++;
        }
        if (index >= text.length()) {
            return 0;
        }
        char marker = text.charAt(index);
        if (marker == '*') {
            if (index + 1 >= text.length() || text.charAt(index + 1) == '*') {
                return 0;
            }
        } else if (marker != '-' && marker != '+') {
            return 0;
        }
        index++;
        if (index >= text.length() || text.charAt(index) != ' ') {
            return 0;
        }
        while (index < text.length() && (text.charAt(index) == ' ' || text.charAt(index) == '\t')) {
            index++;
        }
        return index - from;
    }

    private static void appendEscaped(StringBuilder out, String text, int from, int to) {
        for (int i = from; i < to; i++) {
            appendEscaped(out, text.charAt(i));
        }
    }

    private static void appendEscaped(StringBuilder out, char c) {
        switch (c) {
            case '&':
                out.append("&amp;");
                break;
            case '<':
                out.append("&lt;");
                break;
            case '>':
                out.append("&gt;");
                break;
            default:
                out.append(c);
                break;
        }
    }
}
