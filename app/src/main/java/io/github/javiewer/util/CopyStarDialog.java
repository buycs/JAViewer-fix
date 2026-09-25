package io.github.javiewer.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.activity.FavouriteActivity;

/**
 * 长按菜单弹窗：「复制 / 收藏」左右并排两个等宽按钮。
 *
 * <p>女优卡片与影片卡片两处长按共用，避免文案与排布各写一份、日久走偏。
 *
 * <p><b>为什么不用 {@code setItems()}</b>：那是竖向列表，两个动作各占一行，
 * 又高又散。这里用 {@link AlertDialog.Builder#setView(View)} 自绘一行两个等宽按钮，
 * 与磁力弹窗（{@link MagnetDialog}）保持一致的观感。
 *
 * @param <T> 收藏条目的类型（女优 / 影片）
 */
public class CopyStarDialog<T> {

    /** 从条目取出要复制到剪贴板的文本。 */
    public interface TextProvider<T> {
        String text(T item);
    }

    private CopyStarDialog() {
    }

    /**
     * 弹出「复制 / 收藏」对话框。
     *
     * @param activity      调起对话框的 Activity
     * @param title         对话框标题（通常是条目名）
     * @param item          当前条目
     * @param starred       已收藏的条目列表（直接增删，调用方负责后续 save）
     * @param textProvider  取复制文本
     * @param copyLabel     剪贴板标签（仅用于系统剪贴板内部标记）
     */
    public static <T> void show(final Activity activity,
                               final String title,
                               final T item,
                               final List<T> starred,
                               final TextProvider<T> textProvider,
                               final String copyLabel) {
        final boolean contain = starred.contains(item);

        View content = LayoutInflater.from(activity).inflate(R.layout.dialog_copy_star_actions, null);

        final AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(content)
                .create();

        ((Button) content.findViewById(R.id.action_star)).setText(contain ? "取消收藏" : "收藏");

        content.findViewById(R.id.action_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clip =
                        (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clip != null) {
                    clip.setPrimaryClip(ClipData.newPlainText(copyLabel, textProvider.text(item)));
                }
                Toast.makeText(activity, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        content.findViewById(R.id.action_star).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contain) {
                    starred.remove(item);
                    Toast.makeText(activity, "已取消收藏", Toast.LENGTH_SHORT).show();
                } else {
                    // 保持原有行为：新收藏追加到列表末尾
                    java.util.Collections.reverse(starred);
                    starred.add(item);
                    java.util.Collections.reverse(starred);
                    Toast.makeText(activity, "已收藏", Toast.LENGTH_SHORT).show();
                }
                JAViewer.CONFIGURATIONS.save();
                FavouriteActivity.update();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
