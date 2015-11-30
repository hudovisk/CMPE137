package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeoutException;

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

        //TODO: Back end to query the news feed (Author id, picture link, etc..

        mNewsFeedView.setHasFixedSize(true);
        mNewsFeedView.setLayoutManager(mLayoutManager);
        mNewsFeedView.setAdapter(mLadapter);

        return rootView;
    }

    private class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mProfilePictureView;
            public TextView mAlbumNameView;
            public TextView mAuthorNameView;
            public TextView mPlacePictureView;
            public ImageView mPictureView;
            public TextView mDescriptionView;

            public ViewHolder(View rootView) {
                super(rootView);

                mProfilePictureView = (ImageView) rootView.findViewById(R.id.feed_profile_picture);
                mAlbumNameView = (TextView) rootView.findViewById(R.id.feed_album_name);
                mAuthorNameView = (TextView) rootView.findViewById(R.id.feed_author_name);
                mPlacePictureView = (TextView) rootView.findViewById(R.id.feed_place_picture);
                mPictureView = (ImageView) rootView.findViewById(R.id.feed_picture);
                mDescriptionView = (TextView) rootView.findViewById(R.id.feed_description);

                mPictureView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(v.equals(mPictureView)) {
                    //TODO: Pass a photo identifier to PhotoDetailActivity to query more information.
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), mPictureView, "pictureView");
                    Intent intent = new Intent(getContext(), PhotoDetailActivity.class);
                    getActivity().startActivity(intent, optionsCompat.toBundle());
                }
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
