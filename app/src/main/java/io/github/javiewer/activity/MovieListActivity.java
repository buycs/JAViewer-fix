package io.github.javiewer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Collections;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.Actress;
import io.github.javiewer.adapter.item.ActressDetail;
import io.github.javiewer.fragment.MovieListFragment;
import io.github.javiewer.network.BasicService;
import io.github.javiewer.network.provider.AVMOProvider;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieListActivity extends SecureActivity {

    /** 女优信息栏；仅 action == "star" 时显示。 */
    private View mActressHeader;

    public static Intent newIntent(Context context, String title, String link) {
        return newIntent(context, title, link, "search", null);
    }

    public static Intent newIntent(Context context, String title, String link, String action) {
        return newIntent(context, title, link, action, null);
    }

    public static Intent newIntent(Context context, String title, String link, String action, String original) {
        Intent intent = new Intent(context, MovieListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("link", link);
        bundle.putString("action", action);
        if (original != null) {
            bundle.putString("original", original);
        }
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Bundle bundle = this.getIntent().getExtras();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(bundle.getString("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        View appBar = toolbar.getParent() instanceof AppBarLayout ? (View) toolbar.getParent() : toolbar;
        appBar.setPadding(appBar.getPaddingLeft(), 0, appBar.getPaddingRight(), appBar.getPaddingBottom());

        mActressHeader = findViewById(R.id.actress_header);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            MovieListFragment fragment = new MovieListFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        }

        setupActressHeader(bundle);
    }

    /**
     * 「女优的作品」页在列表上方补一条女优信息栏。
     *
     * <p>先用列表页带过来的 {@link Actress} 立刻渲染——名称、头像、作品数都是现成的，不用等网络；
     * 再异步请求 {@code getStar} 补上列表接口没有的生日、星座、三围等字段。
     * 请求失败就维持已经渲染出来的内容，不影响看作品。
     */
    private void setupActressHeader(Bundle bundle) {
        if (mActressHeader == null) {
            return;
        }
        // 类型 / 片商 / 搜索等入口没有女优资料，信息栏保持隐藏。
        // 这里显式设置而不只依赖布局里的 visibility，避免 include 覆盖失效时串到别的页面。
        if (bundle == null || !"star".equals(bundle.getString("action"))) {
            mActressHeader.setVisibility(View.GONE);
            return;
        }

        mActressHeader.setVisibility(View.VISIBLE);

        ActressDetail detail = new ActressDetail();
        Object extra = bundle.getSerializable("actress");
        if (extra instanceof Actress) {
            Actress actress = (Actress) extra;
            detail.name = nullToEmpty(actress.getName());
            detail.avatarUrl = nullToEmpty(actress.getImageUrl());
            detail.movieCount = actress.getMovieCount();
        }
        if (detail.name.isEmpty()) {
            detail.name = bundle.getString("title", "");
        }
        bindActressHeader(detail);

        final String starId = bundle.getString("link");
        BasicService service = JAViewer.getService();
        if (starId == null || starId.isEmpty() || service == null) {
            return;
        }

        service.getStar(Collections.<Object>singletonList(starId)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed() || !response.isSuccessful() || response.body() == null) {
                    return;
                }
                try {
                    ActressDetail detail = AVMOProvider.parseStarDetail(response.body().string());
                    if (detail != null) {
                        bindActressHeader(detail);
                    }
                } catch (Exception e) {
                    android.util.Log.w("JAViewer", "getStar 解析失败: " + e.getMessage(), e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                android.util.Log.w("JAViewer", "getStar 请求失败: " + t.getMessage(), t);
            }
        });
    }

    private void bindActressHeader(ActressDetail detail) {
        if (mActressHeader == null) {
            return;
        }

        TextView name = mActressHeader.findViewById(R.id.actress_header_name);
        name.setText(detail.name);
        // marquee 需要 selected 才会滚动，长名字才不至于被截断
        name.setSelected(true);

        bindHeaderLine(mActressHeader.findViewById(R.id.actress_header_count), detail.buildCountLine());
        bindHeaderLine(mActressHeader.findViewById(R.id.actress_header_profile), detail.buildProfileLine());
        bindHeaderLine(mActressHeader.findViewById(R.id.actress_header_origin), detail.buildOriginLine());

        if (!detail.avatarUrl.isEmpty()) {
            ImageView avatar = mActressHeader.findViewById(R.id.actress_header_avatar);
            Glide.with(getApplicationContext())
                    .load(detail.avatarUrl)
                    .placeholder(R.drawable.ic_movie_actresses)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .dontAnimate()
                    .into(avatar);
        }
    }

    /** 内容为空时直接隐藏，免得留一条空行把信息栏撑高。 */
    private static void bindHeaderLine(TextView view, String text) {
        boolean empty = text == null || text.isEmpty();
        view.setText(empty ? "" : text);
        view.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
