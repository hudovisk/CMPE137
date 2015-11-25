package com.assignment.sjsu.hudoassenco.cmpe137;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsFeedFragment extends Fragment {

    private RecyclerView mNewsFeedView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mLadapter;

    public NewsFeedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_feed, container, false);

        mNewsFeedView = (RecyclerView) rootView.findViewById(R.id.newsFeedView);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLadapter = new NewsFeedAdapter();

        mNewsFeedView.setHasFixedSize(true);
        mNewsFeedView.setLayoutManager(mLayoutManager);
        mNewsFeedView.setAdapter(mLadapter);

        return rootView;
    }

    private class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_feed, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 10;
        }

    }
}
