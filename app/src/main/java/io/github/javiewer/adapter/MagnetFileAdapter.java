package io.github.javiewer.adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import io.github.javiewer.R;
import io.github.javiewer.adapter.item.MagnetFile;
import io.github.javiewer.adapter.item.TorrentGroup;

public class MagnetFileAdapter extends RecyclerView.Adapter<MagnetFileAdapter.ViewHolder> {

    private List<TorrentGroup> groups;
    private Context context;

    public MagnetFileAdapter(List<TorrentGroup> groups, Context context) {
        this.groups = groups;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_magnet_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TorrentGroup group = groups.get(position);

        holder.torrentName.setText(group.torrentName);
        holder.torrentDate.setText(group.date != null ? group.date : "");
        holder.torrentSize.setText(formatSize(group.totalSize));
        holder.expandIndicator.setText(group.expanded ? "▼" : "▶");

        holder.filesContainer.setVisibility(group.expanded ? View.VISIBLE : View.GONE);
        holder.filesContainer.removeAllViews();
        for (final MagnetFile file : group.files) {
            View fileView = LayoutInflater.from(context).inflate(R.layout.item_magnet_file, holder.filesContainer, false);
            ((TextView) fileView.findViewById(R.id.file_name)).setText(file.filename);
            ((TextView) fileView.findViewById(R.id.file_size)).setText(formatSize(file.size));
            holder.filesContainer.addView(fileView);
        }

        holder.expandIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                group.expanded = !group.expanded;
                notifyItemChanged(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String magnetLink = group.getMagnetLink();
                if (magnetLink == null || magnetLink.isEmpty()) {
                    Toast.makeText(context, "磁力链接获取失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(context)
                        .setTitle("磁力链接")
                        .setMessage(magnetLink)
                        .setNeutralButton("复制到剪贴板", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                clip.setPrimaryClip(ClipData.newPlainText("magnet-link", magnetLink));
                                Toast.makeText(context, "磁力链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(magnetLink));
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    clip.setPrimaryClip(ClipData.newPlainText("magnet-link", magnetLink));
                                    Toast.makeText(context, "未找到磁力播放器，已复制链接", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }

    private String formatSize(long bytes) {
        if (bytes >= 1073741824) {
            return String.format(Locale.US, "%.1f GB", bytes / 1073741824.0);
        } else if (bytes >= 1048576) {
            return String.format(Locale.US, "%.1f MB", bytes / 1048576.0);
        } else if (bytes >= 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }

    @Override
    public int getItemCount() {
        return groups == null ? 0 : groups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView torrentName;
        public TextView torrentDate;
        public TextView torrentSize;
        public TextView expandIndicator;
        public LinearLayout filesContainer;

        public ViewHolder(View view) {
            super(view);
            torrentName = view.findViewById(R.id.torrent_name);
            torrentDate = view.findViewById(R.id.torrent_date);
            torrentSize = view.findViewById(R.id.torrent_size);
            expandIndicator = view.findViewById(R.id.expand_indicator);
            filesContainer = view.findViewById(R.id.files_container);
        }
    }
}
