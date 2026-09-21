package io.github.javiewer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * 分组标签的数据源选择与 type 映射。
 */
public class GenreLabelsTest {

    @Test
    public void eachSiteGetsItsOwnLabels() {
        assertSame(GenreLabels.JAV, GenreLabels.forApiPath("/jav/data/api/"));
        assertSame(GenreLabels.JAVU, GenreLabels.forApiPath("/javu/data/api/"));
        assertSame(GenreLabels.WAV, GenreLabels.forApiPath("/wav/data/api/"));
    }

    @Test
    public void apiPathWithoutTrailingSlashStillResolves() {
        assertSame(GenreLabels.WAV, GenreLabels.forApiPath("/wav"));
        assertSame(GenreLabels.WAV, GenreLabels.forApiPath("wav"));
        assertSame(GenreLabels.JAVU, GenreLabels.forApiPath("/javu"));
    }

    @Test
    public void unknownOrMissingApiPathFallsBackToJav() {
        assertSame(GenreLabels.JAV, GenreLabels.forApiPath(null));
        assertSame(GenreLabels.JAV, GenreLabels.forApiPath(""));
        assertSame(GenreLabels.JAV, GenreLabels.forApiPath("/"));
        assertSame(GenreLabels.JAV, GenreLabels.forApiPath("/unknown/data/api/"));
    }

    @Test
    public void typesZeroToSixFollowTheSiteSemantics() {
        // 站点 cn 字典 genreTypes：theme/character/costume/body/sexActs/sexPlays/genre
        String[] expected = {
                "主题", "角色", "服装", "身体", "性行为", "玩法", "类别",
        };
        for (int type = 0; type < expected.length; type++) {
            assertEquals(expected[type], GenreLabels.JAV.at(type));
            assertEquals(expected[type], GenreLabels.JAVU.at(type));
            assertEquals(expected[type], GenreLabels.WAV.at(type));
        }
    }

    @Test
    public void javTypeSevenIsItsOwnAvOpenGroup() {
        // 原站第 8 段整段都是 AV OPEN，站点自身叫「其他」，应用给它一个更好认的名字
        assertEquals("AV OPEN", GenreLabels.JAV.at(7));
    }

    @Test
    public void javMinusOneFallsBackToOther() {
        assertEquals("其他", GenreLabels.JAV.at(-1));
        assertEquals("其他", GenreLabels.JAV.at(8));
        assertEquals("其他", GenreLabels.JAV.at(99));
    }

    @Test
    public void javuAndWavTypeSevenIsTheFallbackLabel() {
        // 步兵 / 欧美的 type 7 是普通类别（场所 / 节日），没有 AV OPEN 这回事
        assertEquals("其他", GenreLabels.JAVU.at(7));
        assertEquals("其他", GenreLabels.JAVU.at(-1));
        assertEquals("其他", GenreLabels.WAV.at(7));
        assertEquals("其他", GenreLabels.WAV.at(-1));
    }

    @Test
    public void typeCountIsEightForEverySite() {
        assertEquals(8, GenreLabels.JAV.typeCount());
        assertEquals(8, GenreLabels.JAVU.typeCount());
        assertEquals(8, GenreLabels.WAV.typeCount());
    }
}
