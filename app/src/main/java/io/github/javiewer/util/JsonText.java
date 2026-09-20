package io.github.javiewer.util;

/**
 * JSON 文本字段的读取收口。
 *
 * <p>Android 自带的 {@code org.json} 有个坑：字段在响应里显式为 {@code null} 时，
 * {@code optString(key, "")} 返回的**不是**空串，而是字符串 {@code "null"}。
 * 接口里 {@code hometown} / {@code hobby} 这类可选字段就经常是显式 null，
 * 不做处理会在界面上直接显示成「出生地 null · 爱好 null」。
 *
 * <p>而「null」永远不会是一个字段想表达的内容，所以这里统一当成空值处理。
 */
public final class JsonText {

    private JsonText() {
    }

    /**
     * 把 {@code null} 引用与字面量 {@code "null"} 都归一成空串，其余原样返回。
     */
    public static String clean(String value) {
        if (value == null || "null".equals(value)) {
            return "";
        }
        return value;
    }
}
