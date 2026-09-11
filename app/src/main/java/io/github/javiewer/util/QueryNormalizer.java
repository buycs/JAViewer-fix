package io.github.javiewer.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QueryNormalizer {

    private static final Pattern CODE_PATTERN =
            Pattern.compile("^([A-Za-z]{2,8})[\\s\\-]*([0-9]{2,6})$");

    private QueryNormalizer() {
    }

    public static String normalize(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        Matcher matcher = CODE_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1).toUpperCase(Locale.ROOT) + "-" + matcher.group(2);
        }
        return trimmed;
    }
}
