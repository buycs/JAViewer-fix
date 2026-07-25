package io.github.javiewer.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.palette.graphics.Palette;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;


import io.github.javiewer.R;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.view.SquareTopCrop;
import io.github.javiewer.view.ViewUtil;
import io.github.javiewer.view.listener.ActressClickListener;
import io.github.javiewer.view.listener.ActressLongClickListener;

/**
 * Project: JAViewer
 */

public class ActressPaletteAdapter extends RecyclerView.Adapter<ActressPaletteAdapter.ViewHolder> {

    private List<Actress> actresses;

    private Activity mParentActivity;

    private ImageView mIcon;

    public ActressPaletteAdapter(List<Actress> actresses, Activity mParentActivity, ImageView mIcon) {
        this.actresses = actresses;
        this.mParentActivity = mParentActivity;
        this.mIcon = mIcon;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_actress_palette, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Actress actress = actresses.get(position);

        holder.mCard.setOnClickListener(new ActressClickListener(actress, mParentActivity));
        holder.mCard.setOnLongClickListener(new ActressLongClickListener(actress, mParentActivity));

        holder.mName.setText(actress.getName());

        if (position == 0) {
            ViewUtil.alignIconToView(mIcon, holder.mImage);
        }

        holder.mImage.setImageResource(R.drawable.ic_movie_actresses);

        if (actress.getImageUrl().trim().isEmpty()) {
            return;
        }

        Glide.with(holder.mImage.getContext().getApplicationContext())
                .asBitmap()
                .load(actress.getImageUrl())
                .placeholder(R.drawable.ic_movie_actresses)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(true)
                .transform(new SquareTopCrop())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        holder.mImage.setImageBitmap(resource);

                        try {
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getLightVibrantSwatch();
                                    if (swatch == null) {
                                        return;
                                    }
                                    holder.mCard.setCardBackgroundColor(swatch.getRgb());
                                    holder.mName.setTextColor(swatch.getBodyTextColor());
                                }
                            });
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                    }
                });

    }

    @Override
    public int getItemCount() {
        return actresses == null ? 0 : actresses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImage;

        public TextView mName;

        public CardView mCard;

        public ViewHolder(View view) {
            super(view);
            mImage = view.findViewById(R.id.actress_palette_img);
            mName = view.findViewById(R.id.actress_palette_name);
            mCard = view.findViewById(R.id.card_actress_palette);
        }
    }
}
