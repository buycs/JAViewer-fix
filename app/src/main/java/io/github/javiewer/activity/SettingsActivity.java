package io.github.javiewer.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.github.javiewer.BuildConfig;
import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.util.FavouriteBackup;
import io.github.javiewer.util.IOUtils;
import io.github.javiewer.util.ThemeHelper;
import okhttp3.Cache;

public class SettingsActivity extends SecureActivity {

    public static final int REQUEST_IMPORT_FAVOURITES = 2401;

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
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            bindEditDataSourceDomain();
            bindEditMagnetSource();
            bindThemeMode();
            bindCheckUpdate();
            bindClearCache();
            bindExportFavourites();
            bindImportFavourites();
            bindHideRecentPreview();
            bindAbout();
        }

        private void bindEditDataSourceDomain() {
            final Preference preference = findPreference("edit_data_source_domain");
            if (preference == null) {
                return;
            }
            preference.setSummary(sourceSummary(JAViewer.getDataSource()));
            preference.setOnPreferenceClickListener(pref -> {
                showDataSourceEditor(preference);
                return true;
            });
        }

        private void showDataSourceEditor(final Preference preference) {
            final java.util.List<DataSource> sources = JAViewer.DATA_SOURCES;
            final int size = sources.size();
            if (size == 0) {
                Toast.makeText(requireContext(), "数据源不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            final DataSource current = JAViewer.getDataSource();
            float density = getResources().getDisplayMetrics().density;

            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding((int) (24 * density), (int) (8 * density), (int) (24 * density), 0);

            final int[] selectedIndex = {0};
            final RadioButton[] radios = new RadioButton[size];
            final EditText[] inputs = new EditText[size];
            for (int i = 0; i < size; i++) {
                DataSource source = sources.get(i);
                if (source.equals(current)) {
                    selectedIndex[0] = i;
                }
                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);

                final int index = i;
                RadioButton radio = new RadioButton(requireContext());
                radio.setText(source.getName());
                radio.setOnCheckedChangeListener((v, checked) -> {
                    if (!checked) {
                        return;
                    }
                    selectedIndex[0] = index;
                    for (int j = 0; j < radios.length; j++) {
                        if (j != index && radios[j] != null) {
                            radios[j].setChecked(false);
                        }
                    }
                });
                radios[i] = radio;

                EditText input = new EditText(requireContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                String domain = source.domain == null ? "" : source.domain;
                input.setText(domain);
                input.setSelection(input.getText().length());
                input.setSingleLine(true);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                input.setLayoutParams(lp);
                inputs[i] = input;

                row.addView(radio);
                row.addView(input);
                layout.addView(row);
            }
            radios[selectedIndex[0]].setChecked(true);

            new AlertDialog.Builder(requireContext())
                    .setTitle("数据源配置")
                    .setView(layout)
                    .setPositiveButton("保存", (dialog, which) -> {
                        boolean changed = !sources.get(selectedIndex[0]).equals(current);
                        for (int i = 0; i < size; i++) {
                            DataSource source = sources.get(i);
                            String newDomain = inputs[i].getText().toString().trim();
                            if (!newDomain.isEmpty() && !newDomain.equals(source.domain)) {
                                source.domain = newDomain;
                                changed = true;
                            }
                        }
                        if (!changed) {
                            return;
                        }
                        Map<String, String> savedDomains = JAViewer.CONFIGURATIONS.getDataSourceDomains();
                        savedDomains.clear();
                        for (DataSource source : sources) {
                            savedDomains.put(source.getName(), source.domain);
                        }
                        JAViewer.CONFIGURATIONS.setDataSource(sources.get(selectedIndex[0]));
                        JAViewer.CONFIGURATIONS.save();
                        JAViewer.recreateService();
                        preference.setSummary(sourceSummary(sources.get(selectedIndex[0])));
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).restart();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }

        private void bindEditMagnetSource() {
            final Preference preference = findPreference("edit_magnet_source_domain");
            if (preference == null) {
                return;
            }
            preference.setSummary(magnetSummary());
            preference.setOnPreferenceClickListener(pref -> {
                showMagnetSourceEditor(preference);
                return true;
            });
        }

        private void showMagnetSourceEditor(final Preference preference) {
            final io.github.javiewer.Configurations config = JAViewer.CONFIGURATIONS;
            float density = getResources().getDisplayMetrics().density;
            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding((int) (24 * density), (int) (8 * density), (int) (24 * density), 0);

            final EditText btInput = magnetInput(config.getMagnetSourceBtsearch());
            final EditText ciliInput = magnetInput(config.getMagnetSourceCili());
            final EditText btsowInput = magnetInput(config.getMagnetSourceBtsow());
            layout.addView(magnetLabel("BtSearch"));
            layout.addView(btInput);
            layout.addView(magnetLabel("无极磁链"));
            layout.addView(ciliInput);
            layout.addView(magnetLabel("btsow"));
            layout.addView(btsowInput);

            new AlertDialog.Builder(requireContext())
                    .setTitle("磁力源配置")
                    .setView(layout)
                    .setPositiveButton("保存", (dialog, which) -> {
                        config.setMagnetSourceBtsearch(btInput.getText().toString().trim());
                        config.setMagnetSourceCili(ciliInput.getText().toString().trim());
                        config.setMagnetSourceBtsow(btsowInput.getText().toString().trim());
                        config.save();
                        preference.setSummary(magnetSummary());
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }

        private EditText magnetInput(String value) {
            EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            input.setText(value);
            input.setSelection(input.getText().length());
            return input;
        }

        private TextView magnetLabel(String text) {
            TextView label = new TextView(requireContext());
            label.setText(text);
            return label;
        }

        private static String magnetSummary() {
            io.github.javiewer.Configurations config = JAViewer.CONFIGURATIONS;
            return "当前：" + shortHost(config.getMagnetSourceBtsearch())
                    + " · " + shortHost(config.getMagnetSourceCili())
                    + " · " + shortHost(config.getMagnetSourceBtsow());
        }

        private static String shortHost(String url) {
            return url.replaceFirst("^https?://", "");
        }

        private static String sourceSummary(DataSource source) {
            if (source == null) {
                return "切换骑兵、步兵或欧美数据源";
            }
            String name = source.getName() != null ? source.getName() : "";
            if (source.domain == null || source.domain.isEmpty()) {
                return "当前：" + name;
            }
            return "当前：" + name + " · " + source.domain;
        }

        private void bindThemeMode() {
            ListPreference preference = findPreference("theme_mode");
            if (preference == null || JAViewer.CONFIGURATIONS == null) {
                return;
            }
            CharSequence[] values = {ThemeHelper.MODE_SYSTEM, ThemeHelper.MODE_LIGHT, ThemeHelper.MODE_DARK};
            CharSequence[] names = new CharSequence[values.length];
            for (int i = 0; i < values.length; i++) {
                names[i] = ThemeHelper.displayName(values[i].toString());
            }
            preference.setEntries(names);
            preference.setEntryValues(values);
            String current = JAViewer.CONFIGURATIONS.getThemeMode();
            preference.setValue(current);
            preference.setSummary("当前：" + ThemeHelper.displayName(current));
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                String mode = String.valueOf(newValue);
                if (mode.equals(JAViewer.CONFIGURATIONS.getThemeMode())) {
                    return true;
                }
                JAViewer.CONFIGURATIONS.setThemeMode(mode);
                JAViewer.CONFIGURATIONS.save();
                ThemeHelper.apply(mode);
                pref.setSummary("当前：" + ThemeHelper.displayName(mode));
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).restart();
                } else if (getActivity() != null) {
                    getActivity().recreate();
                }
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

        private void bindExportFavourites() {
            Preference preference = findPreference("export_favourites");
            if (preference == null) {
                return;
            }
            preference.setOnPreferenceClickListener(pref -> {
                exportFavourites();
                return true;
            });
        }

        private void exportFavourites() {
            if (JAViewer.CONFIGURATIONS == null) {
                Toast.makeText(requireContext(), "导出失败", Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.List<?> movies = JAViewer.CONFIGURATIONS.getStarredMovies();
            java.util.List<?> actresses = JAViewer.CONFIGURATIONS.getStarredActresses();
            boolean noMovies = movies == null || movies.isEmpty();
            boolean noActresses = actresses == null || actresses.isEmpty();
            if (noMovies && noActresses) {
                Toast.makeText(requireContext(), "收藏为空，无需导出", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File dir = writableExportDir();
                if (dir == null) {
                    Toast.makeText(requireContext(), "导出失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = uniqueExportFile(dir);
                String json = FavouriteBackup.toJson(
                        JAViewer.CONFIGURATIONS.getStarredMovies(),
                        JAViewer.CONFIGURATIONS.getStarredActresses());
                try (FileOutputStream fos = new FileOutputStream(file);
                     OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    writer.write(json);
                    writer.flush();
                }
                Toast.makeText(requireContext(), "已保存到 " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "导出失败", Toast.LENGTH_SHORT).show();
            }
        }

        private File writableExportDir() {
            File appDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (downloads != null) {
                File publicDir = new File(downloads, "JAViewer");
                if ((publicDir.exists() || publicDir.mkdirs()) && publicDir.canWrite()) {
                    return publicDir;
                }
            }
            if (appDir != null && (appDir.exists() || appDir.mkdirs())) {
                return appDir;
            }
            return requireContext().getFilesDir();
        }

        private File uniqueExportFile(File dir) {
            File file = new File(dir, "javiewer-favourites.json");
            if (!file.exists()) {
                return file;
            }
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US);
            return new File(dir, "javiewer-favourites-" + format.format(new java.util.Date()) + ".json");
        }

        private void bindImportFavourites() {
            Preference preference = findPreference("import_favourites");
            if (preference == null) {
                return;
            }
            preference.setOnPreferenceClickListener(pref -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                Intent chooser = Intent.createChooser(intent, "导入收藏");
                Intent fallback = new Intent(Intent.ACTION_GET_CONTENT);
                fallback.addCategory(Intent.CATEGORY_OPENABLE);
                fallback.setType("*/*");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{fallback});
                startActivityForResult(chooser, REQUEST_IMPORT_FAVOURITES);
                return true;
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode != REQUEST_IMPORT_FAVOURITES || resultCode != android.app.Activity.RESULT_OK || data == null) {
                return;
            }
            Uri uri = data.getData();
            if (uri == null || JAViewer.CONFIGURATIONS == null) {
                Toast.makeText(requireContext(), "导入失败", Toast.LENGTH_SHORT).show();
                return;
            }
            try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                if (is == null) {
                    Toast.makeText(requireContext(), "导入失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                String json = IOUtils.readText(is, IOUtils.UTF_8);
                FavouriteBackup.ImportResult result = FavouriteBackup.mergeJson(
                        json,
                        JAViewer.CONFIGURATIONS.getStarredMovies(),
                        JAViewer.CONFIGURATIONS.getStarredActresses());
                JAViewer.CONFIGURATIONS.save();
                FavouriteActivity.update();
                Toast.makeText(requireContext(),
                        "已导入 " + result.moviesAdded + " 部影片、" + result.actressesAdded + " 位女优",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "导入失败", Toast.LENGTH_SHORT).show();
            }
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
            preference.setTitle("关于");
            preference.setSummary(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
            preference.setOnPreferenceClickListener(pref -> {
                showAboutDialog();
                return true;
            });
        }

        private void showAboutDialog() {
            CharSequence message = HtmlCompat.fromHtml(
                    "版本：" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")<br/><br/>"
                            + "<a href=\"https://github.com/SplashCodes/JAViewer\">原项目源码</a><br/><br/>"
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
