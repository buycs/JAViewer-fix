package io.github.javiewer.fragment.genre;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import io.github.javiewer.JAViewer;
import io.github.javiewer.R;
import io.github.javiewer.activity.MovieListActivity;
import io.github.javiewer.adapter.item.Genre;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class GenreFragment extends Fragment {

    private FlowLayout mFlowLayout;
    protected List<Genre> genres = new ArrayList<>();

    public GenreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genre_list, container, false);
        mFlowLayout = view.findViewById(R.id.genre_flow_layout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderGenres();
    }

    /**
     * 每个标签宽度随文字，FlowLayout 自动换行，替代原来的固定两列网格。
     */
    private void renderGenres() {
        mFlowLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (final Genre genre : genres) {
            View chip = inflater.inflate(R.layout.card_genre, mFlowLayout, false);
            ((TextView) chip.findViewById(R.id.genre_name)).setText(genre.getName());
            chip.findViewById(R.id.card_genre).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (genre.getLink() == null || getActivity() == null) {
                        return;
                    }
                    Intent intent = new Intent(getActivity(), MovieListActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("title", genre.getName());
                    bundle.putString("link", genre.getLink());
                    bundle.putString("action", "genre");
                    intent.putExtras(bundle);
                    getActivity().startActivity(intent);
                }
            });
            mFlowLayout.addView(chip);
        }
    }

    public Call<ResponseBody> getCall(int page) {
        return JAViewer.SERVICE.getStars(Arrays.asList("stars", 60));
    }

    public List<Genre> getGenres() {
        return genres;
    }
}
