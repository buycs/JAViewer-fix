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
 * <p>站点把类别按 {@code type} 分成 8 组，但两个站点族的响应结构不同：
 * 骑兵的 {@code data} 是 dict（key 就是 type），步兵与欧美是 list of list（下标即 type）。
 * 这里用真实抓下来的响应形状钉住解析结果，重点是「同一个 type 在三个源上落到同一个分组」。
 */
public class AVMOProviderGenreTest {

    /** 骑兵：dict 结构，另有站点自己加的 -1 组。 */
    private static final String JAV_DICT =
            "{\"code\":200,\"data\":{"
                    + "\"-1\":[{\"genreId\":\"oenqmon\",\"genreName\":\"パラダイスTV\",\"type\":-1},"
                    + "{\"genreId\":\"xparax\",\"genreName\":\"X\",\"type\":-1}],"
                    + "\"0\":[{\"genreId\":\"gznwzlk\",\"genreName\":\"企画\",\"type\":0}],"
                    + "\"1\":[{\"genreId\":\"vjkzbkq\",\"genreName\":\"ウェイトレス\",\"type\":1}],"
                    + "\"2\":[{\"genreId\":\"xlkbqnd\",\"genreName\":\"コスプレ\",\"type\":2}],"
                    + "\"3\":[{\"genreId\":\"yjkojnz\",\"genreName\":\"巨乳\",\"type\":3}],"
                    + "\"4\":[{\"genreId\":\"rpnpmko\",\"genreName\":\"中出し\",\"type\":4}],"
                    + "\"5\":[{\"genreId\":\"gznwlkr\",\"genreName\":\"3P・4P\",\"type\":5}],"
                    + "\"6\":[{\"genreId\":\"vdkagkj\",\"genreName\":\"ベスト・総集編\",\"type\":6}],"
                    + "\"7\":[{\"genreId\":\"rpnpjpn\",\"genreName\":\"AV OPEN 2016 人妻・熟女部門\",\"type\":7}]"
                    + "}}";

    /** 步兵：list of list，下标即 type。 */
    private static final String JAVU_LIST =
            "{\"code\":200,\"data\":["
                    + "[{\"genreId\":\"ekqjjln\",\"genreName\":\"ウェディング\",\"type\":0}],"
                    + "[{\"genreId\":\"lkbqmwn\",\"genreName\":\"エステティシャン\",\"type\":1}],"
                    + "[{\"genreId\":\"mndplon\",\"genreName\":\"短パン\",\"type\":2}],"
                    + "[{\"genreId\":\"mkrqgwn\",\"genreName\":\"Cカップ\",\"type\":3}],"
                    + "[{\"genreId\":\"znwmpbn\",\"genreName\":\"飲尿\",\"type\":4}],"
                    + "[{\"genreId\":\"lkxdebk\",\"genreName\":\"ドッキリ\",\"type\":5}],"
                    + "[{\"genreId\":\"mnrorzk\",\"genreName\":\"カラオケルーム\",\"type\":6}],"
                    + "[{\"genreId\":\"mkdvejk\",\"genreName\":\"別荘\",\"type\":7}]"
                    + "]}";

    /** 欧美：list of list，没有日文名，站点回退英文。 */
    private static final String WAV_LIST =
            "{\"code\":200,\"data\":["
                    + "[{\"genreId\":\"yjkopan\",\"genreName\":\"Wedding\",\"type\":0}],"
                    + "[{\"genreId\":\"xlkbvbk\",\"genreName\":\"Milk Maid\",\"type\":1}],"
                    + "[{\"genreId\":\"qmnrwjk\",\"genreName\":\"Colored Stockings\",\"type\":2}],"
                    + "[{\"genreId\":\"xlkbybn\",\"genreName\":\"Curvy Woman\",\"type\":3}],"
                    + "[{\"genreId\":\"yjkojan\",\"genreName\":\"Anal Fingering\",\"type\":4}],"
                    + "[{\"genreId\":\"pnlqeqk\",\"genreName\":\"Nipple clamps\",\"type\":5}],"
                    + "[{\"genreId\":\"olnxqqk\",\"genreName\":\"Brazilian\",\"type\":6}],"
                    + "[{\"genreId\":\"dpnexbk\",\"genreName\":\"Cinco De Mayo\",\"type\":7}]"
                    + "]}";

    private static List<String> keys(LinkedHashMap<String, List<Genre>> map) {
        return new ArrayList<>(map.keySet());
    }

    private static String nameOf(LinkedHashMap<String, List<Genre>> map, String label) {
        List<Genre> list = map.get(label);
        return list == null || list.isEmpty() ? null : list.get(0).getName();
    }

    @Test
    public void javDictGroupsUseSiteSemantics() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JA);

        assertEquals(
                Arrays.asList("テーマ", "キャラクター", "コスチューム", "身体",
                        "性行為", "プレイ", "ジャンル", "その他"),
                keys(map));
        assertEquals("企画", nameOf(map, "テーマ"));
        assertEquals("巨乳", nameOf(map, "身体"));
        assertEquals("中出し", nameOf(map, "性行為"));
        assertEquals("ベスト・総集編", nameOf(map, "ジャンル"));
    }

    @Test
    public void javMinusOneAndSevenBothLandInOther() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JA);

        List<Genre> others = map.get("その他");
        // -1 组的 2 条 + 7 组的 1 条
        assertEquals(3, others.size());
        assertEquals("パラダイスTV", others.get(0).getName());
        assertEquals("AV OPEN 2016 人妻・熟女部門", others.get(others.size() - 1).getName());
        // 以前骑兵把 7 单独标成「AV OPEN」，站点语义里它其实就是 other
        assertFalse(map.containsKey("AV OPEN"));
    }

    @Test
    public void javuListGroupsUseSameLabelsAsJav() throws Exception {
        LinkedHashMap<String, List<Genre>> jav =
                AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JA);
        LinkedHashMap<String, List<Genre>> javu =
                AVMOProvider.parseGenres(JAVU_LIST, GenreLabels.JA);

        // 同一套 type 语义，两个源的 tab 名必须完全一致
        assertEquals(keys(jav), keys(javu));
        assertEquals("ウェディング", nameOf(javu, "テーマ"));
        assertEquals("エステティシャン", nameOf(javu, "キャラクター"));
        assertEquals("Cカップ", nameOf(javu, "身体"));
        assertEquals("ドッキリ", nameOf(javu, "プレイ"));
    }

    @Test
    public void javuTypeSevenIsFoldedIntoOther() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(JAVU_LIST, GenreLabels.JA);

        List<Genre> others = map.get("その他");
        assertEquals(1, others.size());
        assertEquals("別荘", others.get(0).getName());
        // 步兵没有 -1 组，所以「其他」只有 1 条
        assertEquals(8, map.size());
    }

    @Test
    public void wavUsesEnglishLabels() throws Exception {
        LinkedHashMap<String, List<Genre>> map =
                AVMOProvider.parseGenres(WAV_LIST, GenreLabels.EN);

        assertEquals(
                Arrays.asList("Theme", "Character", "Costume", "Body",
                        "Sex Acts", "Sex Plays", "Genre", "Other"),
                keys(map));
        assertEquals("Wedding", nameOf(map, "Theme"));
        assertEquals("Brazilian", nameOf(map, "Genre"));
        assertEquals("Cinco De Mayo", nameOf(map, "Other"));
    }

    @Test
    public void allThreeSourcesProduceTheSameNumberOfTabs() throws Exception {
        int jav = AVMOProvider.parseGenres(JAV_DICT, GenreLabels.JA).size();
        int javu = AVMOProvider.parseGenres(JAVU_LIST, GenreLabels.JA).size();
        int wav = AVMOProvider.parseGenres(WAV_LIST, GenreLabels.EN).size();
        assertEquals(8, jav);
        assertEquals(8, javu);
        assertEquals(8, wav);
    }

    @Test
    public void emptyGroupsAreSkipped() throws Exception {
        String json = "{\"code\":200,\"data\":{"
                + "\"0\":[{\"genreId\":\"a\",\"genreName\":\"企画\",\"type\":0}],"
                + "\"1\":[],"
                + "\"2\":[{\"genreId\":\"b\",\"genreName\":\"コスプレ\",\"type\":2}]"
                + "}}";
        LinkedHashMap<String, List<Genre>> map = AVMOProvider.parseGenres(json, GenreLabels.JA);

        assertEquals(Arrays.asList("テーマ", "コスチューム"), keys(map));
        assertFalse(map.containsKey("キャラクター"));
    }

    @Test
    public void listGroupWithoutTypeFieldIsTreatedAsOther() throws Exception {
        String json = "{\"code\":200,\"data\":["
                + "[{\"genreId\":\"a\",\"genreName\":\"企画\",\"type\":0}],"
                + "[{\"genreId\":\"z\",\"genreName\":\"謎\"}]"
                + "]}";
        LinkedHashMap<String, List<Genre>> map = AVMOProvider.parseGenres(json, GenreLabels.JA);

        assertEquals(Arrays.asList("テーマ", "その他"), keys(map));
        assertEquals("謎", nameOf(map, "その他"));
    }

    @Test
    public void nullGenreNameIsNormalizedToEmptyString() throws Exception {
        // 站点会返回显式 null；Android 的 optString 会给出字面量 "null"，必须收口成空串
        String json = "{\"code\":200,\"data\":{"
                + "\"0\":[{\"genreId\":\"a\",\"genreName\":null,\"type\":0}]"
                + "}}";
        LinkedHashMap<String, List<Genre>> map = AVMOProvider.parseGenres(json, GenreLabels.JA);

        assertEquals("", nameOf(map, "テーマ"));
    }

    @Test
    public void missingOtherGroupProducesNoOtherTab() throws Exception {
        String json = "{\"code\":200,\"data\":["
                + "[{\"genreId\":\"a\",\"genreName\":\"企画\",\"type\":0}]"
                + "]}";
        LinkedHashMap<String, List<Genre>> map = AVMOProvider.parseGenres(json, GenreLabels.JA);

        assertEquals(1, map.size());
        assertFalse(map.containsKey("その他"));
        assertTrue(map.containsKey("テーマ"));
    }
}
