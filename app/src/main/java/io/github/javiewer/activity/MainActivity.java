package io.github.javiewer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.fragment.ActressesFragment;
import io.github.javiewer.fragment.ExtendedAppBarFragment;
import io.github.javiewer.fragment.HomeFragment;
import io.github.javiewer.fragment.PopularFragment;
import io.github.javiewer.fragment.ReleasedFragment;
import io.github.javiewer.fragment.favourite.FavouriteFragment;
import io.github.javiewer.fragment.favourite.FavouriteTabsFragment;
import io.github.javiewer.fragment.genre.GenreTabsFragment;
import io.github.javiewer.util.QueryNormalizer;
import io.github.javiewer.view.SimpleSearchView;
import io.github.javiewer.view.ViewUtil;

public class MainActivity extends SecureActivity implements NavigationView.OnNavigationItemSelectedListener {

    public Fragment currentFragment;
    private SimpleSearchView mSearchView;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FragmentManager fragmentManager;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (JAViewer.CONFIGURATIONS == null) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
            return;
        }

        JAViewer.recreateService();

        this.savedInstanceState = savedInstanceState;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_drawer);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mSearchView = findViewById(R.id.search_view);
        mSearchView.setSubmitOnClick(true);
        mSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    String original = query.trim();
                    if (JAViewer.CONFIGURATIONS != null) {
                        JAViewer.CONFIGURATIONS.addSearchHistory(original);
                        JAViewer.CONFIGURATIONS.save();
                    }
                    String keyword = QueryNormalizer.normalize(original);
                    startActivity(MovieListActivity.newIntent(MainActivity.this, original, keyword, "search", original));
                    applySearchSuggestions();
                }
                mSearchView.closeSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearchView.setOnSearchViewListener(new SimpleSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                applySearchSuggestions();
            }

            @Override
            public void onSearchViewClosed() {
            }
        });
        mSearchView.setOnClearHistoryListener(new SimpleSearchView.OnClearHistoryListener() {
            @Override
            public void onClearHistory() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("清空搜索历史")
                        .setMessage("确定清空全部搜索历史？")
                        .setPositiveButton("清空", (dialog, which) -> {
                            if (JAViewer.CONFIGURATIONS != null) {
                                JAViewer.CONFIGURATIONS.clearSearchHistory();
                                JAViewer.CONFIGURATIONS.save();
                            }
                            applySearchSuggestions();
                            Toast.makeText(MainActivity.this, "搜索历史已清空", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        initFragments();
        buildDrawerHeader();

        if (savedInstanceState == null) {
            mNavigationView.setCheckedItem(R.id.nav_home);
            setFragment(R.id.nav_home);
        } else if (currentFragment != null) {
            int restoredId = menuIdForFragment(currentFragment);
            if (restoredId != 0) {
                mNavigationView.setCheckedItem(restoredId);
                getSupportActionBar().setTitle(mNavigationView.getMenu().findItem(restoredId).getTitle());
            }
            if (currentFragment instanceof ExtendedAppBarFragment) {
                findViewById(R.id.app_bar).setElevation(0);
            } else {
                findViewById(R.id.app_bar).setElevation(4 * getResources().getDisplayMetrics().density);
            }
        }
    }

    private int menuIdForFragment(Fragment fragment) {
        for (java.util.Map.Entry<Integer, Class<? extends Fragment>> entry : JAViewer.FRAGMENTS.entrySet()) {
            if (entry.getValue() == fragment.getClass()) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public void buildDrawerHeader() {
        View header = mNavigationView.getHeaderView(0);

        header.post(() -> {
            int statusBarHeight = ViewUtil.getStatusBarHeight(MainActivity.this);
            header.setPadding(header.getPaddingLeft(), statusBarHeight, header.getPaddingRight(), header.getPaddingBottom());
        });

    }

    public void initFragments() {
        this.fragmentManager = getSupportFragmentManager();

        if (this.savedInstanceState != null) {
            String tag = this.savedInstanceState.getString("CurrentFragment");
            this.currentFragment = fragmentManager.findFragmentByTag(tag);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setFragment(Fragment fragment, CharSequence title) {
        getSupportActionBar().setTitle(title);

        Fragment old = this.currentFragment;

        if (old == fragment) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (old != null) {
            transaction.hide(old);
        }
        transaction.show(fragment);
        transaction.commit();

        this.currentFragment = fragment;

        if (fragment instanceof ExtendedAppBarFragment) {
            findViewById(R.id.app_bar).setElevation(0);
        } else {
            findViewById(R.id.app_bar).setElevation(4 * getResources().getDisplayMetrics().density);
        }
        invalidateOptionsMenu();
    }

    private void setFragment(int id) {
        Class<? extends Fragment> fragmentClass = JAViewer.FRAGMENTS.get(id);
        if (fragmentClass == null) {
            return;
        }

        String tag = fragmentClass.getSimpleName();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        CharSequence title = mNavigationView.getMenu().findItem(id).getTitle();

        if (fragment == null) {
            try {
                fragment = fragmentClass.getConstructor(new Class[0]).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            getSupportActionBar().setTitle(title);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (this.currentFragment != null) {
                transaction.hide(this.currentFragment);
            }
            transaction.add(R.id.content, fragment, tag);
            transaction.commit();

            this.currentFragment = fragment;

            if (fragment instanceof ExtendedAppBarFragment) {
                findViewById(R.id.app_bar).setElevation(0);
            } else {
                findViewById(R.id.app_bar).setElevation(4 * getResources().getDisplayMetrics().density);
            }
            invalidateOptionsMenu();
        } else {
            this.setFragment(fragment, title);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (this.currentFragment != null) {
            outState.putString("CurrentFragment", this.currentFragment.getClass().getSimpleName());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        moveTaskToBack(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSearchView.setMenuItem(menu.findItem(R.id.action_search));
        applySearchSuggestions();
        if (currentFragment instanceof FavouriteTabsFragment) {
            getMenuInflater().inflate(R.menu.favourite, menu);
            MenuItem searchItem = menu.findItem(R.id.action_fav_search);
            if (searchItem != null && searchItem.getActionView() instanceof SearchView) {
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint("搜索收藏");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (currentFragment instanceof FavouriteTabsFragment) {
                            ((FavouriteTabsFragment) currentFragment).applyFilter(query);
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (currentFragment instanceof FavouriteTabsFragment) {
                            ((FavouriteTabsFragment) currentFragment).applyFilter(newText);
                        }
                        return true;
                    }
                });
            }
            int sortMode = ((FavouriteTabsFragment) currentFragment).getSortMode();
            MenuItem recent = menu.findItem(R.id.action_fav_sort_recent);
            MenuItem name = menu.findItem(R.id.action_fav_sort_name);
            if (recent != null) {
                recent.setChecked(sortMode == FavouriteFragment.SORT_RECENT);
            }
            if (name != null) {
                name.setChecked(sortMode == FavouriteFragment.SORT_NAME);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean hideSearch = currentFragment instanceof FavouriteTabsFragment
                || currentFragment instanceof SettingsActivity.SettingsFragment;
        MenuItem search = menu.findItem(R.id.action_search);
        if (search != null) {
            search.setVisible(!hideSearch);
        }
        if (hideSearch && mSearchView != null && mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void applySearchSuggestions() {
        if (mSearchView == null) {
            return;
        }
        ArrayList<String> history = JAViewer.CONFIGURATIONS != null
                ? JAViewer.CONFIGURATIONS.getSearchHistory()
                : new ArrayList<String>();
        ArrayList<String> suggestions = new ArrayList<String>(history);
        if (!suggestions.isEmpty()) {
            suggestions.add(io.github.javiewer.view.SearchAdapter.CLEAR_HISTORY_ACTION);
        }
        mSearchView.setSuggestions(suggestions.toArray(new String[0]));
        mSearchView.setSubmitOnClick(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        if (item.getItemId() == R.id.action_fav_sort_recent) {
            if (currentFragment instanceof FavouriteTabsFragment) {
                item.setChecked(true);
                ((FavouriteTabsFragment) currentFragment).applySort(FavouriteFragment.SORT_RECENT);
                return true;
            }
        }
        if (item.getItemId() == R.id.action_fav_sort_name) {
            if (currentFragment instanceof FavouriteTabsFragment) {
                item.setChecked(true);
                ((FavouriteTabsFragment) currentFragment).applySort(FavouriteFragment.SORT_NAME);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(true);
        setFragment(id);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
