package com.ayuget.redface.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ayuget.redface.R;

public class DefaultFragment extends BaseFragment {

    public static DefaultFragment newInstance() {
        DefaultFragment fragment = new DefaultFragment();
        return fragment;
    }

    public DefaultFragment() {
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
        return inflater.inflate(R.layout.fragment_default, container, false);
    }
}
