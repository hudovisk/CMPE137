package com.assignment.sjsu.hudoassenco.cmpe137;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class NewsFeedFragment extends Fragment {

    private RecyclerView _newsFeedView;
    private RecyclerView.LayoutManager _layoutManager;
    private RecyclerView.Adapter _adapter;

    public NewsFeedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_feed, container, false);

        _newsFeedView = (RecyclerView) rootView.findViewById(R.id.newsFeedView);
        _layoutManager = new LinearLayoutManager(getContext());
        _adapter = new NewsFeedAdapter();

        _newsFeedView.setHasFixedSize(true);
        _newsFeedView.setLayoutManager(_layoutManager);
        _newsFeedView.setAdapter(_adapter);

        return rootView;
    }
}
