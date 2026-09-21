package io.github.javiewer.adapter.item;

import java.util.ArrayList;
import java.util.List;

/**
 * 女优详情，用于「女优的作品」列表页顶部的信息栏。
 *
 * <p>字段直接对应 {@code getStar} 接口返回体。三个数据源（骑兵 / 步兵 / 欧美）返回的
 * 字段丰俭不同——步兵、欧美没有生日、血型、三围，出生地与爱好也可能是 null——
 * 所以这里统一用空串表示「没有」，由 {@link #buildChips()} 等拼装方法负责跳过空值，
 * 调用方不需要再做判空。
 *
 * <p>界面上的呈现单位是 {@link Chip}，也就是「标签 + 值」——标签走浅色、值走深色、
 * 两者同一字号（排版参照 JavCinema 的女优资料页表头）。所以这里返回的是拆开的
 * 标签与值，而不是拼好的整串：颜色和字号属于布局层的事。
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

    private static final String LABEL_MOVIE_COUNT = "作品数";
    private static final String LABEL_LAST_RELEASE = "最近作品";
    private static final String LABEL_BIRTHDAY = "生日";
    private static final String LABEL_CONSTELLATION = "星座";
    private static final String LABEL_BLOOD_TYPE = "血型";
    private static final String LABEL_HEIGHT = "身高";
    private static final String LABEL_BUST = "胸";
    private static final String LABEL_WAIST = "腰";
    private static final String LABEL_HIP = "臀";
    private static final String LABEL_HOMETOWN = "出生地";
    private static final String LABEL_HOBBY = "爱好";

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
     * 一枚资料胶囊。{@link #label} 一律非空；没有数据的字段**不产生** Chip，
     * 调用方逐枚渲染即可，不必再判空。
     */
    public static final class Chip {

        public final String label;

        public final String value;

        public Chip(String label, String value) {
            this.label = label;
            this.value = value;
        }

        /** 值为空即这一项没有数据，界面据此整枚隐藏。 */
        public boolean isEmpty() {
            return value == null || value.isEmpty();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Chip)) {
                return false;
            }
            Chip that = (Chip) other;
            return label.equals(that.label) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return label.hashCode() * 31 + value.hashCode();
        }

        @Override
        public String toString() {
            return label + " " + value;
        }
    }

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

    /**
     * 作品数胶囊：{@code 作品数 | 4617 部 · 可下载 2433}。
     *
     * <p>可下载数是总数的子集，并成一项写；拆成两枚胶囊会让它们看起来是两回事。
     * 站点没给 {@code downloadMovieCount} 时只剩总数，两个数都没有时值为空串。
     */
    public Chip buildCountChip() {
        List<String> parts = new ArrayList<>();
        if (movieCount > 0) {
            parts.add(movieCount + " 部");
        }
        if (downloadMovieCount > 0) {
            parts.add("可下载 " + downloadMovieCount);
        }
        return new Chip(LABEL_MOVIE_COUNT, join(parts, SEPARATOR));
    }

    /** 最近作品胶囊：{@code 最近作品 | 2026-09-19}；没有数据时值为空串。 */
    public Chip buildReleaseChip() {
        return new Chip(LABEL_LAST_RELEASE, lastReleaseDate == null ? "" : lastReleaseDate);
    }

    /**
     * 资料胶囊，按展示顺序返回，每一枚都是「标签 + 值」。
     *
     * <p>没有的属性不会出现在列表里，调用方直接逐条渲染即可。
     */
    public List<Chip> buildChips() {
        List<Chip> chips = new ArrayList<>();
        addIfPresent(chips, LABEL_BIRTHDAY, birthday);
        addIfPresent(chips, LABEL_CONSTELLATION, constellationName(constellation));
        if (!bloodType.isEmpty()) {
            addIfPresent(chips, LABEL_BLOOD_TYPE, bloodType.endsWith("型") ? bloodType : bloodType + "型");
        }
        if (!height.isEmpty()) {
            addIfPresent(chips, LABEL_HEIGHT, height + "cm");
        }
        chips.addAll(buildMeasurementChips());
        addIfPresent(chips, LABEL_HOMETOWN, hometown);
        addIfPresent(chips, LABEL_HOBBY, hobby);
        return chips;
    }

    /**
     * 三围胶囊：胸 / 腰 / 臀 各占一枚，逐项标注。
     *
     * <p>标签用单字（参照 JavCinema）：胶囊里已经带了底色和「标签浅、值深」的层次，
     * 「胸 88(D)」不会认错，写成「胸围 88(D)」只是白占宽度、把每行挤少一枚。
     * 也不用「三围」一个词概括——那样后面跟着一串 B/W/H 缩写，认不出哪个是哪个。
     * 接口没给的项不会出现在列表里。
     */
    public List<Chip> buildMeasurementChips() {
        List<Chip> chips = new ArrayList<>();
        if (!bust.isEmpty()) {
            chips.add(new Chip(LABEL_BUST, bust + (cup.isEmpty() ? "" : "(" + cup + ")")));
        }
        if (!waist.isEmpty()) {
            chips.add(new Chip(LABEL_WAIST, waist));
        }
        if (!hip.isEmpty()) {
            chips.add(new Chip(LABEL_HIP, hip));
        }
        return chips;
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

    private static void addIfPresent(List<Chip> chips, String label, String value) {
        if (value != null && !value.isEmpty()) {
            chips.add(new Chip(label, value));
        }
    }
}
