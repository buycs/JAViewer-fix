package io.github.javiewer.adapter;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.javiewer.R;
import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetFile;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.network.provider.DownloadLinkProvider;
import io.github.javiewer.util.MagnetFiles;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Project: JAViewer
 */
public class DownloadLinkAdapter extends ItemAdapter<DownloadLink, DownloadLinkAdapter.ViewHolder> {

    private Activity mParentActivity;

    private DownloadLinkProvider provider;

    private String keyword;

    public DownloadLinkAdapter(List<DownloadLink> links, Activity mParentActivity, DownloadLinkProvider provider) {
        super(links);
        this.mParentActivity = mParentActivity;
        this.provider = provider;
    }

    public DownloadLinkAdapter(List<DownloadLink> links, Activity mParentActivity, DownloadLinkProvider provider, String keyword) {
        super(links);
        this.mParentActivity = mParentActivity;
        this.provider = provider;
        this.keyword = keyword;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_magnet_file, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DownloadLink link = getItems().get(position);

        holder.parse(link);
        holder.filesContainer.setVisibility(View.GONE);
        holder.filesContainer.removeAllViews();
        holder.expandIndicator.setVisibility(View.GONE);

        // Show expand indicator if file list support is available
        if (link.getFiles() != null) {
            holder.expandIndicator.setVisibility(View.VISIBLE);
            holder.expandIndicator.setText(link.filesExpanded ? "▼" : "▶");
            holder.filesContainer.setVisibility(link.filesExpanded ? View.VISIBLE : View.GONE);
            if (link.filesExpanded) {
                bindFileList(holder, link);
            }
        }

        // Expand/collapse click handler
        holder.expandIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If files not loaded yet, fetch detail page first
                if (link.getFiles().isEmpty() && link.getLink() != null) {
                    final AlertDialog mDialog = createLoadingDialog("正在获取文件列表");

                    // Check if this is BtSearch (detail API) or CiliInfo (HTML page)
                    if (provider instanceof io.github.javiewer.network.provider.BtSearchLinkProvider) {
                        // BtSearch: use detail API
                        io.github.javiewer.network.provider.BtSearchLinkProvider btProvider =
                                (io.github.javiewer.network.provider.BtSearchLinkProvider) provider;
                        // Extract torrent ID from link URL
                        long torrentId = extractTorrentId(link.getLink());
                        Call<ResponseBody> call = btProvider.getDetail(torrentId, keyword != null ? keyword : "");
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    String body = response.body().string();
                                    android.util.Log.d("JAViewer", "BtSearch detail response length: " + body.length());
                                    if (body.length() > 0) {
                                        android.util.Log.d("JAViewer", "BtSearch detail first 200 chars: " + body.substring(0, Math.min(200, body.length())));
                                    }
                                    // Try parsing as JSON first
                                    List<MagnetFile> files = btProvider.parseFilesFromJson(body);
                                    if (files.isEmpty()) {
                                        // Fallback to HTML parsing
                                        files = btProvider.parseFilesFromHtml(body);
                                    }
                                    android.util.Log.d("JAViewer", "BtSearch files count: " + files.size());
                                    if (!files.isEmpty()) {
                                        link.setFiles(files);
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("JAViewer", "BtSearch parse error: " + e.getMessage());
                                }
                                mDialog.dismiss();

                                link.filesExpanded = true;
                                holder.expandIndicator.setText("▼");
                                bindFileList(holder, link);
                                holder.filesContainer.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                android.util.Log.e("JAViewer", "BtSearch detail failure: " + t.getMessage());
                                mDialog.dismiss();
                            }
                        });
                    } else {
                        // CiliInfo: use HTML page
                        Call<ResponseBody> call = provider.get(link.getLink());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    String html = response.body().string();
                                    MagnetLink magnetLink = provider.parseMagnetLink(html);
                                    if (magnetLink != null) {
                                        link.setMagnetLink(magnetLink);
                                    }
                                    List<MagnetFile> files = provider.parseFileList(html);
                                    if (files != null && !files.isEmpty()) {
                                        link.setFiles(files);
                                    }
                                    // Update date only if empty
                                    if (link.getDate() == null || link.getDate().isEmpty()) {
                                        String date = provider.parseDate(html);
                                        if (date != null && !date.isEmpty()) {
                                            link.setDate(date);
                                            holder.setDate(date);
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                                mDialog.dismiss();

                                link.filesExpanded = true;
                                holder.expandIndicator.setText("▼");
                                bindFileList(holder, link);
                                holder.filesContainer.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                mDialog.dismiss();
                            }
                        });
                    }
                } else {
                    // Files already loaded, just toggle
                    link.filesExpanded = !link.filesExpanded;
                    holder.expandIndicator.setText(link.filesExpanded ? "▼" : "▶");
                    holder.filesContainer.setVisibility(link.filesExpanded ? View.VISIBLE : View.GONE);
                    if (link.filesExpanded) {
                        bindFileList(holder, link);
                    }
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!link.hasMagnetLink()) {
                    final AlertDialog mDialog = createLoadingDialog("正在获取磁力链接");

                    Call<ResponseBody> call = provider.get(link.getLink());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String html = response.body().string();
                                MagnetLink magnetLink = provider.parseMagnetLink(html);
                                if (magnetLink != null) {
                                    link.setMagnetLink(magnetLink);
                                }

                                // Parse file list if provider supports it
                                List<MagnetFile> files = provider.parseFileList(html);
                                if (files != null && !files.isEmpty()) {
                                    link.setFiles(files);
                                    holder.expandIndicator.setVisibility(View.VISIBLE);
                                    holder.expandIndicator.setText("▶");
                                }

                                // Update date only if empty
                                if (link.getDate() == null || link.getDate().isEmpty()) {
                                    String date = provider.parseDate(html);
                                    if (date != null && !date.isEmpty()) {
                                        link.setDate(date);
                                        holder.setDate(date);
                                    }
                                }

                                onMagnetGet(link.getMagnetLink(), holder);
                            } catch (Throwable e) {
                                onFailure(call, e);
                            }

                            mDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                            mDialog.dismiss();
                        }
                    });
                } else {
                    onMagnetGet(link.getMagnetLink(), holder);
                }
            }
        });
    }

    public void onMagnetGet(final String magnetLink, final ViewHolder holder) {
        if (magnetLink != null && !magnetLink.isEmpty()) {
            final DownloadLink link = getItems().get(holder.getAdapterPosition());

            AlertDialog mDialog = new AlertDialog.Builder(mParentActivity)
                    .setTitle("磁力链接")
                    .setMessage(magnetLink)
                    .setNeutralButton("复制到剪贴板", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager clip = (ClipboardManager) mParentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            clip.setPrimaryClip(ClipData.newPlainText("magnet-link", magnetLink));
                            Toast.makeText(mParentActivity, "磁力链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(magnetLink));
                                mParentActivity.startActivity(intent);
                            } catch (Exception e) {
                                ClipboardManager clip = (ClipboardManager) mParentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                                clip.setPrimaryClip(ClipData.newPlainText("magnet-link", magnetLink));
                                Toast.makeText(mParentActivity, "未找到磁力播放器，已复制链接", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        } else {
            Toast.makeText(mParentActivity, "磁力链接获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindFileList(ViewHolder holder, DownloadLink link) {
        MagnetFiles.bindMediaFileList(
                LayoutInflater.from(mParentActivity),
                holder.filesContainer,
                link.getFiles());
    }

    private long extractTorrentId(String url) {
        try {
            // URL format: https://www.btsearch.love/torrent/123456
            String[] parts = url.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return 0;
        }
    }

    private AlertDialog createLoadingDialog(String message) {
        ProgressBar progressBar = new ProgressBar(mParentActivity);
        progressBar.setIndeterminate(true);
        int padding = (int) (20 * mParentActivity.getResources().getDisplayMetrics().density);
        progressBar.setPadding(padding, padding, padding, padding);

        AlertDialog dialog = new AlertDialog.Builder(mParentActivity)
                .setTitle("请稍后")
                .setView(progressBar)
                .setMessage(message)
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView torrentName;

        public TextView torrentSize;

        public TextView torrentDate;

        public LinearLayout filesContainer;

        public TextView expandIndicator;

        public ViewHolder(View view) {
            super(view);
            torrentName = view.findViewById(R.id.torrent_name);
            torrentSize = view.findViewById(R.id.torrent_size);
            torrentDate = view.findViewById(R.id.torrent_date);
            filesContainer = view.findViewById(R.id.files_container);
            expandIndicator = view.findViewById(R.id.expand_indicator);
        }

        public void parse(DownloadLink link) {
            torrentName.setText(link.getTitle());
            setDate(link.getDate());
            setSize(link.getSize());
        }

        /** 日期为空时隐藏，否则「日期 + 大小」这一行会多出一段空白，和别的源对不齐。 */
        public void setDate(String date) {
            bindMeta(torrentDate, date);
        }

        /** 大小为空时同样隐藏。 */
        public void setSize(String size) {
            bindMeta(torrentSize, size);
        }

        private static void bindMeta(TextView view, String value) {
            boolean empty = value == null || value.trim().isEmpty();
            view.setText(empty ? "" : value);
            view.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }
}
