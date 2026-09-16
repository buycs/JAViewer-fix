package io.github.javiewer.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;


import io.github.javiewer.R;
import io.github.javiewer.adapter.ViewPagerAdapter;
import io.github.javiewer.fragment.favourite.FavouriteActressFragment;
import io.github.javiewer.fragment.favourite.FavouriteFragment;
import io.github.javiewer.fragment.favourite.FavouriteMovieFragment;
import io.github.javiewer.fragment.favourite.FavouriteTabsFragment;

public class FavouriteActivity extends SecureActivity {

    public static ViewPagerAdapter mAdapter;
    AHBottomNavigationViewPager mViewPager;
    AppBarLayout mAppBarLayout;
    AHBottomNavigation mBottomNav;
    Toolbar mToolbar;
    int mColorPrimary;
    private String filterQuery = "";
    private int sortMode = FavouriteFragment.SORT_RECENT;
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mBottomNav.setCurrentItem(position);
            mBottomNav.restoreBottomNavigation();
        }
    };

    public static void update() {
        FavouriteTabsFragment.update();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        mViewPager = findViewById(R.id.favourite_view_pager);
        mAppBarLayout = findViewById(R.id.app_bar_fav);
        mBottomNav = findViewById(R.id.bottom_navigation);
        mToolbar = findViewById(R.id.toolbar_fav);
        mColorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPagingEnabled(true);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        FavouriteFragment fragment = new FavouriteMovieFragment();
        mAdapter.addFragment(fragment, "作品");
        fragment = new FavouriteActressFragment();
        mAdapter.addFragment(fragment, "女优");
        mAdapter.notifyDataSetChanged();

        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.nav_favourite);
        navigationAdapter.setupWithBottomNavigation(mBottomNav);
        mBottomNav.setTranslucentNavigationEnabled(true);
        mBottomNav.setAccentColor(mColorPrimary);
        mBottomNav.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        mBottomNav.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (!wasSelected) {
                    mViewPager.setCurrentItem(position);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favourite, menu);
        MenuItem searchItem = menu.findItem(R.id.action_fav_search);
        if (searchItem != null && searchItem.getActionView() instanceof SearchView) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("搜索收藏");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    applyFilter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    applyFilter(newText);
                    return true;
                }
            });
        }
        MenuItem recent = menu.findItem(R.id.action_fav_sort_recent);
        MenuItem name = menu.findItem(R.id.action_fav_sort_name);
        if (recent != null) {
            recent.setChecked(sortMode == FavouriteFragment.SORT_RECENT);
        }
        if (name != null) {
            name.setChecked(sortMode == FavouriteFragment.SORT_NAME);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fav_sort_recent) {
            item.setChecked(true);
            sortMode = FavouriteFragment.SORT_RECENT;
            applyFilter(filterQuery);
            return true;
        }
        if (id == R.id.action_fav_sort_name) {
            item.setChecked(true);
            sortMode = FavouriteFragment.SORT_NAME;
            applyFilter(filterQuery);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyFilter(String query) {
        filterQuery = query == null ? "" : query;
        if (mAdapter == null) {
            return;
        }
        for (int i = 0; i < mAdapter.getCount(); i++) {
            ((FavouriteFragment) mAdapter.getItem(i)).applyFilter(filterQuery, sortMode);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }
}
