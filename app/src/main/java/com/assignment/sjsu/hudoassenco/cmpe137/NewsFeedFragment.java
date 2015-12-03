package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedFragment extends Fragment {

    private RecyclerView mNewsFeedView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NewsFeedAdapter mAdapter;

    public NewsFeedFragment() {
        mAdapter = new NewsFeedAdapter(new ArrayList<Feed>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_feed, container, false);

        mNewsFeedView = (RecyclerView) rootView.findViewById(R.id.newsFeedView);
        mLayoutManager = new LinearLayoutManager(getContext());


        mNewsFeedView.setHasFixedSize(true);
        mNewsFeedView.setLayoutManager(mLayoutManager);
        mNewsFeedView.setAdapter(mAdapter);

        ParseQuery<Feed> query = ParseQuery.getQuery("Feed");

        query.findInBackground(new FindCallback<Feed>() {
            public void done(List<Feed> feeds, ParseException e) {
                if (e == null) {
                    mAdapter.setFeeds(feeds);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        return rootView;
    }

    private class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> implements BitmapDownloader.OnBitmapDownloadedListenner<NewsFeedAdapter.ViewHolder> {

        private List<Feed> mFeeds;
        private BitmapDownloader<ViewHolder> mBitmapDownloader;

        public List<Feed> getmFeeds() {
            return mFeeds;
        }

        public void setFeeds(List<Feed> mFeeds) {
            this.mFeeds = mFeeds;
        }

        public NewsFeedAdapter (List<Feed> feeds){
            this.mFeeds = feeds;

            mBitmapDownloader = new BitmapDownloader<>(new Handler());
            mBitmapDownloader.setOnBitmapDownloadedListenner(this);
            mBitmapDownloader.start();
            mBitmapDownloader.getLooper();
        }

        @Override
        public void onBitmapDownloaded(ViewHolder holder, Bitmap image) {
            holder.mProfilePictureView.setImageBitmap(image);
        }

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
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                            Pair.create((View) mPictureView, "pictureView"),
                            Pair.create((View) mProfilePictureView, "profilePictureView"));
//                            Pair.create((View) mDescriptionView, "descriptionView"));
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
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Feed feed = mFeeds.get(position);

            try {
                final Album album = feed.getAlbum().fetchIfNeeded();
                holder.mAlbumNameView.setText(album.getName());
                final String facebookId = album.getAuthor().fetchIfNeeded().getString("facebookId");

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                GraphRequest request = GraphRequest.newGraphPathRequest(
                        accessToken,
                        "/"+facebookId,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject result = response.getJSONObject();
                                try {
                                    String name = result.getString("name");
                                    String pictureUrl = result.getJSONObject("picture")
                                            .getJSONObject("data")
                                            .getString("url");
                                    holder.mAuthorNameView.setText(name);

                                    int width = holder.mProfilePictureView.getWidth();
                                    int height = holder.mProfilePictureView.getHeight();

                                    mBitmapDownloader.queueUrl(holder, pictureUrl, new Size(width, height));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,picture");
                request.setParameters(parameters);
                request.executeAsync();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        @Override
        public int getItemCount() {
            return mFeeds.size();
        }

    }
}
