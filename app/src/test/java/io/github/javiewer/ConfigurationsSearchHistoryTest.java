package io.github.javiewer;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationsSearchHistoryTest {

    @Test
    public void getSearchHistoryNeverNull() {
        Configurations config = new Configurations();
        assertNotNull(config.getSearchHistory());
        assertTrue(config.getSearchHistory().isEmpty());
    }

    @Test
    public void addSearchHistoryIgnoresNullAndBlank() {
        Configurations config = new Configurations();
        config.addSearchHistory(null);
        config.addSearchHistory("");
        config.addSearchHistory("   ");
        assertTrue(config.getSearchHistory().isEmpty());
    }

    @Test
    public void addSearchHistoryDedupesIgnoreCaseAndMovesToFront() {
        Configurations config = new Configurations();
        config.addSearchHistory("abp-123");
        config.addSearchHistory("hello");
        config.addSearchHistory("ABP-123");

        ArrayList<String> history = config.getSearchHistory();
        assertEquals(2, history.size());
        assertEquals("ABP-123", history.get(0));
        assertEquals("hello", history.get(1));
    }

    @Test
    public void addSearchHistoryCapsAtTwenty() {
        Configurations config = new Configurations();
        for (int i = 0; i < 25; i++) {
            config.addSearchHistory("q" + i);
        }
        ArrayList<String> history = config.getSearchHistory();
        assertEquals(20, history.size());
        assertEquals("q24", history.get(0));
        assertEquals("q5", history.get(19));
    }

    @Test
    public void clearSearchHistoryEmptiesList() {
        Configurations config = new Configurations();
        config.addSearchHistory("a");
        config.addSearchHistory("b");
        config.clearSearchHistory();
        assertTrue(config.getSearchHistory().isEmpty());
        config.addSearchHistory("c");
        assertEquals(1, config.getSearchHistory().size());
        assertEquals("c", config.getSearchHistory().get(0));
    }
}
