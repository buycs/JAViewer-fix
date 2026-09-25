package io.github.javiewer.util;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.github.javiewer.R;

/**
 * 磁力链接弹窗：展示链接，并提供「复制 / 打开 / 取消」三个等宽按钮。
 *
 * <p>「女优的作品」页与「磁力链接」页两处共用，避免文案与按钮排布各写一份、日久走偏。
 *
 * <p><b>为什么按钮栏是自己画的</b>：{@link AlertDialog.Builder} 的
 * {@code setPositiveButton / setNegativeButton / setNeutralButton} 只接按钮文字与回调，
 * 不给任何布局控制权 —— 系统把三个按钮按 space-between 摊开，
 * 文字长短不一时视觉重心就会偏（「复制到剪贴板」这种长文案尤其明显）。
 * 这里改成整块内容用 {@code dialog_magnet_actions.xml} 自绘：
 * 三个 Button 各占 1 份 {@code layout_weight}，无论文字多长都严格等宽、左中右间隔一致。
 *
 * <p>用自绘内容区（而不是替换系统按钮栏）是有意为之 ——
 * 替换按钮栏只能反射拿 {@code mButtonPanel} / {@code mAlert} 这类私有字段，
 * 字段名随 AlertDialog 版本变，太脆。自绘内容区走公开 API，稳定。
 */
public class MagnetDialog {

    private MagnetDialog() {
    }

    /**
     * 弹出磁力链接对话框。
     *
     * @param context    调起对话框的 Context
     * @param magnetLink 磁力链接；空则只弹一条「获取失败」提示
     */
    public static void show(final Context context, final String magnetLink) {
        if (magnetLink == null || magnetLink.isEmpty()) {
            Toast.makeText(context, "磁力链接获取失败", Toast.LENGTH_SHORT).show();
            return;
        }

        View content = LayoutInflater.from(context).inflate(R.layout.dialog_magnet_actions, null);
        // setMessage 与自定义内容区同时用时链接会重复显示，所以链接放进自绘布局里
        ((TextView) content.findViewById(R.id.magnet_link_text)).setText(magnetLink);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("磁力链接")
                .setView(content)
                .create();

        content.findViewById(R.id.magnet_action_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(context, magnetLink);
                dialog.dismiss();
            }
        });
        content.findViewById(R.id.magnet_action_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open(context, magnetLink);
                dialog.dismiss();
            }
        });
        content.findViewById(R.id.magnet_action_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /** 复制到剪贴板。 */
    private static void copy(Context context, String magnetLink) {
        ClipboardManager clip =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clip != null) {
            clip.setPrimaryClip(ClipData.newPlainText("magnet-link", magnetLink));
        }
        Toast.makeText(context, "磁力链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    /** 交给外部应用打开；没有可处理的应用时退回复制。 */
    private static void open(Context context, String magnetLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(magnetLink));
            context.startActivity(intent);
        } catch (Exception e) {
            copy(context, magnetLink);
            Toast.makeText(context, "未找到磁力播放器，已复制链接", Toast.LENGTH_SHORT).show();
        }
    }
}
