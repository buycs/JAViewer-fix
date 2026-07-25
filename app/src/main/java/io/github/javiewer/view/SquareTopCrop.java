package io.github.javiewer.view;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class SquareTopCrop extends BitmapTransformation {

    public SquareTopCrop() {
        super();
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getWidth());
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update("SquareTopCrop".getBytes(CHARSET));
    }
}
