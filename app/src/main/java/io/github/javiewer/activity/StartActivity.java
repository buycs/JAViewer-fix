package io.github.javiewer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import androidx.appcompat.app.AppCompatActivity;
import io.github.javiewer.Configurations;
import io.github.javiewer.JAViewer;
import io.github.javiewer.Properties;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.util.IOUtils;
import io.github.javiewer.util.ThemeHelper;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loadConfigurations();
    }

    public void readProperties() {
        try (InputStream is = getAssets().open("properties.json")) {
            Properties properties = JAViewer.parseJson(Properties.class, IOUtils.readText(is, IOUtils.UTF_8));
            if (properties != null) {
                handleProperties(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleProperties(Properties properties) {
        JAViewer.DATA_SOURCES.clear();
        JAViewer.DATA_SOURCES.addAll(properties.getDataSources());
        JAViewer.CONFIGURATIONS.applyDataSourceDomains(JAViewer.DATA_SOURCES);

        JAViewer.hostReplacements.clear();
        for (DataSource source : JAViewer.DATA_SOURCES) {
            try {
                String host = new URI(source.getLink()).getHost();
                for (String h : source.legacies) {
                    JAViewer.hostReplacements.put(h, host);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        start();

    }

    public void start() {
        boolean hide = JAViewer.CONFIGURATIONS != null && JAViewer.CONFIGURATIONS.isHideRecentPreview();
        closeStaleTasks(hide);
        Intent intent = new Intent(StartActivity.this, hide ? HiddenMainActivity.class : MainActivity.class);
        if (hide) {
            // excludeFromRecents 只对任务根生效：必须 NEW_TASK 让 HiddenMainActivity 自己成为任务根
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
        finish();
    }

    /** 设置与已有任务类型不匹配时（开→关 / 关→开 后首次进入），结束旧任务，避免残留无法关闭的隐藏任务。 */
    private void closeStaleTasks(boolean hide) {
        android.app.ActivityManager am =
                (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }
        int myTask = getTaskId();
        for (android.app.ActivityManager.AppTask task : am.getAppTasks()) {
            android.app.TaskInfo info = task.getTaskInfo();
            if (info == null || info.taskId == myTask || info.baseActivity == null) {
                continue;
            }
            boolean taskIsHidden = HiddenMainActivity.class.getName()
                    .equals(info.baseActivity.getClassName());
            if (taskIsHidden != hide) {
                task.finishAndRemoveTask();
            }
        }
    }

    private void loadConfigurations() {
        File oldConfig = new File(StartActivity.this.getExternalFilesDir(null), "configurations.json");
        File config = new File(JAViewer.getStorageDir(), "configurations.json");
        if (oldConfig.exists()) {
            oldConfig.renameTo(config);
        }

        File noMedia = new File(JAViewer.getStorageDir(), ".nomedia");
        try {
            noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JAViewer.CONFIGURATIONS = Configurations.load(config);
        ThemeHelper.apply(JAViewer.CONFIGURATIONS.getThemeMode());

        readProperties();
    }

}
