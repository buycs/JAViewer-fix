package io.github.javiewer.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import io.github.javiewer.JAViewer;

public class SecureActivity extends AppCompatActivity {

    private static int sStartedCount;

    @Override
    protected void onResume() {
        super.onResume();
        applySecureFlag();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sStartedCount++;
    }

    @Override
    protected void onStop() {
        sStartedCount = Math.max(0, sStartedCount - 1);
        boolean hide = JAViewer.CONFIGURATIONS != null && JAViewer.CONFIGURATIONS.isHideRecentPreview();
        if (sStartedCount == 0 && hide && !isChangingConfigurations()) {
            removeRecentTask();
        }
        super.onStop();
    }

    private void removeRecentTask() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                for (ActivityManager.AppTask task : am.getAppTasks()) {
                    task.finishAndRemoveTask();
                }
            }
        } catch (Exception ignored) {
        }
    }

    protected void applySecureFlag() {
        if (JAViewer.CONFIGURATIONS != null && JAViewer.CONFIGURATIONS.isHideRecentPreview()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }
}
