package io.github.javiewer.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueryNormalizerTest {

    @Test
    public void normalizeCodeVariants() {
        assertEquals("ABP-123", QueryNormalizer.normalize("abp 123"));
        assertEquals("ABP-123", QueryNormalizer.normalize("abp-123"));
        assertEquals("ABP-123", QueryNormalizer.normalize("ABP123"));
        assertEquals("ABP-123", QueryNormalizer.normalize("  ABP123  "));
    }

    @Test
    public void normalizeLeavesOrdinaryText() {
        assertEquals("三上悠亚", QueryNormalizer.normalize("三上悠亚"));
        assertEquals("查找 三上悠亚", QueryNormalizer.normalize(" 查找 三上悠亚 "));
        assertEquals("search abp 123", QueryNormalizer.normalize("search abp 123"));
    }

    @Test
    public void normalizeNullAndBlank() {
        assertNull(QueryNormalizer.normalize(null));
        assertEquals("", QueryNormalizer.normalize(""));
        assertEquals("", QueryNormalizer.normalize("   "));
    }
}
