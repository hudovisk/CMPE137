package com.assignment.sjsu.hudoassenco.cmpe137;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharedAlbunsFragment extends Fragment {

    private RecyclerView _albunsView;
    private RecyclerView.LayoutManager _layoutManager;
    private RecyclerView.Adapter _adapter;

    public SharedAlbunsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shared_albuns, container, false);

        _albunsView = (RecyclerView) rootView.findViewById(R.id.sharedAlbunsView);
        _layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        _layoutManager = new GridLayoutManager(getContext(), 2); //2 columns
//        _layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        _adapter = new SharedAlbunsAdapter();

        _albunsView.setHasFixedSize(true);
        _albunsView.setLayoutManager(_layoutManager);
        _albunsView.setAdapter(_adapter);

        return rootView;
    }


}
