package io.github.javiewer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.AppBarLayout;
import com.wefika.flowlayout.FlowLayout;

import java.util.Collections;
import java.util.List;

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

        bindChip(mActressHeader.findViewById(R.id.actress_header_count), detail.buildCountChip());
        bindChip(mActressHeader.findViewById(R.id.actress_header_release), detail.buildReleaseChip());
        bindMetaChips(detail.buildChips());

        if (!detail.avatarUrl.isEmpty()) {
            ImageView avatar = mActressHeader.findViewById(R.id.actress_header_avatar);
            Glide.with(getApplicationContext())
                    .load(detail.avatarUrl)
                    .placeholder(R.drawable.ic_movie_actresses)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .dontAnimate()
                    .into(avatar);
        }

        // 异步补到的资料会改变信息栏高度，AppBarLayout 可能停在半收起、
        // 把 CTL 的 contentScrim 刷到信息栏上；布局完成后展开一次复位。
        final AppBarLayout appBar = findViewById(R.id.movie_list_app_bar);
        if (appBar != null) {
            mActressHeader.post(new Runnable() {
                @Override
                public void run() {
                    appBar.setExpanded(true, false);
                }
            });
        }
    }

    /**
     * 逐枚渲染资料胶囊。接口没给的字段不会出现在 chips 里，所以这里不用再做判空；
     * 一个字段都没有时整块隐藏，免得留一片空白。
     */
    private void bindMetaChips(List<ActressDetail.Chip> chips) {
        FlowLayout container = mActressHeader.findViewById(R.id.actress_header_chips);
        container.removeAllViews();
        container.setVisibility(chips.isEmpty() ? View.GONE : View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ActressDetail.Chip chip : chips) {
            View view = inflater.inflate(R.layout.chip_actress_meta, container, false);
            bindChip(view, chip);
            container.addView(view);
        }
    }

    /**
     * 把「标签 + 值」写进一枚胶囊，标签与值只是颜色不同、字号相同（见 chip_actress_meta.xml）。
     * 值为空表示这一项没数据，整枚隐藏，免得留一枚只有标签的空胶囊。
     */
    private static void bindChip(View chip, ActressDetail.Chip content) {
        boolean empty = content.isEmpty();
        chip.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        ((TextView) chip.findViewById(R.id.chip_actress_meta_label)).setText(content.label);
        ((TextView) chip.findViewById(R.id.chip_actress_meta_value)).setText(content.value);
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
