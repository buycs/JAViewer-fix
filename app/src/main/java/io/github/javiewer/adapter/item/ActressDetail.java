package io.github.javiewer.adapter.item;

import java.util.ArrayList;
import java.util.List;

/**
 * 女优详情，用于「女优的作品」列表页顶部的信息栏。
 *
 * <p>字段直接对应 {@code getStar} 接口返回体。三个数据源（骑兵 / 步兵 / 欧美）返回的
 * 字段丰俭不同——步兵、欧美没有生日、血型、三围，出生地与爱好也可能是 null——
 * 所以这里统一用空串表示「没有」，由 {@link #buildCountLine()} 等拼装方法负责跳过空值，
 * 调用方不需要再做判空。
 */
public class ActressDetail {

    /**
     * {@code constellation} 的数字编码：1 白羊座 … 12 双鱼座。
     * 0 表示未设置（用 480 条线上样本核对过：例如 1988-05-24 → 3 双子座，1988-02-21 → 12 双鱼座）。
     */
    private static final String[] CONSTELLATIONS = {
            "",
            "白羊座", "金牛座", "双子座", "巨蟹座",
            "狮子座", "处女座", "天秤座", "天蝎座",
            "射手座", "摩羯座", "水瓶座", "双鱼座"
    };

    private static final String SEPARATOR = " · ";

    public String name = "";

    public String avatarUrl = "";

    public int movieCount;

    public int downloadMovieCount;

    public String birthday = "";

    public int constellation;

    public String bloodType = "";

    /** 身高，接口只给数字，单位 cm。 */
    public String height = "";

    /** 三围：胸围 / 罩杯 / 腰围 / 臀围，接口只给数字或字母。 */
    public String bust = "";

    public String cup = "";

    public String waist = "";

    public String hip = "";

    public String hometown = "";

    public String hobby = "";

    public String lastReleaseDate = "";

    /**
     * 把接口的星座数字编码翻成中文名。
     *
     * @param code 1 白羊座 … 12 双鱼座
     * @return 星座名；0 或越界（未设置）返回空串
     */
    public static String constellationName(int code) {
        if (code < 1 || code >= CONSTELLATIONS.length) {
            return "";
        }
        return CONSTELLATIONS[code];
    }

    /** 作品数量一行，如 {@code 4617 部作品 · 2433 部可下载}。 */
    public String buildCountLine() {
        List<String> parts = new ArrayList<>();
        if (movieCount > 0) {
            parts.add(movieCount + " 部作品");
        }
        if (downloadMovieCount > 0) {
            parts.add(downloadMovieCount + " 部可下载");
        }
        return join(parts, SEPARATOR);
    }

    /** 最近发布一行，如 {@code 最近发布 2026-09-19}；没有数据时返回空串。 */
    public String buildReleaseLine() {
        return lastReleaseDate.isEmpty() ? "" : "最近发布 " + lastReleaseDate;
    }

    /**
     * 资料胶囊的文案，按展示顺序返回。
     *
     * <p>命名规则：值本身就能说明是什么的（日期、星座、血型、身高）不加标签，
     * 光看值看不出含义的（三围的 B/W/H 缩写、地名、爱好）带上短标签。
     * 没有的属性不会出现在列表里，调用方直接逐条渲染即可。
     */
    public List<String> buildChips() {
        List<String> chips = new ArrayList<>();
        addIfPresent(chips, birthday);
        addIfPresent(chips, constellationName(constellation));
        if (!bloodType.isEmpty()) {
            addIfPresent(chips, bloodType.endsWith("型") ? bloodType : bloodType + "型");
        }
        if (!height.isEmpty()) {
            addIfPresent(chips, height + "cm");
        }
        addIfPresent(chips, buildMeasurementChip());
        if (!hometown.isEmpty()) {
            addIfPresent(chips, "出生地 " + hometown);
        }
        if (!hobby.isEmpty()) {
            addIfPresent(chips, "爱好 " + hobby);
        }
        return chips;
    }

    /** 三围摘要，如 {@code B88(D) W59 H85}；只有部分数据时只拼出已有的那几项。 */
    public String buildMeasurement() {
        List<String> parts = new ArrayList<>();
        if (!bust.isEmpty()) {
            parts.add("B" + bust + (cup.isEmpty() ? "" : "(" + cup + ")"));
        }
        if (!waist.isEmpty()) {
            parts.add("W" + waist);
        }
        if (!hip.isEmpty()) {
            parts.add("H" + hip);
        }
        return join(parts, " ");
    }

    /**
     * 带「三围」标签的三围，给胶囊用：{@code 三围 B88(D) W59 H85}。
     * 光看 B/W/H 缩写认不出是什么，所以这里必须把标签补上。
     */
    public String buildMeasurementChip() {
        String measurement = buildMeasurement();
        return measurement.isEmpty() ? "" : "三围 " + measurement;
    }

    /**
     * 拼接非空片段。不用 {@code String.join}——那是 API 26 才有的方法，本应用 minSdk 21。
     */
    private static String join(List<String> parts, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(part);
        }
        return builder.toString();
    }

    private static void addIfPresent(List<String> parts, String part) {
        if (part != null && !part.isEmpty()) {
            parts.add(part);
        }
    }
}
