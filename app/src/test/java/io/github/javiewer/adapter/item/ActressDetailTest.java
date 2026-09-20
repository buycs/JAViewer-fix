package io.github.javiewer.adapter.item;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 女优信息栏的文案拼装。
 *
 * <p>三个数据源返回的字段丰俭不同（步兵、欧美没有生日 / 血型 / 三围），
 * 所以这里的重点是：缺哪个字段就少哪一段，不能留下多余的分隔符或空行。
 */
public class ActressDetailTest {

    private static ActressDetail hatano() {
        ActressDetail detail = new ActressDetail();
        detail.name = "波多野結衣";
        detail.avatarUrl = "https://jp.netcdn.space/mono/actjpgs/hatano_yui.jpg";
        detail.movieCount = 4617;
        detail.downloadMovieCount = 2433;
        detail.birthday = "1988-05-24";
        detail.constellation = 3;
        detail.bloodType = "A";
        detail.height = "163";
        detail.bust = "88";
        detail.cup = "D";
        detail.waist = "59";
        detail.hip = "85";
        detail.hometown = "京都府";
        detail.hobby = "ゲーム";
        detail.lastReleaseDate = "2026-09-19";
        return detail;
    }

    /** 编码是 1 白羊 … 12 双鱼，用线上样本核对过：05-24 → 3、02-21 → 12。 */
    @Test
    public void constellationNameMapsOneBasedCode() {
        assertEquals("白羊座", ActressDetail.constellationName(1));
        assertEquals("双子座", ActressDetail.constellationName(3));
        assertEquals("射手座", ActressDetail.constellationName(9));
        assertEquals("双鱼座", ActressDetail.constellationName(12));
    }

    /** 0 是「未设置」，越界也不该崩。 */
    @Test
    public void constellationNameTreatsZeroAndOutOfRangeAsUnset() {
        assertEquals("", ActressDetail.constellationName(0));
        assertEquals("", ActressDetail.constellationName(-1));
        assertEquals("", ActressDetail.constellationName(13));
    }

    @Test
    public void buildsFullLines() {
        ActressDetail detail = hatano();

        assertEquals("4617部作品 · 2433部可下载 · 最近发布 2026-09-19", detail.buildCountLine());
        assertEquals("1988-05-24 · 双子座 · A型 · 163cm · B88(D) W59 H85", detail.buildProfileLine());
        assertEquals("出生地 京都府 · 爱好 ゲーム", detail.buildOriginLine());
    }

    /** 步兵 / 欧美的响应里这些字段是 null 或空数组，对应行应整行为空。 */
    @Test
    public void sparseDataSourceLeavesLinesEmpty() {
        ActressDetail detail = new ActressDetail();
        detail.name = "Aubrey James";
        detail.movieCount = 4;
        detail.downloadMovieCount = 4;

        assertEquals("4部作品 · 4部可下载", detail.buildCountLine());
        assertEquals("", detail.buildProfileLine());
        assertEquals("", detail.buildOriginLine());
    }

    /** 只有部分资料时，只拼出已有的那几段，不留尾部分隔符。 */
    @Test
    public void partialDataOmitsEmptySegments() {
        ActressDetail detail = new ActressDetail();
        detail.birthday = "2000-05-20";
        detail.constellation = 2;
        detail.height = "155";

        assertEquals("2000-05-20 · 金牛座 · 155cm", detail.buildProfileLine());
    }

    @Test
    public void measurementDropsMissingParts() {
        ActressDetail detail = new ActressDetail();
        detail.bust = "90";
        detail.cup = "H";
        detail.waist = "60";
        detail.hip = "89";

        assertEquals("B90(H) W60 H89", detail.buildMeasurement());
    }

    /** 没有罩杯数据时不要拼出空的括号。 */
    @Test
    public void measurementWithoutCupHasNoParentheses() {
        ActressDetail detail = new ActressDetail();
        detail.bust = "88";

        assertEquals("B88", detail.buildMeasurement());
    }

    /** 血型字段本身带「型」时不要重复拼接。 */
    @Test
    public void bloodTypeAlreadySuffixedIsNotDoubled() {
        ActressDetail detail = new ActressDetail();
        detail.bloodType = "AB型";

        assertEquals("AB型", detail.buildProfileLine());
    }

    /** 没数据时整行是空串，调用方据此隐藏这一行。 */
    @Test
    public void emptyDetailProducesNoText() {
        ActressDetail detail = new ActressDetail();

        assertEquals("", detail.buildCountLine());
        assertEquals("", detail.buildProfileLine());
        assertEquals("", detail.buildOriginLine());
        assertEquals("", detail.buildMeasurement());
    }
}
