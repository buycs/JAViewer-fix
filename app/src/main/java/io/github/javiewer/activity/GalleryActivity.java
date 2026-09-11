package io.github.javiewer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.util.BundleCompat;

public class GalleryActivity extends SecureActivity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    public ViewPager mPager;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    public Toolbar mToolbar;
    Animation fadeIn = new AlphaAnimation(0, 1);
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mToolbar.startAnimation(fadeIn);

            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    Animation fadeOut = new AlphaAnimation(1, 0);
    GestureDetector detector;
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private String[] imageUrls;
    private Movie movie;

    {
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(150);
    }

    {
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(150);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);

        mPager = findViewById(R.id.gallery_pager);
        mToolbar = findViewById(R.id.toolbar_gallery);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                toggle();
                return true;
            }
        });
        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });

        {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            mToolbar.startAnimation(fadeOut);
            //mControlsView.setVisibility(View.GONE);
            mVisible = false;
            mHidePart2Runnable.run();
        }

        Bundle bundle = this.getIntent().getExtras();
        movie = BundleCompat.getSerializable(bundle, "movie", Movie.class);
        imageUrls = bundle != null ? bundle.getStringArray("urls") : null;
        if (movie == null || imageUrls == null || imageUrls.length == 0) {
            Toast.makeText(this, "影片数据无效", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mPager.setAdapter(new ImageAdapter(this, imageUrls, this));
        mPager.setCurrentItem(bundle.getInt("position"));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updateIndicator();
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        updateIndicator();
    }

    private void updateIndicator() {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
        getSupportActionBar().setTitle((mPager.getCurrentItem() + 1) + " / " + (imageUrls.length));
        //mTextIndicator.setText((mPager.getCurrentItem() + 1) + " / " + (imageUrls.length));
    }

    public void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
        }
    }

    private void hide() {
        hide(UI_ANIMATION_DELAY);
    }

    private void hide(int delay) {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mToolbar.startAnimation(fadeOut);
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, delay);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            final File dir = new File(
                    JAViewer.getStorageDir(),
                    String.format("/movies/[%s] %s", movie.code, movie.title).replaceAll("^(?!(COM[0-9]|LPT[0-9]|CON|PRN|AUX|CLOCK\\$|NUL)$)[^./\\\\:*?\u200C\u200B\"<>|]+$", "-")
            );
            dir.mkdirs();
            final int index = mPager.getCurrentItem();
            Glide
                    .with(this)
                    .asBitmap()
                    .load(imageUrls[index])
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            try {
                                OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(dir, (index + 1) + ".jpeg")));
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, os);
                                os.flush();
                                os.close();
                                Toast.makeText(GalleryActivity.this, "成功保存到 " + dir, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }
                    });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class ImageAdapter extends PagerAdapter {

        private final String[] imageUrls;
        private GalleryActivity mActivity;
        private LayoutInflater inflater;
        //private DisplayImageOptions options;

        ImageAdapter(Context context, String[] imageUrls, GalleryActivity mActivity) {
            inflater = LayoutInflater.from(context);
            this.mActivity = mActivity;
            this.imageUrls = imageUrls;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.content_gallery, view, false);
            final ImageView imageView = imageLayout.findViewById(R.id.image);
            final ProgressBar progressBar = imageLayout.findViewById(R.id.progress_bar);
            final TextView textView = imageLayout.findViewById(R.id.gallery_text_error);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.toggle();
                }
            });

            Glide.with(imageView.getContext().getApplicationContext())
                    .load(imageUrls[position])
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            progressBar.setVisibility(View.GONE);
                            imageView.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(Drawable errorDrawable) {
                            progressBar.setVisibility(View.GONE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("图片加载失败 :(\n");
                        }
                    });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
