package io.github.javiewer.util;

/**
 * 类别页的分组标签。
 *
 * <p>站点把类别按 {@code type} 分成 8 组，分组名由站点前端 i18n 的 {@code genreTypes.*} 定义，
 * 语义依次为 theme / character / costume / body / sexActs / sexPlays / genre / other。
 * 应用请求类别时传的语言是 {@code "ja"}：骑兵与步兵站点返回日文类别名，欧美站点没有日文名、
 * 站点会回退成英文，因此分组名也跟随同样的语言取用，保证 tab 名与组内类别名一致。
 *
 * <p>{@code type} 落在 0~6 之外时（骑兵会出现 {@code -1}，各源都有 {@code 7}）统一归入「其他」，
 * 即数组的最后一项。
 */
public final class GenreLabels {

    /** 骑兵（jav）与步兵（javu）站点：类别名是日文。 */
    public static final String[] JA = {
            "テーマ", "キャラクター", "コスチューム", "身体",
            "性行為", "プレイ", "ジャンル", "その他",
    };

    /** 欧美（wav）站点：没有日文类别名，站点回退英文。 */
    public static final String[] EN = {
            "Theme", "Character", "Costume", "Body",
            "Sex Acts", "Sex Plays", "Genre", "Other",
    };

    /** 兜底分组（type 7 与越界 type）在数组中的下标。 */
    public static final int OTHER_INDEX = 7;

    private GenreLabels() {
    }

    /**
     * 按数据源的 {@code apiPath} 选分组语言。
     *
     * <p>站点标识是 apiPath 的第一段路径：{@code /jav/} 骑兵、{@code /javu/} 步兵、
     * {@code /wav/} 欧美。无法识别时按日文处理（骑兵是默认数据源）。
     */
    public static String[] forApiPath(String apiPath) {
        if (apiPath == null) {
            return JA;
        }
        int start = 0;
        while (start < apiPath.length() && apiPath.charAt(start) == '/') {
            start++;
        }
        int end = apiPath.indexOf('/', start);
        String site = end < 0 ? apiPath.substring(start) : apiPath.substring(start, end);
        return "wav".equalsIgnoreCase(site) ? EN : JA;
    }

    /**
     * 取某个 {@code type} 对应的分组名。
     *
     * <p>0~6 返回对应标签；{@code 7}、{@code -1} 及任何越界值统一返回「其他」。
     */
    public static String labelAt(String[] labels, int type) {
        if (labels == null || labels.length == 0) {
            return "";
        }
        if (type < 0 || type >= OTHER_INDEX) {
            return labels[Math.min(OTHER_INDEX, labels.length - 1)];
        }
        return labels[type];
    }

    /** 该 {@code type} 是否属于兜底的「其他」组。 */
    public static boolean isOther(int type) {
        return type < 0 || type >= OTHER_INDEX;
    }
}
