package io.github.javiewer.activity;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import io.github.javiewer.R;
import io.github.javiewer.adapter.ViewPagerAdapter;
import io.github.javiewer.fragment.BtSearchFragment;
import io.github.javiewer.fragment.DownloadFragment;
import io.github.javiewer.fragment.MagnetSearchFragment;

public class DownloadActivity extends SecureActivity {

    public Toolbar mToolbar;

    public TabLayout mTabLayout;

    public ViewPager mViewPager;

    public String keyword;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        mToolbar = findViewById(R.id.download_toolbar);
        mTabLayout = findViewById(R.id.download_tabs);
        mViewPager = findViewById(R.id.download_view_pager);

        Bundle bundle = this.getIntent().getExtras();
        this.keyword = this.getIntent().getExtras().getString("keyword");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Fragment fragment;

        fragment = new BtSearchFragment();
        Bundle btBundle = (Bundle) bundle.clone();
        btBundle.putString("keyword", this.keyword);
        fragment.setArguments(btBundle);
        adapter.addFragment(fragment, "BtSearch");

        fragment = new DownloadFragment();
        Bundle ciliBundle = (Bundle) bundle.clone();
        ciliBundle.putString("keyword", this.keyword);
        ciliBundle.putString("provider", "ciliinfo");
        fragment.setArguments(ciliBundle);
        adapter.addFragment(fragment, "无极磁链");

        fragment = new MagnetSearchFragment();
        Bundle magnetBundle = (Bundle) bundle.clone();
        magnetBundle.putString("keyword", this.keyword);
        fragment.setArguments(magnetBundle);
        adapter.addFragment(fragment, "btsow");

        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setVisibility(mTabLayout.getTabCount() <= 1 ? View.GONE : View.VISIBLE);
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
}
