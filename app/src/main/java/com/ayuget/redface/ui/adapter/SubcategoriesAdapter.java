package com.ayuget.redface.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.SelectableItem;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.ui.misc.BindableAdapter;

public class SubcategoriesAdapter extends BindableAdapter<SelectableItem> {
    private static final String LOG_TAG = SubcategoriesAdapter.class.getSimpleName();

    public final static int TYPE_CATEGORY = 0;
    public final static int TYPE_SUBCATEGORY = 1;

    private Category category;

    private String activeTopicFilter;

    static class ViewHolder {
        public TextView itemName;
        public TextView activeTopicFilter;
    }

    static class DropdownViewHolder {
        public TextView categoryName;
        public TextView subcategoryName;
        public View dividerView;
    }

    public SubcategoriesAdapter(Context context, final TopicFilter defaultFilter) {
        super(context);

        setActiveTopicFilter(defaultFilter);
    }

    public void replaceWith(Category category) {
        this.category = category;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return 2 + category.getSubcategories().size();
    }

    @Override
    public SelectableItem getItem(int position) {
        if (position == 0) {
            return category;
        }
        else if (position == 1) {
            return null; // Divider
        }
        else {
            return category.getSubcategories().get(position - 2);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        int viewLayout = position == 0 ? R.layout.actionbar_spinner_category_actionbar : R.layout.actionbar_spinner_subcategory_actionbar;
        int itemTextView = position == 0 ? R.id.category_name : R.id.subcategory_name;

        View activeSubcategoryView = inflater.inflate(viewLayout, container, false);
        ViewHolder holder = new ViewHolder();
        holder.itemName = (TextView) activeSubcategoryView.findViewById(itemTextView);
        holder.activeTopicFilter = (TextView) activeSubcategoryView.findViewById(R.id.selected_topics);
        activeSubcategoryView.setTag(holder);
        return activeSubcategoryView;
    }

    @Override
    public void bindView(SelectableItem item, int position, View view) {
        if (position != 1) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.itemName.setText(item.getName());
            holder.activeTopicFilter.setText(activeTopicFilter);
        }
    }

    public boolean isCategory(int position) {
        return position == 0;
    }

    public boolean isSubcategory(int position) {
        return position >= 2;
    }

    @Override
    public View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
        View dropdownView = inflater.inflate(R.layout.actionbar_spinner_dropdown, container, false);
        DropdownViewHolder holder = new DropdownViewHolder();

        // Android bug 17128 : https://code.google.com/p/android/issues/detail?id=17128
        // Spinner does not support multiple view types, hence the dropdown view having the 3 different
        // view types.
        holder.categoryName = (TextView) dropdownView.findViewById(R.id.category_name);
        holder.subcategoryName = (TextView) dropdownView.findViewById(R.id.subcategory_name);
        holder.dividerView = dropdownView.findViewById(R.id.spinner_divider);

        dropdownView.setTag(holder);

        return dropdownView;
    }

    @Override
    public void bindDropDownView(SelectableItem item, int position, View view) {
        DropdownViewHolder holder = (DropdownViewHolder) view.getTag();

        if (isCategory(position)) {
            holder.categoryName.setText(item.getName());

            holder.categoryName.setVisibility(View.VISIBLE);
            holder.subcategoryName.setVisibility(View.GONE);
            holder.dividerView.setVisibility(View.GONE);

        }
        else if (isSubcategory(position)) {
            holder.subcategoryName.setText(item.getName());

            holder.categoryName.setVisibility(View.GONE);
            holder.subcategoryName.setVisibility(View.VISIBLE);
            holder.dividerView.setVisibility(View.GONE);
        }
        else {
            // Divider
            holder.categoryName.setVisibility(View.GONE);
            holder.subcategoryName.setVisibility(View.GONE);
            holder.dividerView.setVisibility(View.VISIBLE);
        }
    }

    public void setActiveTopicFilter(TopicFilter topicFilter) {
        this.activeTopicFilter = topicFilter.resolve(getContext()).toUpperCase();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_CATEGORY : TYPE_SUBCATEGORY;
    }

    @Override
    public boolean isEnabled(int position) {
        // Divider is at position 1
        return position != 1;
    }
}
