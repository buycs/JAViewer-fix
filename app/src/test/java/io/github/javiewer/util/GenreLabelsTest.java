package io.github.javiewer.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 分组标签的语言选择与越界兜底。
 */
public class GenreLabelsTest {

    @Test
    public void javSiteUsesJapaneseLabels() {
        assertSame(GenreLabels.JA, GenreLabels.forApiPath("/jav/data/api/"));
    }

    @Test
    public void javuSiteUsesJapaneseLabels() {
        assertSame(GenreLabels.JA, GenreLabels.forApiPath("/javu/data/api/"));
    }

    @Test
    public void wavSiteUsesEnglishLabels() {
        assertSame(GenreLabels.EN, GenreLabels.forApiPath("/wav/data/api/"));
    }

    @Test
    public void apiPathWithoutTrailingSlashStillResolves() {
        assertSame(GenreLabels.EN, GenreLabels.forApiPath("/wav"));
        assertSame(GenreLabels.EN, GenreLabels.forApiPath("wav"));
        assertSame(GenreLabels.JA, GenreLabels.forApiPath("/javu"));
    }

    @Test
    public void unknownOrMissingApiPathFallsBackToJapanese() {
        assertSame(GenreLabels.JA, GenreLabels.forApiPath(null));
        assertSame(GenreLabels.JA, GenreLabels.forApiPath(""));
        assertSame(GenreLabels.JA, GenreLabels.forApiPath("/"));
        assertSame(GenreLabels.JA, GenreLabels.forApiPath("/unknown/data/api/"));
    }

    @Test
    public void labelAtReturnsPerTypeLabelForZeroToSix() {
        assertEquals("テーマ", GenreLabels.labelAt(GenreLabels.JA, 0));
        assertEquals("ジャンル", GenreLabels.labelAt(GenreLabels.JA, 6));
        assertEquals("Theme", GenreLabels.labelAt(GenreLabels.EN, 0));
        assertEquals("Genre", GenreLabels.labelAt(GenreLabels.EN, 6));
    }

    @Test
    public void labelAtFoldsSevenAndOutOfRangeIntoOther() {
        // 站点把 type 7 也定义为 other，骑兵还会出现 -1，两者都要落到「其他」
        assertEquals("その他", GenreLabels.labelAt(GenreLabels.JA, 7));
        assertEquals("その他", GenreLabels.labelAt(GenreLabels.JA, -1));
        assertEquals("その他", GenreLabels.labelAt(GenreLabels.JA, 99));
        assertEquals("Other", GenreLabels.labelAt(GenreLabels.EN, 7));
        assertEquals("Other", GenreLabels.labelAt(GenreLabels.EN, -1));
    }

    @Test
    public void isOtherMatchesTheSameBoundaries() {
        assertFalse(GenreLabels.isOther(0));
        assertFalse(GenreLabels.isOther(6));
        assertTrue(GenreLabels.isOther(7));
        assertTrue(GenreLabels.isOther(-1));
        assertTrue(GenreLabels.isOther(8));
    }

    @Test
    public void labelArraysCoverTheOtherIndex() {
        assertEquals(8, GenreLabels.JA.length);
        assertEquals(8, GenreLabels.EN.length);
        assertArrayEquals(
                new String[]{"テーマ", "キャラクター", "コスチューム", "身体",
                        "性行為", "プレイ", "ジャンル", "その他"},
                GenreLabels.JA);
        assertArrayEquals(
                new String[]{"Theme", "Character", "Costume", "Body",
                        "Sex Acts", "Sex Plays", "Genre", "Other"},
                GenreLabels.EN);
    }

    @Test
    public void labelAtToleratesEmptyArray() {
        assertEquals("", GenreLabels.labelAt(null, 3));
        assertEquals("", GenreLabels.labelAt(new String[0], 3));
    }
}
