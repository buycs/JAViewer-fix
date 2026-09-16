package io.github.javiewer.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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
import io.github.javiewer.network.provider.AVMOProvider;
import io.github.javiewer.util.BundleCompat;
import io.github.javiewer.view.ViewUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends SecureActivity {

    public Movie movie;

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

        View preview = findViewById(R.id.view_preview);
        if (preview != null) {
            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MovieActivity.this, "预览暂未接通", Toast.LENGTH_SHORT).show();
                }
            });
        }
        View play = findViewById(R.id.view_play);
        if (play != null) {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MovieActivity.this, "在线播放暂未接通", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
        int id = item.getItemId();
        if (id == android.R.id.home) {
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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        if (movieDetailCall != null) {
            movieDetailCall.cancel();
            movieDetailCall = null;
        }
        super.onDestroy();
    }
}
