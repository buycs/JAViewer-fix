package io.github.javiewer.adapter.item;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 女优资料栏的文案拼装。
 *
 * <p>三个数据源返回的字段丰俭不同（步兵、欧美没有生日 / 血型 / 三围），
 * 所以这里的重点是：缺哪个字段就少哪一枚胶囊，不能留下多余的分隔符、空的括号或空白行。
 *
 * <p>界面上的呈现单位是 {@link ActressDetail.Chip}（标签 + 值），所以断言比的是 Chip 而不是整串。
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

    private static ActressDetail.Chip chip(String label, String value) {
        return new ActressDetail.Chip(label, value);
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
    public void buildsCountAndReleaseChips() {
        ActressDetail detail = hatano();

        assertEquals(chip("作品数", "4617 部 · 可下载 2433"), detail.buildCountChip());
        assertEquals(chip("最近作品", "2026-09-19"), detail.buildReleaseChip());
    }

    /** 胸 / 腰 / 臀 各占一枚胶囊，逐项标注 —— 不要用「三围」一个词概括。 */
    @Test
    public void measurementIsSplitIntoChestWaistHipChips() {
        assertEquals(
                Arrays.asList(chip("胸", "88(D)"), chip("腰", "59"), chip("臀", "85")),
                hatano().buildMeasurementChips()
        );
    }

    @Test
    public void chipsContainEveryFieldInOrder() {
        assertEquals(
                Arrays.asList(
                        chip("生日", "1988-05-24"),
                        chip("星座", "双子座"),
                        chip("血型", "A型"),
                        chip("身高", "163cm"),
                        chip("胸", "88(D)"),
                        chip("腰", "59"),
                        chip("臀", "85"),
                        chip("出生地", "京都府"),
                        chip("爱好", "ゲーム")
                ),
                hatano().buildChips()
        );
    }

    /**
     * 每枚胶囊都必须带标签 —— 界面把标签渲染成浅色、值渲染成深色，
     * 少了标签就会退化成「一枚只有值的胶囊」，与旁边几枚不成套。
     */
    @Test
    public void everyChipCarriesALabel() {
        for (ActressDetail.Chip c : hatano().buildChips()) {
            assertFalse("标签不该为空: " + c, c.label.isEmpty());
            assertFalse("值不该为空: " + c, c.value.isEmpty());
        }
        assertFalse(hatano().buildCountChip().label.isEmpty());
        assertFalse(hatano().buildReleaseChip().label.isEmpty());
    }

    /** 步兵 / 欧美的响应里生日、血型、三围都缺，出生地与爱好是 null —— 不该产出任何胶囊。 */
    @Test
    public void sparseDataSourceProducesNoChips() {
        ActressDetail detail = new ActressDetail();
        detail.name = "Aubrey James";
        detail.movieCount = 4;
        detail.downloadMovieCount = 4;

        assertEquals("4 部 · 可下载 4", detail.buildCountChip().value);
        assertEquals("", detail.buildReleaseChip().value);
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
                Arrays.asList(chip("生日", "2000-05-20"), chip("星座", "金牛座"), chip("身高", "155cm")),
                detail.buildChips()
        );
    }

    /** 没有罩杯数据时不要拼出空的括号。 */
    @Test
    public void measurementWithoutCupHasNoParentheses() {
        ActressDetail detail = new ActressDetail();
        detail.bust = "88";

        assertEquals(Collections.singletonList(chip("胸", "88")), detail.buildMeasurementChips());
    }

    /** 只有腰和臀时也照样成立，不会产出空的「胸」胶囊。 */
    @Test
    public void measurementDropsMissingParts() {
        ActressDetail detail = new ActressDetail();
        detail.waist = "60";
        detail.hip = "89";

        assertEquals(
                Arrays.asList(chip("腰", "60"), chip("臀", "89")),
                detail.buildMeasurementChips()
        );
    }

    /** 血型字段本身带「型」时不要重复拼接。 */
    @Test
    public void bloodTypeAlreadySuffixedIsNotDoubled() {
        ActressDetail detail = new ActressDetail();
        detail.bloodType = "AB型";

        assertEquals(Collections.singletonList(chip("血型", "AB型")), detail.buildChips());
    }

    /** 没数据时全部返回空，调用方据此隐藏整块。 */
    @Test
    public void emptyDetailProducesNothing() {
        ActressDetail detail = new ActressDetail();

        assertEquals("", detail.buildCountChip().value);
        assertEquals("", detail.buildReleaseChip().value);
        assertTrue(detail.buildCountChip().isEmpty());
        assertTrue(detail.buildReleaseChip().isEmpty());
        assertEquals(Collections.emptyList(), detail.buildMeasurementChips());
        assertEquals(Collections.emptyList(), detail.buildChips());
    }

    /** 只有作品总数、站点没给可下载数时，不要拼出多余的分隔符。 */
    @Test
    public void countChipWithoutDownloadableHasNoSeparator() {
        ActressDetail detail = new ActressDetail();
        detail.movieCount = 12;

        assertEquals("12 部", detail.buildCountChip().value);
    }

    /** Chip 的相等性按标签和值比 —— 断言依赖它，顺带钉住。 */
    @Test
    public void chipEqualityComparesLabelAndValue() {
        assertEquals(chip("生日", "1988-05-24"), chip("生日", "1988-05-24"));
        assertFalse(chip("生日", "1988-05-24").equals(chip("星座", "1988-05-24")));
        assertFalse(chip("生日", "1988-05-24").equals(chip("生日", "1988-05-25")));
    }

    /** 列表里不该出现空值，否则界面会渲染出一枚只有标签的空胶囊。 */
    @Test
    public void chipsListHasNoNulls() {
        List<ActressDetail.Chip> chips = hatano().buildChips();
        for (ActressDetail.Chip c : chips) {
            assertTrue(c != null && !c.isEmpty());
        }
    }
}
