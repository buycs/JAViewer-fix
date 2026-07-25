package io.github.javiewer.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;


import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.ViewPagerAdapter;
import io.github.javiewer.fragment.BtSearchFragment;
import io.github.javiewer.fragment.DownloadFragment;

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
        getSupportActionBar().setTitle(this.keyword);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Fragment fragment;

        fragment = new DownloadFragment();
        bundle = (Bundle) bundle.clone();
        bundle.putString("provider", "btso");
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "BTSO");

        fragment = new DownloadFragment();
        bundle = (Bundle) bundle.clone();
        bundle.putString("provider", "torrentkitty");
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "Torrent Kitty");

        fragment = new BtSearchFragment();
        bundle = (Bundle) bundle.clone();
        bundle.putString("keyword", this.keyword);
        fragment.setArguments(bundle);
        adapter.addFragment(fragment, "BtSearch");

        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);

        long downloadCounter = JAViewer.CONFIGURATIONS.getDownloadCounter();
        if (downloadCounter == -1) {
            return;
        }
        downloadCounter++;
        JAViewer.CONFIGURATIONS.setDownloadCounter(downloadCounter);
        if (downloadCounter % 20 == 0) {
            new AlertDialog.Builder(this)
                    .setNeutralButton("不再显示", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JAViewer.CONFIGURATIONS.setDownloadCounter(-1);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
        JAViewer.CONFIGURATIONS.save();
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
