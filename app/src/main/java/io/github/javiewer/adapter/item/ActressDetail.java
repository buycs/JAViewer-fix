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

    /** 作品数量一行，如 {@code 4617部作品 · 2433部可下载 · 最近发布 2026-09-19}。 */
    public String buildCountLine() {
        List<String> parts = new ArrayList<>();
        if (movieCount > 0) {
            parts.add(movieCount + "部作品");
        }
        if (downloadMovieCount > 0) {
            parts.add(downloadMovieCount + "部可下载");
        }
        if (!lastReleaseDate.isEmpty()) {
            parts.add("最近发布 " + lastReleaseDate);
        }
        return join(parts, SEPARATOR);
    }

    /** 基本资料一行，如 {@code 1988-05-24 · 双子座 · A型 · 163cm · B88(D) W59 H85}。 */
    public String buildProfileLine() {
        List<String> parts = new ArrayList<>();
        parts.add(birthday);
        parts.add(constellationName(constellation));
        if (!bloodType.isEmpty()) {
            parts.add(bloodType.endsWith("型") ? bloodType : bloodType + "型");
        }
        if (!height.isEmpty()) {
            parts.add(height + "cm");
        }
        parts.add(buildMeasurement());
        return join(parts, SEPARATOR);
    }

    /** 出生地与爱好一行，如 {@code 出生地 京都府 · 爱好 ゲーム}；两者都没有时返回空串。 */
    public String buildOriginLine() {
        List<String> parts = new ArrayList<>();
        if (!hometown.isEmpty()) {
            parts.add("出生地 " + hometown);
        }
        if (!hobby.isEmpty()) {
            parts.add("爱好 " + hobby);
        }
        return join(parts, SEPARATOR);
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
}
