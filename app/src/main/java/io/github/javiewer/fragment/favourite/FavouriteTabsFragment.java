package io.github.javiewer.fragment.favourite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;

import io.github.javiewer.R;
import io.github.javiewer.adapter.ViewPagerAdapter;

public class FavouriteTabsFragment extends Fragment {

    private static FavouriteTabsFragment instance;
    private ViewPagerAdapter mAdapter;
    AHBottomNavigationViewPager mViewPager;
    AHBottomNavigation mBottomNav;
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

    public FavouriteTabsFragment() {
    }

    public static void update() {
        FavouriteTabsFragment host = instance;
        if (host == null || host.mAdapter == null) {
            return;
        }
        for (int i = 0; i < host.mAdapter.getCount(); i++) {
            Fragment page = host.mAdapter.getItem(i);
            if (page instanceof FavouriteFragment) {
                ((FavouriteFragment) page).update();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourite_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        instance = this;
        mViewPager = view.findViewById(R.id.favourite_view_pager);
        mBottomNav = view.findViewById(R.id.bottom_navigation);
        mColorPrimary = ContextCompat.getColor(requireActivity(), R.color.colorPrimary);

        if (mOnPageChangeListener != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        }

        mAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPagingEnabled(true);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        if (mAdapter.getCount() == 0) {
            Fragment movie = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.favourite_view_pager + ":0");
            Fragment actress = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.favourite_view_pager + ":1");
            if (!(movie instanceof FavouriteMovieFragment)) {
                movie = new FavouriteMovieFragment();
            }
            if (!(actress instanceof FavouriteActressFragment)) {
                actress = new FavouriteActressFragment();
            }
            mAdapter.addFragment(movie, "作品");
            mAdapter.addFragment(actress, "女优");
            mAdapter.notifyDataSetChanged();
        }

        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(requireActivity(), R.menu.nav_favourite);
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

    public void applyFilter(String query) {
        filterQuery = query == null ? "" : query;
        if (mAdapter == null) {
            return;
        }
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Fragment page = mAdapter.getItem(i);
            if (page instanceof FavouriteFragment) {
                ((FavouriteFragment) page).applyFilter(filterQuery, sortMode);
            }
        }
    }

    public void applySort(int sortMode) {
        this.sortMode = sortMode;
        applyFilter(filterQuery);
    }

    public int getSortMode() {
        return sortMode;
    }

    @Override
    public void onDestroyView() {
        if (mViewPager != null && mOnPageChangeListener != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        }
        mAdapter = null;
        if (instance == this) {
            instance = null;
        }
        mViewPager = null;
        mBottomNav = null;
        super.onDestroyView();
    }
}
