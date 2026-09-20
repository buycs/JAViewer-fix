package io.github.javiewer.util;

/**
 * 类别页的分组标签。
 *
 * <p>站点把类别按 {@code type} 分成若干组，分组名由站点前端 i18n 的 {@code genreTypes.*} 定义，
 * 语义依次为 theme / character / costume / body / sexActs / sexPlays / genre / other。
 * 应用请求类别时传的语言是 {@code "ja"}：骑兵与步兵站点返回日文类别名，欧美站点没有日文名、
 * 站点会回退成英文，因此分组名也跟随同样的语言，保证 tab 名与组内类别名一致。
 *
 * <p><b>骑兵的 {@code type 7} 是个例外。</b> 原站类别页渲染 9 段，第 8 段整段都是
 * AV OPEN 2016 / 2017 / 2018 各部门（站点自身给它的标题是「其他」），第 9 段才是
 * {@code -1} 组（パラダイスTV、促销精选、AV OPEN 2014/2015 等）。应用把它单独成组并标成
 * 「AV OPEN」，比出现两个同名「其他」tab 更可读。步兵与欧美的 {@code type 7} 是普通类别
 * （场所 / 节日），仍按站点语义叫「その他」/「Other」。
 *
 * <p>骑兵还会出现 {@code -1}，它不在 0~7 里，落到兜底标签「その他」。
 */
public final class GenreLabels {

    /** type 0~7 的分组名，下标即 {@code type}。 */
    private final String[] byType;

    /** type 越界（骑兵的 {@code -1}）时用的分组名。 */
    private final String fallback;

    private GenreLabels(String[] byType, String fallback) {
        this.byType = byType;
        this.fallback = fallback;
    }

    /** 骑兵（jav）：第 8 段是 AV OPEN 专题。 */
    public static final GenreLabels JAV = new GenreLabels(
            new String[]{
                    "テーマ", "キャラクター", "コスチューム", "身体",
                    "性行為", "プレイ", "ジャンル", "AV OPEN",
            },
            "その他");

    /** 步兵（javu）：没有 -1 组，type 7 就是普通的「その他」。 */
    public static final GenreLabels JAVU = new GenreLabels(
            new String[]{
                    "テーマ", "キャラクター", "コスチューム", "身体",
                    "性行為", "プレイ", "ジャンル", "その他",
            },
            "その他");

    /** 欧美（wav）：没有日文类别名，站点回退英文。 */
    public static final GenreLabels WAV = new GenreLabels(
            new String[]{
                    "Theme", "Character", "Costume", "Body",
                    "Sex Acts", "Sex Plays", "Genre", "Other",
            },
            "Other");

    /**
     * 按数据源的 {@code apiPath} 选分组标签。
     *
     * <p>站点标识是 apiPath 的第一段路径：{@code /jav/} 骑兵、{@code /javu/} 步兵、
     * {@code /wav/} 欧美。无法识别时按骑兵处理（骑兵是默认数据源）。
     */
    public static GenreLabels forApiPath(String apiPath) {
        if (apiPath == null) {
            return JAV;
        }
        int start = 0;
        while (start < apiPath.length() && apiPath.charAt(start) == '/') {
            start++;
        }
        int end = apiPath.indexOf('/', start);
        String site = end < 0 ? apiPath.substring(start) : apiPath.substring(start, end);
        if ("wav".equalsIgnoreCase(site)) {
            return WAV;
        }
        if ("javu".equalsIgnoreCase(site)) {
            return JAVU;
        }
        return JAV;
    }

    /**
     * 取某个 {@code type} 对应的分组名。
     *
     * <p>0~7 返回对应标签；越界（骑兵的 {@code -1}，或响应里缺 {@code type} 字段）返回兜底标签。
     */
    public String at(int type) {
        if (type < 0 || type >= byType.length) {
            return fallback;
        }
        return byType[type];
    }

    /** 标签总数，即受支持的 type 个数。 */
    public int typeCount() {
        return byType.length;
    }
}
