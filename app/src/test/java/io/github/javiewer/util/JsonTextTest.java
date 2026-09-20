package io.github.javiewer.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 读取 JSON 文本字段时的 null 收口。
 *
 * <p>起因是一个线上实测出来的 bug：Android 的 {@code org.json} 在字段显式为 null 时，
 * {@code optString(key, "")} 返回的是字符串 {@code "null"}，
 * 于是「女优的作品」页信息栏直接显示成了「出生地 null · 爱好 null」。
 */
public class JsonTextTest {

    /** 这就是 bug 的形态：optString 的返回值是四个字母的 "null"。 */
    @Test
    public void literalNullStringBecomesEmpty() {
        assertEquals("", JsonText.clean("null"));
    }

    @Test
    public void nullReferenceBecomesEmpty() {
        assertEquals("", JsonText.clean(null));
    }

    /** 正常值原样保留，别把名字里恰好含 null 的字段误伤。 */
    @Test
    public void ordinaryValuesPassThrough() {
        assertEquals("京都府", JsonText.clean("京都府"));
        assertEquals("ゲーム", JsonText.clean("ゲーム"));
        assertEquals("1988-05-24", JsonText.clean("1988-05-24"));
        assertEquals("0", JsonText.clean("0"));
    }

    /** 只有整个字段等于 "null" 才算空，含 null 的正常文本不动。 */
    @Test
    public void valueMerelyContainingNullIsKept() {
        assertEquals("Nullpo", JsonText.clean("Nullpo"));
        assertEquals("null pointer", JsonText.clean("null pointer"));
    }

    /** 空串还是空串。 */
    @Test
    public void emptyStringStaysEmpty() {
        assertEquals("", JsonText.clean(""));
    }
}
