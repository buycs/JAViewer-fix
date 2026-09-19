package io.github.javiewer.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionUtilTest {

    @Test
    public void normalizeStripsPrefixAndWhitespace() {
        assertEquals("2.5.0", VersionUtil.normalize("v2.5.0"));
        assertEquals("2.5.0", VersionUtil.normalize("V2.5.0"));
        assertEquals("1.2", VersionUtil.normalize("  v1.2  "));
        assertEquals("", VersionUtil.normalize(null));
        assertEquals("", VersionUtil.normalize("   "));
    }

    @Test
    public void compareTreatsMissingSegmentsAsZero() {
        assertEquals(0, VersionUtil.compare("2.5", "2.5.0"));
        assertEquals(0, VersionUtil.compare("2.5.0", "2.5"));
        assertEquals(0, VersionUtil.compare("v2.5.0", "2.5.0"));
    }

    @Test
    public void compareIsNumericNotLexicographic() {
        assertTrue(VersionUtil.compare("2.10.0", "2.9.0") > 0);
        assertTrue(VersionUtil.compare("2.9.0", "2.10.0") < 0);
    }

    @Test
    public void compareOrdersVersions() {
        assertTrue(VersionUtil.compare("2.5.1", "2.5.0") > 0);
        assertTrue(VersionUtil.compare("3.0.0", "2.9.9") > 0);
        assertTrue(VersionUtil.compare("2.0.3", "2.5.0") < 0);
        assertEquals(0, VersionUtil.compare("2.5.0", "2.5.0"));
    }

    @Test
    public void isNewerMatchesCompare() {
        assertTrue(VersionUtil.isNewer("v2.6.0", "2.5.0"));
        assertFalse(VersionUtil.isNewer("v2.5.0", "2.5.0"));
        assertFalse(VersionUtil.isNewer("v2.0.3", "2.5.0"));
    }

    @Test
    public void preReleaseSuffixIsIgnoredForCoreComparison() {
        assertEquals(0, VersionUtil.compare("2.5.0-beta1", "2.5.0"));
        assertEquals(0, VersionUtil.compare("2.5.0+build7", "2.5.0"));
    }

    @Test
    public void malformedInputDoesNotThrow() {
        assertEquals(0, VersionUtil.compare("abc", "0"));
        assertFalse(VersionUtil.isNewer("not-a-version", "2.5.0"));
        assertTrue(VersionUtil.isNewer("1.0.0", "not-a-version"));
    }
}
