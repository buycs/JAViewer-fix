package io.github.javiewer.adapter.item;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;

import io.github.javiewer.util.FavouriteBackup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 女优作品数量字段。
 *
 * <p>这个字段是后加的，而 {@link Actress} 会被 Gson 序列化进收藏配置与导出文件，
 * 所以必须保证「老数据没有这个字段」时不会出问题。
 */
public class ActressTest {

    @Test
    public void createWithMovieCount() {
        Actress actress = Actress.create("波多野結衣", "http://img", "starId", 4617);

        assertEquals("波多野結衣", actress.getName());
        assertEquals("http://img", actress.getImageUrl());
        assertEquals("starId", actress.getLink());
        assertEquals(4617, actress.getMovieCount());
    }

    /** 详情页里的女优列表拿不到数量，走的是这个重载，数量应为 0（调用方据此不展示）。 */
    @Test
    public void createWithoutMovieCountDefaultsToZero() {
        assertEquals(0, Actress.create("某女优", null, "http://a").getMovieCount());
    }

    @Test
    public void movieCountSurvivesGsonRoundTrip() {
        Actress original = Actress.create("波多野結衣", "http://img", "starId", 4617);

        Actress restored = new Gson().fromJson(new Gson().toJson(original), Actress.class);

        assertNotNull(restored);
        assertEquals("波多野結衣", restored.getName());
        assertEquals(4617, restored.getMovieCount());
    }

    /** 升级前存的收藏里没有 movieCount 字段，反序列化后应为 0 而不是抛异常。 */
    @Test
    public void legacyJsonWithoutMovieCountParsesAsZero() {
        String legacy = "{\"name\":\"旧女优\",\"imageUrl\":null,\"link\":\"http://a\"}";

        Actress restored = new Gson().fromJson(legacy, Actress.class);

        assertNotNull(restored);
        assertEquals("旧女优", restored.getName());
        assertEquals(0, restored.getMovieCount());
    }

    /** 走一遍真实的收藏导入路径，确认老备份文件仍能正常导入。 */
    @Test
    public void legacyFavouriteBackupImportsWithZeroCount() {
        String legacyBackup = "{\"movies\":[],\"actresses\":["
                + "{\"name\":\"旧女优\",\"imageUrl\":null,\"link\":\"http://a\"}]}";
        ArrayList<Actress> dest = new ArrayList<>();

        FavouriteBackup.ImportResult result =
                FavouriteBackup.mergeJson(legacyBackup, new ArrayList<>(), dest);

        assertEquals(1, result.actressesAdded);
        assertEquals(1, dest.size());
        assertEquals("旧女优", dest.get(0).getName());
        assertEquals(0, dest.get(0).getMovieCount());
    }
}
