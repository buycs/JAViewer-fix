package io.github.javiewer.adapter;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;


import io.github.javiewer.R;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.view.SquareTopCrop;
import io.github.javiewer.view.listener.ActressClickListener;
import io.github.javiewer.view.listener.ActressLongClickListener;

/**
 * Project: JAViewer
 */
public class ActressAdapter extends ItemAdapter<Actress, ActressAdapter.ViewHolder> {

    private Activity mParentActivity;

    public ActressAdapter(List<Actress> actresses, Activity mParentActivity) {
        super(actresses);
        this.mParentActivity = mParentActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_actress, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Actress actress = getItems().get(position);

        holder.parse(actress);

        holder.mLayout.setOnClickListener(new ActressClickListener(actress, mParentActivity));
        holder.mLayout.setOnLongClickListener(new ActressLongClickListener(actress, mParentActivity));

        holder.mImage.setImageDrawable(null);
        Glide.with(holder.mImage.getContext().getApplicationContext())
                .load(actress.getImageUrl())
                .placeholder(R.drawable.ic_movie_actresses)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(true) // do not reuse the transformed result while running
                .transform(new SquareTopCrop())
                .dontAnimate()
                .into(holder.mImage);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextName;

        public TextView mTextMovieCount;

        public ImageView mImage;

        public View mLayout;

        public ViewHolder(View view) {
            super(view);

            mTextName = view.findViewById(R.id.actress_name);
            mTextMovieCount = view.findViewById(R.id.actress_movie_count);
            mImage = view.findViewById(R.id.actress_img);
            mLayout = view.findViewById(R.id.layout_actress);
        }

        public void parse(Actress actress) {
            mTextName.setText(actress.getName());
            mTextName.setSelected(true);

            // 数量未知时不显示，避免旧收藏数据出现「0 部」
            int count = actress.getMovieCount();
            if (count > 0) {
                mTextMovieCount.setText(count + " 部");
                mTextMovieCount.setVisibility(View.VISIBLE);
            } else {
                mTextMovieCount.setText("");
                mTextMovieCount.setVisibility(View.GONE);
            }
        }
    }
}
