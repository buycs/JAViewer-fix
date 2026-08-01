package io.github.javiewer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.fragment.ActressesFragment;
import io.github.javiewer.fragment.ExtendedAppBarFragment;
import io.github.javiewer.fragment.HomeFragment;
import io.github.javiewer.fragment.PopularFragment;
import io.github.javiewer.fragment.ReleasedFragment;
import io.github.javiewer.fragment.genre.GenreTabsFragment;
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_home);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mSearchView = findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    startActivity(MovieListActivity.newIntent(MainActivity.this, query, query));
                }
                mSearchView.closeSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        initFragments();
        buildDrawerHeader();

        if (savedInstanceState == null) {
            mNavigationView.setCheckedItem(R.id.nav_home);
            setFragment(R.id.nav_home);
        }
    }

    public void buildDrawerHeader() {
        View header = mNavigationView.getHeaderView(0);

        header.post(() -> {
            int statusBarHeight = ViewUtil.getStatusBarHeight(MainActivity.this);
            header.setPadding(header.getPaddingLeft(), statusBarHeight, header.getPaddingRight(), header.getPaddingBottom());
        });

        TextView mTextSource = header.findViewById(R.id.text_view_source);
        mTextSource.setText("数据源");

        ImageButton mBtnEdit = header.findViewById(R.id.btn_edit_sources);
        mBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDomainEditor();
            }
        });

        RadioGroup radioGroup = header.findViewById(R.id.radio_group_source);
        for (int i = 0; i < JAViewer.DATA_SOURCES.size(); i++) {
            DataSource ds = JAViewer.DATA_SOURCES.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(i);
            rb.setText(ds.name);
            rb.setPadding(0, 8, 24, 8);
            radioGroup.addView(rb);
        }

        DataSource current = JAViewer.getDataSource();
        int currentIndex = JAViewer.DATA_SOURCES.indexOf(current);
        if (currentIndex >= 0) {
            radioGroup.check(currentIndex);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId < 0) return;
                DataSource newSource = JAViewer.DATA_SOURCES.get(checkedId);
                if (newSource.equals(JAViewer.getDataSource())) {
                    return;
                }
                JAViewer.CONFIGURATIONS.setDataSource(newSource);
                JAViewer.CONFIGURATIONS.save();
                JAViewer.recreateService();
                restart();
            }
        });
    }

    public void showDomainEditor() {
        final DataSource current = JAViewer.getDataSource();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("编辑数据源域名");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(current.domain);
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newDomain = input.getText().toString().trim();
                if (!newDomain.isEmpty()) {
                    current.domain = newDomain;
                    JAViewer.CONFIGURATIONS.save();
                    JAViewer.recreateService();
                    restart();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public void initFragments() {
        this.fragmentManager = getSupportFragmentManager();

        if (this.savedInstanceState != null) {
            String tag = this.savedInstanceState.getString("CurrentFragment");
            this.currentFragment = fragmentManager.findFragmentByTag(tag);
            return;
        }

        FragmentTransaction transaction = this.fragmentManager.beginTransaction();
        for (Class<? extends Fragment> fragmentClass : JAViewer.FRAGMENTS.values()) {
            try {
                Fragment fragment = fragmentClass.getConstructor(new Class[0]).newInstance();
                transaction.add(R.id.content, fragment, fragmentClass.getSimpleName()).hide(fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        transaction.commit();
        this.fragmentManager.executePendingTransactions();
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
    }

    private void setFragment(int id) {
        Class<? extends Fragment> fragmentClass = JAViewer.FRAGMENTS.get(id);
        if (fragmentClass == null) return;
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentClass.getSimpleName());
        CharSequence title = ((MenuItem) mNavigationView.getMenu().findItem(id)).getTitle();
        this.setFragment(fragment, title);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_favourite) {
            startActivity(new Intent(this, FavouriteActivity.class));
        } else if (id == R.id.nav_source) {
            openSourceRepository("https://github.com/SplashCodes/JAViewer");
        } else if (id == R.id.nav_source_fork) {
            openSourceRepository("https://github.com/buycs/JAViewer-fix");
        } else {
            item.setChecked(true);
            setFragment(id);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openSourceRepository(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
