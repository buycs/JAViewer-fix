package io.github.javiewer.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


import io.github.javiewer.R;
import io.github.javiewer.activity.DownloadActivity;
import io.github.javiewer.activity.MovieListActivity;
import io.github.javiewer.adapter.item.MovieDetail;
import io.github.javiewer.view.ViewUtil;

/**
 * Project: JAViewer
 */

public class MovieHeaderAdapter extends RecyclerView.Adapter<MovieHeaderAdapter.ViewHolder> {

    public boolean first = true;
    private List<MovieDetail.Header> headers;
    private Activity mParentActivity;
    private ImageView mIcon;

    public MovieHeaderAdapter(List<MovieDetail.Header> headers, Activity mParentActivity, ImageView mIcon) {
        this.headers = headers;
        this.mParentActivity = mParentActivity;
        this.mIcon = mIcon;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_header, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MovieDetail.Header header = headers.get(position);

        if (header.name != null && header.value != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clip = (ClipboardManager) mParentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                    clip.setPrimaryClip(ClipData.newPlainText(header.name, header.value));
                    Toast.makeText(mParentActivity, "已复制到剪贴板", Toast.LENGTH_SHORT).show();

                    return true;
                }
            });

            holder.mHeaderName.setText(header.name);
            holder.mHeaderValue.setText(header.value);

            if (header.getLink() != null) {
                holder.mHeaderValue.setPaintFlags(holder.mHeaderValue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.mHeaderValue.setTextColor(ResourcesCompat.getColor(this.mParentActivity.getResources(), R.color.colorAccent, null));
                    holder.mHeaderValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ("影片番号".equals(header.getName())) {
                                Intent intent = new Intent(mParentActivity, DownloadActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("keyword", header.getValue());
                                intent.putExtras(bundle);
                                mParentActivity.startActivity(intent);
                                return;
                            }
                            String action = "search";
                            if ("导演".equals(header.getName())) action = "director";
                            else if ("制作商".equals(header.getName())) action = "studio";
                            else if ("发行商".equals(header.getName())) action = "label";
                            else if ("系列".equals(header.getName())) action = "series";
                            Intent intent = new Intent(mParentActivity, MovieListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("title", header.getName() + " " + header.getValue());
                            bundle.putString("link", header.getLink());
                            bundle.putString("action", action);
                            intent.putExtras(bundle);
                            mParentActivity.startActivity(intent);
                        }
                    });
            }

            if (first) {
                ViewUtil.alignIconToView(mIcon, holder.mHeaderName);
                first = false;
            }
        }
    }

    @Override
    public int getItemCount() {
        return headers == null ? 0 : headers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mHeaderName;

        public TextView mHeaderValue;

        public ViewHolder(View view) {
            super(view);
            mHeaderName = view.findViewById(R.id.header_name);
            mHeaderValue = view.findViewById(R.id.header_value);
        }
    }
}
