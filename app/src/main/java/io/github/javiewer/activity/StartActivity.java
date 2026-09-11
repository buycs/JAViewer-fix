package io.github.javiewer.activity;

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
        startActivity(new Intent(StartActivity.this, MainActivity.class));
        finish();
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

        readProperties();
    }

}
