package io.github.javiewer.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.bumptech.glide.Glide;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.InputStream;

import io.github.javiewer.BuildConfig;
import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.util.IOUtils;
import okhttp3.Cache;

public class SettingsActivity extends SecureActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            bindDataSource();
            bindCheckUpdate();
            bindClearCache();
            bindHideRecentPreview();
            bindAbout();
        }

        private void bindDataSource() {
            ListPreference preference = findPreference("data_source");
            if (preference == null || JAViewer.DATA_SOURCES.isEmpty()) {
                return;
            }

            int size = JAViewer.DATA_SOURCES.size();
            CharSequence[] names = new CharSequence[size];
            CharSequence[] values = new CharSequence[size];
            DataSource current = JAViewer.getDataSource();
            String selected = "0";
            for (int i = 0; i < size; i++) {
                DataSource source = JAViewer.DATA_SOURCES.get(i);
                names[i] = source.getName();
                values[i] = String.valueOf(i);
                if (source.equals(current)) {
                    selected = String.valueOf(i);
                }
            }
            preference.setEntries(names);
            preference.setEntryValues(values);
            preference.setValue(selected);
            preference.setSummary(current != null ? current.getName() : null);
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                int index = Integer.parseInt(String.valueOf(newValue));
                if (index < 0 || index >= JAViewer.DATA_SOURCES.size()) {
                    return false;
                }
                DataSource source = JAViewer.DATA_SOURCES.get(index);
                if (source.equals(JAViewer.getDataSource())) {
                    return true;
                }
                JAViewer.CONFIGURATIONS.setDataSource(source);
                JAViewer.CONFIGURATIONS.save();
                JAViewer.recreateService();
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
                return true;
            });
        }

        private void bindCheckUpdate() {
            Preference preference = findPreference("check_update");
            if (preference == null) {
                return;
            }
            preference.setOnPreferenceClickListener(pref -> {
                checkUpdate();
                return true;
            });
        }

        private void checkUpdate() {
            try (InputStream is = requireContext().getAssets().open("properties.json")) {
                String json = IOUtils.readText(is, IOUtils.UTF_8);
                JsonObject object = JsonParser.parseString(json).getAsJsonObject();
                int latest = readVersionCode(object.get("latest_version_code"));
                String changelog = "";
                if (object.has("changelog") && !object.get("changelog").isJsonNull()) {
                    changelog = object.get("changelog").getAsString();
                }
                if (BuildConfig.VERSION_CODE >= latest) {
                    Toast.makeText(requireContext(), "已是最新版本", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("发现新版本")
                        .setMessage(changelog)
                        .setPositiveButton("打开", (dialog, which) -> openUrl("https://github.com/buycs/JAViewer-fix"))
                        .setNegativeButton("取消", null)
                        .show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "检查更新失败", Toast.LENGTH_SHORT).show();
            }
        }

        private static int readVersionCode(JsonElement element) {
            if (element == null || !element.isJsonPrimitive()) {
                return 0;
            }
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsInt();
            }
            try {
                return Integer.parseInt(primitive.getAsString().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private void bindClearCache() {
            Preference preference = findPreference("clear_cache");
            if (preference == null) {
                return;
            }
            preference.setOnPreferenceClickListener(pref -> {
                final android.app.Activity activity = requireActivity();
                new Thread(() -> {
                    Glide.get(activity.getApplicationContext()).clearDiskCache();
                    Cache cache = JAViewer.HTTP_CLIENT.cache();
                    if (cache != null) {
                        try {
                            cache.evictAll();
                        } catch (IOException ignored) {
                        }
                    }
                    activity.runOnUiThread(() -> {
                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            Toast.makeText(activity, "缓存已清除", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
                return true;
            });
        }

        private void bindHideRecentPreview() {
            SwitchPreferenceCompat preference = findPreference("hide_recent_preview");
            if (preference == null) {
                return;
            }
            boolean hide = JAViewer.CONFIGURATIONS != null && JAViewer.CONFIGURATIONS.isHideRecentPreview();
            preference.setChecked(hide);
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                boolean enabled = Boolean.TRUE.equals(newValue);
                if (JAViewer.CONFIGURATIONS != null) {
                    JAViewer.CONFIGURATIONS.setHideRecentPreview(enabled);
                    JAViewer.CONFIGURATIONS.save();
                }
                if (getActivity() instanceof SecureActivity) {
                    ((SecureActivity) getActivity()).applySecureFlag();
                }
                return true;
            });
        }

        private void bindAbout() {
            Preference preference = findPreference("about");
            if (preference == null) {
                return;
            }
            preference.setTitle(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
            preference.setSummary("本应用仅供学习交流，请勿用于非法用途");
            preference.setOnPreferenceClickListener(pref -> {
                showAboutDialog();
                return true;
            });
        }

        private void showAboutDialog() {
            CharSequence message = HtmlCompat.fromHtml(
                    "版本：" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")<br/><br/>"
                            + "<a href=\"https://github.com/SplashCodes/JAViewer\">原项目源码</a><br/>"
                            + "<a href=\"https://github.com/buycs/JAViewer-fix\">本项目源码</a><br/><br/>"
                            + "免责声明：本应用仅供学习交流，请勿用于非法用途。",
                    HtmlCompat.FROM_HTML_MODE_LEGACY);
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("关于")
                    .setMessage(message)
                    .setPositiveButton("确定", null)
                    .show();
            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        private void openUrl(@NonNull String url) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "无法打开链接", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
