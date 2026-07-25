package io.github.javiewer.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.javiewer.R;

public class SearchAdapter extends ArrayAdapter<String> {

    private String[] suggestions;
    private Drawable suggestionIcon;
    private boolean ellipsize;
    private LayoutInflater inflater;

    public SearchAdapter(Context context, String[] suggestions, Drawable suggestionIcon, boolean ellipsize) {
        super(context, android.R.layout.simple_list_item_1, suggestions);
        this.suggestions = suggestions;
        this.suggestionIcon = suggestionIcon;
        this.ellipsize = ellipsize;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.search_suggestion_item, parent, false);
        }

        TextView text = view.findViewById(R.id.suggestion_text);
        ImageView icon = view.findViewById(R.id.suggestion_icon);

        text.setText(suggestions[position]);
        if (ellipsize) {
            text.setSingleLine(true);
            text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }

        if (suggestionIcon != null) {
            icon.setImageDrawable(suggestionIcon);
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }

        return view;
    }
}
