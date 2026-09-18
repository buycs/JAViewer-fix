package io.github.javiewer;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import io.github.javiewer.util.ThemeHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigurationsThemeTest {

    private File dir;
    private File configFile;

    @Before
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("javiewer-theme").toFile();
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
    public void defaultThemeModeIsSystem() {
        assertEquals(ThemeHelper.MODE_SYSTEM, new Configurations().getThemeMode());
    }

    @Test
    public void saveThenReloadPreservesThemeMode() throws Exception {
        Configurations config = new Configurations();
        config.setThemeMode(ThemeHelper.MODE_DARK);
        config.save();

        String json = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(json.contains("theme_mode"));

        Configurations reloaded = new Gson().fromJson(
                new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8),
                Configurations.class);
        assertEquals(ThemeHelper.MODE_DARK, reloaded.getThemeMode());
    }

    @Test
    public void invalidThemeModeFallsBackToSystem() {
        Configurations config = new Configurations();
        config.setThemeMode("nonsense");
        assertEquals(ThemeHelper.MODE_SYSTEM, config.getThemeMode());

        config.setThemeMode(ThemeHelper.MODE_LIGHT);
        assertEquals(ThemeHelper.MODE_LIGHT, config.getThemeMode());
    }

    @Test
    public void displayNameCoversAllModes() {
        assertEquals("跟随系统", ThemeHelper.displayName(ThemeHelper.MODE_SYSTEM));
        assertEquals("浅色", ThemeHelper.displayName(ThemeHelper.MODE_LIGHT));
        assertEquals("深色", ThemeHelper.displayName(ThemeHelper.MODE_DARK));
        assertEquals("跟随系统", ThemeHelper.displayName("garbage"));
    }

    private static void setConfigFile(File file) throws Exception {
        Field field = Configurations.class.getDeclaredField("file");
        field.setAccessible(true);
        field.set(null, file);
    }
}
