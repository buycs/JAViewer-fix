package io.github.javiewer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.wefika.flowlayout.FlowLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;
import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.network.BasicService;
import io.github.javiewer.adapter.ActressPaletteAdapter;
import io.github.javiewer.adapter.MovieHeaderAdapter;
import io.github.javiewer.adapter.RelatedMovieAdapter;
import io.github.javiewer.adapter.ScreenshotAdapter;
import io.github.javiewer.adapter.item.Genre;
import io.github.javiewer.view.decoration.GridSpacingItemDecoration;
import io.github.javiewer.adapter.item.Movie;
import io.github.javiewer.adapter.item.MovieDetail;
import io.github.javiewer.network.PSVS;
import io.github.javiewer.network.item.AvgleSearchResult;
import io.github.javiewer.network.provider.AVMOProvider;
import io.github.javiewer.util.BundleCompat;
import io.github.javiewer.util.SimpleVideoPlayer;
import io.github.javiewer.view.ViewUtil;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends SecureActivity {

    public Movie movie;
    public AvgleSearchResult.Response.Video video = null;

    CollapsingToolbarLayout mToolbarLayout;

    Toolbar mToolbar;

    ImageView mToolbarLayoutBackground;

    NestedScrollView mContent;

    ProgressBar mProgressBar;

    FloatingActionButton mFab;

    FlowLayout mFlowLayout;

    MenuItem mStarButton;

    private Call<ResponseBody> movieDetailCall;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        mToolbarLayout = findViewById(R.id.toolbar_layout);
        mToolbar = findViewById(R.id.toolbar);
        mToolbarLayoutBackground = findViewById(R.id.toolbar_layout_background);
        mContent = findViewById(R.id.movie_content);
        mProgressBar = findViewById(R.id.movie_progress_bar);
        mFab = findViewById(R.id.fab);
        mFlowLayout = findViewById(R.id.genre_flow_layout);

        Bundle bundle = this.getIntent().getExtras();
        movie = BundleCompat.getSerializable(bundle, "movie", Movie.class);
        if (movie == null) {
            Toast.makeText(this, "影片数据无效", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(movie.title);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovieActivity.this, DownloadActivity.class);
                Bundle arguments = new Bundle();
                arguments.putString("keyword", movie.getCode());
                intent.putExtras(arguments);
                startActivity(intent);
            }
        });
        mFab.bringToFront();

        BasicService service = JAViewer.getService();
        if (service == null) {
            Toast.makeText(this, "服务初始化失败，请检查数据源配置", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        loadMovieDetail();
    }

    private void loadMovieDetail() {
        if (movieDetailCall != null) {
            movieDetailCall.cancel();
        }
        mProgressBar.animate().setListener(null).cancel();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setAlpha(1f);
        mContent.setVisibility(View.INVISIBLE);

        movieDetailCall = JAViewer.getService().getMovie(Arrays.asList(this.movie.getLink(), "cn"));
        movieDetailCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    showDetailError();
                    return;
                }

                try {
                    MovieDetail detail = AVMOProvider.parseMoviesDetail(response.body().string());
                    detail.headers.add(0, MovieDetail.Header.create("影片番号", detail.code, "magnet"));
                    detail.headers.add(1, MovieDetail.Header.create("影片名称", movie.getTitle(), null));
                    displayInfo(detail);

                    Glide.with(mToolbarLayoutBackground.getContext().getApplicationContext())
                            .load(detail.coverUrl)
                            .into(mToolbarLayoutBackground);
                } catch (Exception e) {
                    e.printStackTrace();
                    showDetailError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }
                t.printStackTrace();
                showDetailError();
            }
        });
    }

    private void showDetailError() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        mProgressBar.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mProgressBar.setVisibility(View.GONE);
            }
        }).start();
        Snackbar.make(findViewById(android.R.id.content), "影片信息加载失败", Snackbar.LENGTH_INDEFINITE)
                .setAction("重试", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMovieDetail();
                    }
                })
                .show();
    }

    private AlertDialog showLoadingDialog(String message) {
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        progressBar.setPadding(padding, padding, padding, padding);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("请稍后")
                .setView(progressBar)
                .setMessage(message)
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }

    private void displayInfo(MovieDetail detail) {
        //Info
        {
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.headers_recycler_view);
            ImageView mIcon = (ImageView) findViewById(R.id.movie_icon_header);

            if (detail.headers.isEmpty()) {
                TextView mText = (TextView) findViewById(R.id.header_empty_text);
                mRecyclerView.setVisibility(View.GONE);
                mText.setVisibility(View.VISIBLE);
                ViewUtil.alignIconToView(mIcon, mText);
            } else {
                mRecyclerView.setAdapter(new MovieHeaderAdapter(detail.headers, this, mIcon));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setNestedScrollingEnabled(false);
            }
        }

        //Screenshots
        {
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.screenshots_recycler_view);
            ImageView mIcon = (ImageView) findViewById(R.id.movie_icon_screenshots);

            if (detail.screenshots.isEmpty()) {
                TextView mText = (TextView) findViewById(R.id.screenshots_empty_text);
                mRecyclerView.setVisibility(View.GONE);
                mText.setVisibility(View.VISIBLE);
                ViewUtil.alignIconToView(mIcon, mText);
            } else {
                mRecyclerView.setAdapter(new ScreenshotAdapter(detail.screenshots, this, mIcon, movie));
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
                mRecyclerView.setNestedScrollingEnabled(false);
            }
        }

        //Actress
        {
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.actresses_recycler_view);
            ImageView mIcon = (ImageView) findViewById(R.id.movie_icon_actresses);

            if (detail.actresses.isEmpty()) {
                TextView mText = (TextView) findViewById(R.id.actresses_empty_text);
                mRecyclerView.setVisibility(View.GONE);
                mText.setVisibility(View.VISIBLE);
                ViewUtil.alignIconToView(mIcon, mText);
            } else {
                mRecyclerView.setAdapter(new ActressPaletteAdapter(detail.actresses, this, mIcon));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                mRecyclerView.setNestedScrollingEnabled(false);
            }
        }

        //Genre
        {
            ImageView mIcon = (ImageView) findViewById(R.id.movie_icon_genre);

            if (detail.genres.isEmpty()) {
                mFlowLayout.setVisibility(View.GONE);
                TextView mText = (TextView) findViewById(R.id.genre_empty_text);
                mText.setVisibility(View.VISIBLE);
                ViewUtil.alignIconToView(mIcon, mText);
            } else {
                for (int i = 0; i < detail.genres.size(); i++) {
                    final Genre genre = detail.genres.get(i);
                    View view = getLayoutInflater().inflate(R.layout.chip_genre, mFlowLayout, false);
                    Chip chip = (Chip) view.findViewById(R.id.chip_genre);
                    chip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (genre.getLink() != null) {
                                startActivity(MovieListActivity.newIntent(MovieActivity.this, genre.getName(), genre.getLink(), "genre"));
                            }
                        }
                    });
                    chip.setText(genre.getName());
                    mFlowLayout.addView(view);

                    if (i == 0) {
                        ViewUtil.alignIconToView(mIcon, view);
                    }
                }
            }
        }

        //Related Movies
        {
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.related_recycler_view);
            ImageView mIcon = (ImageView) findViewById(R.id.movie_icon_related);

            Call<ResponseBody> relatedCall = JAViewer.getService().getRelatedMovies(Arrays.asList(movie.getLink(), "cn", 12));
            relatedCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        List<Movie> related = AVMOProvider.parseMovies(response.body().string());
                        if (related.isEmpty()) {
                            TextView mText = (TextView) findViewById(R.id.related_empty_text);
                            mRecyclerView.setVisibility(View.GONE);
                            mText.setVisibility(View.VISIBLE);
                            ViewUtil.alignIconToView(mIcon, mText);
                        } else {
                            mRecyclerView.setAdapter(new RelatedMovieAdapter(related, MovieActivity.this));
                            mRecyclerView.setLayoutManager(new GridLayoutManager(MovieActivity.this, 3));
                            mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, ViewUtil.dpToPx(6), false));
                            mRecyclerView.setNestedScrollingEnabled(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }

        //Changing visibility
        mProgressBar.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mProgressBar.setVisibility(View.GONE);
            }
        }).start();

        //Slide Up Animation
        mContent.setVisibility(View.VISIBLE);
        mContent.setY(mContent.getY() + 120);
        mContent.setAlpha(0);
        mContent.animate().translationY(0).alpha(1).setDuration(500).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie, menu);

        mStarButton = menu.findItem(R.id.action_star);
        {
            if (JAViewer.CONFIGURATIONS.getStarredMovies().contains(movie)) {
                mStarButton.setIcon(R.drawable.ic_menu_star);
                mStarButton.setTitle("取消收藏");
            }
        }
        mStarButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (JAViewer.CONFIGURATIONS.getStarredMovies().contains(movie)) {
                    JAViewer.CONFIGURATIONS.getStarredMovies().remove(movie);
                    mStarButton.setIcon(R.drawable.ic_menu_star_border);
                    Snackbar.make(mContent, "已取消收藏", Snackbar.LENGTH_LONG).show();
                    mStarButton.setTitle("收藏");
                } else {
                    List<Movie> movies = JAViewer.CONFIGURATIONS.getStarredMovies();
                    Collections.reverse(movies);
                    movies.add(movie);
                    Collections.reverse(movies);
                    mStarButton.setIcon(R.drawable.ic_menu_star);
                    Snackbar.make(mContent, "已收藏", Snackbar.LENGTH_LONG).show();
                    mStarButton.setTitle("取消收藏");
                }
                JAViewer.CONFIGURATIONS.save();
                FavouriteActivity.update();
                return true;
            }
        });

        final MenuItem mShareButton = menu.findItem(R.id.action_share);
        mShareButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {

                    File cache = new File(getExternalFilesDir("cache"), "screenshot");

                    //Generate screenshot
                    FileOutputStream os = new FileOutputStream(cache);
                    Bitmap screenshot = getScreenBitmap();
                    screenshot.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();

                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), "io.github.javiewer.fileprovider", cache);
                    // Uri uri = Uri.fromFile(cache);
                    Intent intent = new Intent(Intent.ACTION_SEND)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .setType("image/jpeg")
                            .putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "分享此影片"));

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MovieActivity.this, "无法分享：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public Bitmap getScreenBitmap() {
        int imageHeight = mToolbarLayoutBackground.getHeight();
        int scrollViewHeight = 0;
        for (int i = 0; i < mContent.getChildCount(); i++) {
            scrollViewHeight += mContent.getChildAt(i).getHeight();
        }
        Bitmap result = Bitmap.createBitmap(mContent.getWidth(), imageHeight + scrollViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.parseColor("#FAFAFA"));

        //Image
        {
            Bitmap bitmap = Bitmap.createBitmap(mToolbarLayoutBackground.getWidth(), imageHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            mToolbarLayoutBackground.draw(c);
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        //ScrollView
        {
            Bitmap bitmap = Bitmap.createBitmap(mContent.getWidth(), scrollViewHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            mContent.draw(c);
            canvas.drawBitmap(bitmap, 0, imageHeight, null);
        }

        return result;
    }

    public void onClickPreview() {
        //TODO: Deprecated
        if (video != null) {
            JZVideoPlayerStandard.startFullscreen(MovieActivity.this, SimpleVideoPlayer.class, video.preview_video_url, movie.title);
            return;
        }

        final AlertDialog dialog = showLoadingDialog("正在搜索该影片的预览视频");

        Call<AvgleSearchResult> call = PSVS.INSTANCE.search(movie.code);
        call.enqueue(new Callback<AvgleSearchResult>() {
            @Override
            public void onResponse(Call<AvgleSearchResult> call, Response<AvgleSearchResult> response) {
                if (response.isSuccessful()) {
                    AvgleSearchResult result = response.body();
                    if (result.success && result.response.videos.size() > 0) {
                        video = result.response.videos.get(0);
                        JZVideoPlayerStandard.startFullscreen(MovieActivity.this, SimpleVideoPlayer.class, video.preview_video_url, movie.title);
                        Toast.makeText(MovieActivity.this, "提示：预览视频可能需要科学上网", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }
                }

                Toast.makeText(MovieActivity.this, "该影片暂无预览", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<AvgleSearchResult> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MovieActivity.this, "获取预览失败，请重试，或使用科学上网", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    public void onPlay() {
        //TODO: Deprecated
        final String ts = String.valueOf(System.currentTimeMillis() / 1000);
        if (video != null) {
            JZVideoPlayerStandard.startFullscreen(
                    MovieActivity.this,
                    SimpleVideoPlayer.class,
                    String.format("http://api.rekonquer.com/psvs/mp4.php?vid=%s&ts=%s&sign=%s", video.vid, ts, JAViewer.b(video.vid, ts)),
                    movie.title
            );
            return;
        }

        final AlertDialog dialog = showLoadingDialog("正在搜索该影片的在线视频源");

        Call<AvgleSearchResult> call = PSVS.INSTANCE.search(movie.code);
        call.enqueue(new Callback<AvgleSearchResult>() {
            @Override
            public void onResponse(Call<AvgleSearchResult> call, Response<AvgleSearchResult> response) {
                if (response.isSuccessful()) {
                    AvgleSearchResult result = response.body();
                    if (result.success && result.response.videos.size() > 0) {
                        video = result.response.videos.get(0);
                        JZVideoPlayerStandard.startFullscreen(
                                MovieActivity.this,
                                SimpleVideoPlayer.class,
                                String.format("http://api.rekonquer.com/psvs/mp4.php?vid=%s&ts=%s&sign=%s", video.vid, ts, JAViewer.b(video.vid, ts)),
                                movie.title
                        );
                        dialog.dismiss();
                        return;
                    }
                }

                Toast.makeText(MovieActivity.this, "该影片暂无在线视频源", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<AvgleSearchResult> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MovieActivity.this, "获取在线视频源失败，请重试，或使用科学上网", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    /*@OnClick(R.id.view_play)
    public void onPlay() {
        //TODO: Deprecated
        final ProgressDialog dialog = ProgressDialog.show(this, "请稍后", "正在搜索该影片的在线视频源", true, false);

        if (video != null) {
            dialog.setMessage("正在获取播放地址");

            String ts = String.valueOf(System.currentTimeMillis() / 1000);
            Request request = new Request.Builder()
                    .url(String.format(
                            "https://avgle.com/mp4.php?vid=%s&ts=%s&hash=%s&m3u8"
                            , video.vid
                            , ts
                            , PSVS21.computeHash(new PSVS21.StubContext(MovieActivity.this.getApplicationContext()), video.vid, ts)))
                    .build();
            JAViewer.HTTP_CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    dialog.dismiss();
                    Toast.makeText(MovieActivity.this, "获取播放地址失败，请尝试科学上网", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    dialog.dismiss();
                    startFullscreen(response.request().url().toString(), movie.title);
                }
            });
            //startActivityForResult(WebViewActivity.newIntent(MovieActivity.this, video.embedded_url), 0x0000eeff);
            return;
        }

        Call<AvgleSearchResult> call = Avgle.INSTANCE.search(movie.code);
        call.enqueue(new Callback<AvgleSearchResult>() {
            @Override
            public void onResponse(Call<AvgleSearchResult> call, Response<AvgleSearchResult> response) {
                if (response.isSuccessful()) {
                    AvgleSearchResult result = response.body();
                    if (result.success && result.response.videos.size() > 0) {
                        video = result.response.videos.get(0);
                        //startActivityForResult(WebViewActivity.newIntent(MovieActivity.this, video.embedded_url), 0x0000eeff);
                        //dialog.dismiss();
                        dialog.setMessage("正在获取播放地址");

                        String ts = String.valueOf(System.currentTimeMillis() / 1000);
                        Request request = new Request.Builder()
                                .url(String.format(
                                        "https://avgle.com/mp4.php?vid=%s&ts=%s&hash=%s&m3u8"
                                        , video.vid
                                        , ts
                                        , PSVS21.computeHash(new PSVS21.StubContext(MovieActivity.this.getApplicationContext()), video.vid, ts)))
                                .build();
                        JAViewer.HTTP_CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(okhttp3.Call call, IOException e) {
                                dialog.dismiss();
                                Toast.makeText(MovieActivity.this, "获取播放地址失败，请尝试科学上网", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                dialog.dismiss();
                                startFullscreen(response.request().url().toString(), movie.title);
                            }
                        });
                        return;
                    }
                }

                Toast.makeText(MovieActivity.this, "该影片暂无在线视频源", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<AvgleSearchResult> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MovieActivity.this, "获取视频源失败，请尝试科学上网", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0000eeff && resultCode == RESULT_OK) {
            JZVideoPlayerStandard.startFullscreen(MovieActivity.this, SimpleVideoPlayer.class, data.getStringExtra("m3u8"), movie.title);
        }
    }*/

    void startFullscreen(final String url, final String title) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                JZVideoPlayerStandard.startFullscreen(MovieActivity.this, SimpleVideoPlayer.class, url, title);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (movieDetailCall != null) {
            movieDetailCall.cancel();
            movieDetailCall = null;
        }
        super.onDestroy();
        JZVideoPlayer.releaseAllVideos();
    }
}
