package io.github.javiewer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import io.github.javiewer.R;
import io.github.javiewer.fragment.MovieListFragment;
import io.github.javiewer.view.ViewUtil;

public class MovieListActivity extends SecureActivity {

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

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(bundle.getString("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(ViewUtil.dpToPx(4));

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            MovieListFragment fragment = new MovieListFragment();
            fragment.setArguments(bundle);
            transaction.replace(R.id.content_query, fragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
