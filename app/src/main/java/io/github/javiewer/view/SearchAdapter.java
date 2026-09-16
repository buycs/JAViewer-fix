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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.javiewer.R;

public class SearchAdapter extends ArrayAdapter<String> {

    public static final String CLEAR_HISTORY_ACTION = "清空搜索历史";

    private String[] suggestions;
    private Drawable suggestionIcon;
    private boolean ellipsize;
    private LayoutInflater inflater;

    public SearchAdapter(Context context, String[] suggestions, Drawable suggestionIcon, boolean ellipsize) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<String>());
        this.suggestions = suggestions != null ? suggestions : new String[0];
        this.suggestionIcon = suggestionIcon;
        this.ellipsize = ellipsize;
        this.inflater = LayoutInflater.from(context);
        addAll(this.suggestions);
    }

    public static boolean isClearHistoryAction(String value) {
        return CLEAR_HISTORY_ACTION.equals(value);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.search_suggestion_item, parent, false);
        }

        TextView text = view.findViewById(R.id.suggestion_text);
        ImageView icon = view.findViewById(R.id.suggestion_icon);

        String suggestion = getItem(position);
        text.setText(suggestion != null ? suggestion : "");
        if (ellipsize) {
            text.setSingleLine(true);
            text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }

        if (isClearHistoryAction(suggestion)) {
            icon.setVisibility(View.GONE);
            text.setTextColor(0xFFD32F2F);
        } else {
            text.setTextColor(0xDE000000);
            if (suggestionIcon != null) {
                icon.setImageDrawable(suggestionIcon);
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> values = new ArrayList<>();
                String needle = constraint == null ? "" : constraint.toString().trim().toLowerCase(Locale.ROOT);
                if (suggestions != null) {
                    for (String suggestion : suggestions) {
                        if (isClearHistoryAction(suggestion)) {
                            continue;
                        }
                        if (needle.isEmpty() || suggestion.toLowerCase(Locale.ROOT).contains(needle)) {
                            values.add(suggestion);
                        }
                    }
                    if (!values.isEmpty()) {
                        values.add(CLEAR_HISTORY_ACTION);
                    }
                }
                results.values = values;
                results.count = values.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results != null && results.values instanceof List) {
                    addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }
}
