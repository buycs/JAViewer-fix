package io.github.javiewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.javiewer.R;
import io.github.javiewer.adapter.item.TorrentGroup;
import io.github.javiewer.util.MagnetDialog;
import io.github.javiewer.util.MagnetFiles;

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
        bindMeta(holder.torrentDate, group.date);
        bindMeta(holder.torrentSize, MagnetFiles.formatSize(group.totalSize));
        holder.expandIndicator.setText(group.expanded ? "▼" : "▶");

        holder.filesContainer.setVisibility(group.expanded ? View.VISIBLE : View.GONE);
        MagnetFiles.bindMediaFileList(
                LayoutInflater.from(context),
                holder.filesContainer,
                group.files);

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
                MagnetDialog.show(context, group.getMagnetLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups == null ? 0 : groups.size();
    }

    /** 值为空时隐藏该 TextView，避免「日期 + 大小」这一行留下多余空白。 */
    private static void bindMeta(TextView view, String value) {
        boolean empty = value == null || value.trim().isEmpty();
        view.setText(empty ? "" : value);
        view.setVisibility(empty ? View.GONE : View.VISIBLE);
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
