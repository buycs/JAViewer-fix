package io.github.javiewer.util;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    public static final String MODE_SYSTEM = "system";
    public static final String MODE_LIGHT = "light";
    public static final String MODE_DARK = "dark";

    public static void apply(String themeMode) {
        int nightMode;
        if (MODE_LIGHT.equals(themeMode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (MODE_DARK.equals(themeMode)) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public static String displayName(String themeMode) {
        if (MODE_LIGHT.equals(themeMode)) {
            return "浅色";
        } else if (MODE_DARK.equals(themeMode)) {
            return "深色";
        } else {
            return "跟随系统";
        }
    }
}
