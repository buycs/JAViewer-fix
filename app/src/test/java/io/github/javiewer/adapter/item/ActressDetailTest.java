package io.github.javiewer.adapter.item;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * 女优资料栏的文案拼装。
 *
 * <p>三个数据源返回的字段丰俭不同（步兵、欧美没有生日 / 血型 / 三围），
 * 所以这里的重点是：缺哪个字段就少哪一段 / 少哪一枚胶囊，
 * 不能留下多余的分隔符、空的括号或空白行。
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
    public void buildsCountAndReleaseLines() {
        ActressDetail detail = hatano();

        assertEquals("4617 部作品 · 2433 部可下载", detail.buildCountLine());
        assertEquals("最近发布 2026-09-19", detail.buildReleaseLine());
    }

    /** 三围必须带标签 —— 光看 B/W/H 缩写认不出是什么，这是用户明确提的要求。 */
    @Test
    public void measurementChipIsLabeled() {
        assertEquals("B88(D) W59 H85", hatano().buildMeasurement());
        assertEquals("三围 B88(D) W59 H85", hatano().buildMeasurementChip());
    }

    /** 日期 / 星座 / 血型 / 身高值本身就能说明是什么，不加标签；地名与爱好要加。 */
    @Test
    public void chipsContainEveryFieldInOrder() {
        assertEquals(
                Arrays.asList(
                        "1988-05-24",
                        "双子座",
                        "A型",
                        "163cm",
                        "三围 B88(D) W59 H85",
                        "出生地 京都府",
                        "爱好 ゲーム"
                ),
                hatano().buildChips()
        );
    }

    /** 步兵 / 欧美的响应里生日、血型、三围都缺，出生地与爱好是 null —— 不该产出任何胶囊。 */
    @Test
    public void sparseDataSourceProducesNoChips() {
        ActressDetail detail = new ActressDetail();
        detail.name = "Aubrey James";
        detail.movieCount = 4;
        detail.downloadMovieCount = 4;

        assertEquals("4 部作品 · 4 部可下载", detail.buildCountLine());
        assertEquals("", detail.buildReleaseLine());
        assertEquals(Collections.emptyList(), detail.buildChips());
    }

    /** 只有部分资料时，只产出对应的那几枚胶囊，不留空项。 */
    @Test
    public void partialDataProducesOnlyPresentChips() {
        ActressDetail detail = new ActressDetail();
        detail.birthday = "2000-05-20";
        detail.constellation = 2;
        detail.height = "155";

        assertEquals(
                Arrays.asList("2000-05-20", "金牛座", "155cm"),
                detail.buildChips()
        );
    }

    /** 没有罩杯数据时不要拼出空的括号。 */
    @Test
    public void measurementWithoutCupHasNoParentheses() {
        ActressDetail detail = new ActressDetail();
        detail.bust = "88";

        assertEquals("B88", detail.buildMeasurement());
        assertEquals("三围 B88", detail.buildMeasurementChip());
    }

    /** 只有腰围时也照样成立，不会留下多余空格。 */
    @Test
    public void measurementDropsMissingParts() {
        ActressDetail detail = new ActressDetail();
        detail.waist = "60";
        detail.hip = "89";

        assertEquals("W60 H89", detail.buildMeasurement());
    }

    /** 血型字段本身带「型」时不要重复拼接。 */
    @Test
    public void bloodTypeAlreadySuffixedIsNotDoubled() {
        ActressDetail detail = new ActressDetail();
        detail.bloodType = "AB型";

        assertEquals(Collections.singletonList("AB型"), detail.buildChips());
    }

    /** 没数据时全部返回空，调用方据此隐藏整块。 */
    @Test
    public void emptyDetailProducesNothing() {
        ActressDetail detail = new ActressDetail();

        assertEquals("", detail.buildCountLine());
        assertEquals("", detail.buildReleaseLine());
        assertEquals("", detail.buildMeasurement());
        assertEquals("", detail.buildMeasurementChip());
        assertEquals(Collections.emptyList(), detail.buildChips());
    }
}
