package io.github.javiewer.activity;

import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import io.github.javiewer.JAViewer;

public class SecureActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        applySecureFlag();
    }

    protected void applySecureFlag() {
        if (JAViewer.CONFIGURATIONS != null && JAViewer.CONFIGURATIONS.isHideRecentPreview()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }
}
