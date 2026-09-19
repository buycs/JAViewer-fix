package io.github.javiewer.util;

/**
 * 版本号比较工具。
 *
 * <p>用于把 GitHub Release 的 tag（形如 {@code v2.5.0}）和本机
 * {@code BuildConfig.VERSION_NAME} 做比较，判断是否有新版本。
 */
public final class VersionUtil {

    private VersionUtil() {
    }

    /** 去掉首尾空白与开头的 {@code v} / {@code V} 前缀。 */
    public static String normalize(String version) {
        if (version == null) {
            return "";
        }
        String trimmed = version.trim();
        if (!trimmed.isEmpty() && (trimmed.charAt(0) == 'v' || trimmed.charAt(0) == 'V')) {
            trimmed = trimmed.substring(1).trim();
        }
        return trimmed;
    }

    /**
     * 按数字段逐段比较两个版本号，缺省的段按 0 处理。
     *
     * <p>例如 {@code 2.5.0} &lt; {@code 2.5.1}，{@code 2.5} 等于 {@code 2.5.0}。
     *
     * @return 负数表示 {@code a} 比 {@code b} 旧，0 表示相同，正数表示 {@code a} 比 {@code b} 新
     */
    public static int compare(String a, String b) {
        int[] left = parseSegments(a);
        int[] right = parseSegments(b);
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int l = i < left.length ? left[i] : 0;
            int r = i < right.length ? right[i] : 0;
            if (l != r) {
                return l < r ? -1 : 1;
            }
        }
        return 0;
    }

    /** {@code candidate} 是否比 {@code current} 新。 */
    public static boolean isNewer(String candidate, String current) {
        return compare(candidate, current) > 0;
    }

    private static int[] parseSegments(String version) {
        String core = normalize(version);
        int cut = indexOfAny(core, '-', '+');
        if (cut >= 0) {
            core = core.substring(0, cut);
        }
        if (core.isEmpty()) {
            return new int[0];
        }
        String[] parts = core.split("\\.");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = parseSegment(parts[i]);
        }
        return values;
    }

    private static int indexOfAny(String value, char first, char second) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == first || c == second) {
                return i;
            }
        }
        return -1;
    }

    private static int parseSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return 0;
        }
        try {
            long value = Long.parseLong(segment);
            if (value < 0) {
                return 0;
            }
            return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
