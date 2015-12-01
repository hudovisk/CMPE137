package com.assignment.sjsu.hudoassenco.cmpe137;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class SharedAlbumsFragment extends Fragment {

    private RecyclerView mAlbumsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_shared_album_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    public SharedAlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shared_albums, container, false);

        mAlbumsView = (RecyclerView) rootView.findViewById(R.id.shared_albums_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        mLayoutManager = new GridLayoutManager(getContext(), 2); //2 columns
//        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mAlbumsView.setHasFixedSize(true);
        mAlbumsView.setLayoutManager(mLayoutManager);

        ParseQuery<Album> query = ParseQuery.getQuery("Album");
        query.whereEqualTo("collaborators", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Album>() {
            public void done(List<Album> albums, ParseException e) {
                if (e == null) {
                    mAdapter = new SharedAlbumsAdapter(albums);
                    mAlbumsView.setAdapter(mAdapter);
                }
            }
        });

        return rootView;
    }

    private class SharedAlbumsAdapter extends RecyclerView.Adapter<SharedAlbumsAdapter.ViewHolder> implements BitmapDownloader.OnBitmapDownloadedListenner<SharedAlbumsAdapter.ViewHolder> {

        private List<Album> mAlbums;
        private BitmapDownloader<ViewHolder> mBitmapDownloader;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{

            public ImageView mThumbnailView;
            public ImageView mAuthorPictureView;
            public TextView mAuthorNameView;
            public TextView mAlbumNameView;
            public TextView mNumberCollaboratorsView;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnLongClickListener(this);

                mThumbnailView = (ImageView) itemView.findViewById(R.id.album_thumbnail);
                mAuthorPictureView = (ImageView) itemView.findViewById(R.id.album_profile_pic);
                mAuthorNameView = (TextView) itemView.findViewById(R.id.album_author_view);
                mAlbumNameView = (TextView) itemView.findViewById(R.id.album_name_view);
                mNumberCollaboratorsView = (TextView) itemView.findViewById(R.id.album_number_contributors);
            }

            @Override
            public boolean onLongClick(View v) {
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                v.setSelected(true);
                return true;
            }
        }

        public SharedAlbumsAdapter(List<Album> mAlbums) {
            this.mAlbums = mAlbums;

            mBitmapDownloader = new BitmapDownloader<>(new Handler());
            mBitmapDownloader.setmOnBitmapDownloadedListenner(this);
            mBitmapDownloader.start();
            mBitmapDownloader.getLooper();
        }


        @Override
        public void onBitmapDownloaded(ViewHolder holder, Bitmap image) {
            holder.mAuthorPictureView.setImageBitmap(image);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_shared_album, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Album album = mAlbums.get(position);

            holder.mAlbumNameView.setText(album.getName());

            String facebookId = album.getAuthor().getString("facebookId");
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
                                String pictureUrl = result.getJSONObject("data")
                                        .getJSONObject("picture")
                                        .getString("url");
                                holder.mAlbumNameView.setText(name);
                                mBitmapDownloader.queueUrl(holder, pictureUrl);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "name,picture");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }

    }

}
