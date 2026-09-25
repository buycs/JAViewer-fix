package io.github.javiewer.view.listener;

import android.app.Activity;
import android.view.View;

import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.util.CopyStarDialog;

/**
 * Project: JAViewer
 */

public class ActressLongClickListener implements View.OnLongClickListener {

    private Activity mActivity;
    private Actress actress;

    public ActressLongClickListener(Actress actress, Activity mActivity) {
        this.actress = actress;
        this.mActivity = mActivity;
    }

    @Override
    public boolean onLongClick(View v) {
        final List<Actress> actresses = JAViewer.CONFIGURATIONS.getStarredActresses();
        CopyStarDialog.show(mActivity, actress.getName(), actress, actresses,
                new CopyStarDialog.TextProvider<Actress>() {
                    @Override
                    public String text(Actress item) {
                        return item.getName();
                    }
                }, "actress");
        return true;
    }
}
