package io.github.javiewer.network.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.util.GenreLabels;

/**
 * 三个数据源的类别页解析契约。
 *
 * <p>站点把类别按 {@code type} 分成若干组，但两个站点族的响应结构不同：
 * 骑兵的 {@code data} 是 dict（key 就是 type，另含站点自己加的 {@code -1}），
 * 步兵与欧美是 list of list（下标即 type）。
 *
 * <p>分组顺序与命名对齐原站类别页：骑兵渲染 9 段（0~6、AV OPEN、其他），
 * 步兵与欧美 8 段（0~6、其他）。
 */
public class AVMOProviderGenreTest {

    /** 骑兵：dict 结构，另有站点自己加的 -1 组。 */
    private static final String JAV_DICT =
            "{\"code\":200,\"data\":{"
                    + "\"-1\":[{\"genreId\":\"oenqmon\",\"genreName\":\"パラダイスTV\",\"type\":-1},"
                    + "{\"genreId\":\"xparax\",\"genreName\":\"DVDトースター\",\"type\":-1}],"
                    + "\"0\":[{\"genreId\":\"gznwzlk\",\"genreName\":\"企划\",\"type\":0}],"
                    + "\"1\":[{\"genreId\":\"vjkzbkq\",\"genreName\":\"服务生\",\"type\":1}],"
                    + "\"2\":[{\"genreId\":\"xlkbqnd\",\"genreName\":\"角色扮演\",\"type\":2}],"
                    + "\"3\":[{\"genreId\":\"yjkojnz\",\"genreName\":\"巨乳\",\"type\":3}],"
                    + "\"4\":[{\"genreId\":\"rpnpmko\",\"genreName\":\"中出\",\"type\":4}],"
                    + "\"5\":[{\"genreId\":\"gznwlkr\",\"genreName\":\"多P\",\"type\":5}],"
                    + "\"6\":[{\"genreId\":\"vdkagkj\",\"genreName\":\"合集\",\"type\":6}],"
                    + "\"7\":[{\"genreId\":\"rpnpjpn\",\"genreName\":\"AV OPEN 2016 人妻・熟女部門\",\"type\":7}]"
                    + "}}";

    /** 步兵：list of list，下标即 type。 */
    private static final String JAVU_LIST =
            "{\"code\":200,\"data\":["
                    + "[{\"genreId\":\"ekqjjln\",\"genreName\":\"婚礼\",\"type\":0}],"
                    + "[{\"genreId\":\"lkbqmwn\",\"genreName\":\"美容师\",\"type\":1}],"
                    + "[{\"genreId\":\"mndplon\",\"genreName\":\"短裤\",\"type\":2}],"
                    + "[{\"genreId\":\"mkrqgwn\",\"genreName\":\"C罩杯\",\"type\":3}],"
                    + "[{\"genreId\":\"znwmpbn\",\"genreName\":\"喝尿\",\"type\":4}],"
                    + "[{\"genreId\":\"lkxdebk\",\"genreName\":\"恶作剧\",\"type\":5}],"
                    + "[{\"genreId\":\"mnrorzk\",\"genreName\":\"KTV\",\"type\":6}],"
                    + "[{\"genreId\":\"mkdvejk\",\"genreName\":\"别墅\",\"type\":7}]"
                    + "]}";

    /** 欧美：list of list，cn 字典同样有中文名。 */
    private static final String WAV_LIST =
            "{\"code\":200,\"data\":["
                    + "[{\"genreId\":\"yjkopan\",\"genreName\":\"婚礼\",\"type\":0}],"
                    + "[{\"genreId\":\"xlkbvbk\",\"genreName\":\"奶女仆\",\"type\":1}],"
                    + "[{\"genreId\":\"qmnrwjk\",\"genreName\":\"彩色丝袜\",\"type\":2}],"
                    + "[{\"genreId\":\"xlkbybn\",\"genreName\":\"弯曲的女人\",\"type\":3}],"
                    + "[{\"genreId\":\"yjkojan\",\"genreName\":\"肛门指法\",\"type\":4}],"
                    + "[{\"genreId\":\"pnlqeqk\",\"genreName\":\"乳头夹子\",\"type\":5}],"
                    + "[{\"genreId\":\"olnxqqk\",\"genreName\":\"巴西人\",\"type\":6}],"
                    + "[{\"genreId\":\"dpnexbk\",\"genreName\":\"五月五日\",\"type\":7}]"
                    + "]}";

    private static List<String> keys(LinkedHashMap<String, List<Genre>> map) {
        return new ArrayList<>(map.keySet());
    }

    private static String nameOf(LinkedHashMap<String, List<Genre>> map, String label) {
        List<Genre> list = map.get(label);
        return list == null || list.isEmpty() ? null : list.get(0).getName();
    }

    private static int sizeOf(LinkedHashMap<String, List<Genre>> map, String label) {
        List<Genre> list = map.get(label);
        return list == null ? 0 : list.size();
    }

    @Test
    public void javDictRendersNineGroupsInSiteOrder() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JAV);

        assertEquals(
                Arrays.asList("主题", "角色", "服装", "身体",
                        "性行为", "玩法", "类别", "AV OPEN", "其他"),
                keys(map));
        assertEquals("企划", nameOf(map, "主题"));
        assertEquals("巨乳", nameOf(map, "身体"));
        assertEquals("中出", nameOf(map, "性行为"));
        assertEquals("合集", nameOf(map, "类别"));
    }

    @Test
    public void javTypeSevenIsItsOwnAvOpenGroup() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JAV);

        // 原站第 8 段整段都是 AV OPEN，单独成组而不是并进「其他」
        assertEquals(1, sizeOf(map, "AV OPEN"));
        assertEquals("AV OPEN 2016 人妻・熟女部門", nameOf(map, "AV OPEN"));
    }

    @Test
    public void javMinusOneGroupIsKeptAndComesLast() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JAV);

        List<String> keys = keys(map);
        assertEquals("其他", keys.get(keys.size() - 1));
        assertEquals(2, sizeOf(map, "其他"));
        assertEquals("パラダイスTV", nameOf(map, "其他"));
    }

    @Test
    public void javuListRendersEightGroupsWithoutAvOpen() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAVU_LIST, GenreLabels.JAVU);

        assertEquals(
                Arrays.asList("主题", "角色", "服装", "身体",
                        "性行为", "玩法", "类别", "其他"),
                keys(map));
        assertEquals("婚礼", nameOf(map, "主题"));
        assertEquals("美容师", nameOf(map, "角色"));
        assertEquals("C罩杯", nameOf(map, "身体"));
        assertEquals("恶作剧", nameOf(map, "玩法"));
        // 步兵没有 -1 组，type 7 就是普通的「其他」
        assertEquals("别墅", nameOf(map, "其他"));
        assertEquals(1, sizeOf(map, "其他"));
    }

    @Test
    public void wavUsesChineseLabels() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(WAV_LIST, GenreLabels.WAV);

        assertEquals(
                Arrays.asList("主题", "角色", "服装", "身体",
                        "性行为", "玩法", "类别", "其他"),
                keys(map));
        assertEquals("婚礼", nameOf(map, "主题"));
        assertEquals("巴西人", nameOf(map, "类别"));
        assertEquals("五月五日", nameOf(map, "其他"));
    }

    @Test
    public void groupCountsMatchWhatTheSitesRender() throws Exception {
        // 骑兵 9 段（多一个 -1），步兵与欧美各 8 段
        assertEquals(9, AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JAV).size());
        assertEquals(8, AVMOProvider.parseGenres(JAVU_LIST, GenreLabels.JAVU).size());
        assertEquals(8, AVMOProvider.parseGenres(WAV_LIST, GenreLabels.WAV).size());
    }

    @Test
    public void emptyGroupsAreSkipped() throws Exception {
        String json = "{\"code\":200,\"data\":{"
                + "\"0\":[{\"genreId\":\"a\",\"genreName\":\"企划\",\"type\":0}],"
                + "\"1\":[],"
                + "\"2\":[{\"genreId\":\"b\",\"genreName\":\"角色扮演\",\"type\":2}]"
                + "}}";
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(json, GenreLabels.JAV);

        assertEquals(Arrays.asList("主题", "服装"), keys(map));
        assertFalse(map.containsKey("角色"));
    }

    @Test
    public void listGroupWithoutTypeFieldIsTreatedAsOther() throws Exception {
        String json = "{\"code\":200,\"data\":["
                + "[{\"genreId\":\"a\",\"genreName\":\"企划\",\"type\":0}],"
                + "[{\"genreId\":\"z\",\"genreName\":\"谜\"}]"
                + "]}";
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(json, GenreLabels.JAV);

        assertEquals(Arrays.asList("主题", "其他"), keys(map));
        assertEquals("谜", nameOf(map, "其他"));
    }

    @Test
    public void nullGenreNameIsNormalizedToEmptyString() throws Exception {
        // 站点会返回显式 null；Android 的 optString 会给出字面量 "null"，必须收口成空串
        String json = "{\"code\":200,\"data\":{"
                + "\"0\":[{\"genreId\":\"a\",\"genreName\":null,\"type\":0}]"
                + "}}";
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(json, GenreLabels.JAV);

        assertEquals("", nameOf(map, "主题"));
    }

    @Test
    public void missingOtherGroupProducesNoOtherTab() throws Exception {
        String json = "{\"code\":200,\"data\":["
                + "[{\"genreId\":\"a\",\"genreName\":\"企划\",\"type\":0}]"
                + "]}";
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(json, GenreLabels.JAV);

        assertEquals(1, map.size());
        assertFalse(map.containsKey("其他"));
        assertTrue(map.containsKey("主题"));
    }
}
