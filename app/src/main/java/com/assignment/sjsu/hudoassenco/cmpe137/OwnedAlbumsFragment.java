package com.assignment.sjsu.hudoassenco.cmpe137;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OwnedAlbumsFragment extends Fragment {

    private RecyclerView mAlbumsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    public OwnedAlbumsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shared_albums, container, false);

        mAlbumsView = (RecyclerView) rootView.findViewById(R.id.shared_albums_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        mLayoutManager = new GridLayoutManager(getContext(), 2); //2 columns
//        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mAdapter = new OwnedAlbumsAdapter();

        mAlbumsView.setHasFixedSize(true);
        mAlbumsView.setLayoutManager(mLayoutManager);
        mAlbumsView.setAdapter(mAdapter);

        return rootView;
    }

    private class OwnedAlbumsAdapter extends RecyclerView.Adapter<OwnedAlbumsAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

}
