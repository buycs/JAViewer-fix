package io.github.javiewer;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.javiewer.adapter.item.Movie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationsSaveTest {

    private File dir;
    private File configFile;

    @Before
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("javiewer-config").toFile();
        configFile = new File(dir, "configurations.json");
        setConfigFile(configFile);
    }

    @After
    public void tearDown() throws Exception {
        setConfigFile(null);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File child : files) {
                child.delete();
            }
        }
        dir.delete();
    }

    @Test
    public void saveThenReloadPreservesStarredMovie() throws Exception {
        Configurations config = new Configurations();
        config.getStarredMovies().add(Movie.create("标题", "ABC-123", "2026-01-01", "http://cover", "http://detail", false));
        config.save();

        assertTrue(configFile.exists());
        assertFalse(new File(configFile.getAbsolutePath() + ".tmp").exists());

        Configurations reloaded = readConfig(configFile);
        assertEquals(1, reloaded.getStarredMovies().size());
        assertEquals("ABC-123", reloaded.getStarredMovies().get(0).getCode());
        assertEquals("标题", reloaded.getStarredMovies().get(0).getTitle());
    }

    @Test
    public void concurrentSaveLeavesReadableJson() throws Exception {
        Configurations config = new Configurations();
        config.getStarredMovies();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(8);
        AtomicInteger failures = new AtomicInteger();

        for (int t = 0; t < 8; t++) {
            final int index = t;
            new Thread(() -> {
                try {
                    start.await();
                    synchronized (config) {
                        config.getStarredMovies().add(Movie.create("t" + index, "CODE-" + index, "2026-01-01", null, "link-" + index, false));
                    }
                    config.save();
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertEquals(0, failures.get());

        Configurations reloaded = readConfig(configFile);
        assertTrue(reloaded.getStarredMovies().size() >= 1);
        assertTrue(configFile.length() > 2);
        String json = new String(Files.readAllBytes(configFile.toPath()));
        assertTrue(json.contains("starred_movies"));
    }

    @Test
    public void saveThenReloadPreservesHideRecentPreview() throws Exception {
        Configurations config = new Configurations();
        assertFalse(config.isHideRecentPreview());
        config.setHideRecentPreview(true);
        config.save();

        Configurations reloaded = readConfig(configFile);
        assertTrue(reloaded.isHideRecentPreview());
        String json = new String(Files.readAllBytes(configFile.toPath()));
        assertTrue(json.contains("hide_recent_preview"));
    }

    @Test
    public void overwriteKeepsLastSave() throws Exception {
        Configurations config = new Configurations();
        config.getStarredMovies().add(Movie.create("one", "ONE-1", null, null, "l1", false));
        config.save();

        config.getStarredMovies().clear();
        config.getStarredMovies().add(Movie.create("two", "TWO-2", null, null, "l2", false));
        config.save();

        Configurations reloaded = readConfig(configFile);
        ArrayList<Movie> movies = reloaded.getStarredMovies();
        assertEquals(1, movies.size());
        assertEquals("TWO-2", movies.get(0).getCode());
    }

    private static void setConfigFile(File file) throws Exception {
        Field field = Configurations.class.getDeclaredField("file");
        field.setAccessible(true);
        field.set(null, file);
    }

    private static Configurations readConfig(File file) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Configurations config = new Gson().fromJson(reader, Configurations.class);
            return config != null ? config : new Configurations();
        }
    }
}
