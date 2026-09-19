package io.github.javiewer;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 远端 properties.json 的解析契约。
 *
 * <p>注意仓库里 {@code latest_version_code} 写的是字符串 {@code "21"}，而
 * {@link Properties} 里字段声明为 {@code int}；这里把 Gson 的隐式转换钉住，
 * 避免以后有人把它改成真正的数字（或反之）时「检查更新」静默失效。
 */
public class PropertiesParsingTest {

    private static final String QUOTED_CODE_JSON =
            "{\n"
                    + "  \"latest_version\": \"2.5.0\",\n"
                    + "  \"latest_version_code\": \"21\",\n"
                    + "  \"changelog\": \"**v2.5.0**\\n- 收藏导入导出\",\n"
                    + "  \"data_sources\": []\n"
                    + "}";

    @Test
    public void parsesQuotedVersionCode() {
        Properties properties = new Gson().fromJson(QUOTED_CODE_JSON, Properties.class);

        assertNotNull(properties);
        assertEquals(21, properties.getLatestVersionCode());
        assertEquals("2.5.0", properties.getLatestVersion());
        assertEquals("**v2.5.0**\n- 收藏导入导出", properties.getChangelog());
    }

    @Test
    public void parsesNumericVersionCode() {
        Properties properties = new Gson().fromJson(
                "{\"latest_version\":\"2.6.0\",\"latest_version_code\":22}", Properties.class);

        assertNotNull(properties);
        assertEquals(22, properties.getLatestVersionCode());
        assertEquals("2.6.0", properties.getLatestVersion());
    }

    @Test
    public void missingFieldsFallBackToDefaults() {
        Properties properties = new Gson().fromJson("{}", Properties.class);

        assertNotNull(properties);
        assertEquals(0, properties.getLatestVersionCode());
    }
}
