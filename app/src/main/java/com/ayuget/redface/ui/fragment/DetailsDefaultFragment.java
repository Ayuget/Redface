package com.ayuget.redface.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.misc.UiUtils;

import butterknife.InjectView;

public class DetailsDefaultFragment extends BaseFragment{
    @InjectView(R.id.empty_content_image)
    ImageView emptyContentImage;

    public static DetailsDefaultFragment newInstance() {
        DetailsDefaultFragment fragment = new DetailsDefaultFragment();
        return fragment;
    }

    public DetailsDefaultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflateRootView(R.layout.fragment_details_default, inflater, container);

        UiUtils.setDrawableColor(emptyContentImage.getDrawable(), getResources().getColor(R.color.empty_view_image_color));

        return rootView;
    }
}
