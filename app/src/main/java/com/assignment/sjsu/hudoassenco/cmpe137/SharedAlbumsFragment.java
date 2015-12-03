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
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.List;


public class SharedAlbumsFragment extends Fragment {

    private RecyclerView mAlbumsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedAlbumsAdapter mAdapter;

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
        mAdapter = new SharedAlbumsAdapter(new ArrayList<Album>());
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
        mAlbumsView.setAdapter(mAdapter);

        ParseQuery<Album> query = ParseQuery.getQuery("Album");
        query.whereEqualTo("collaborators", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Album>() {
            public void done(List<Album> albums, ParseException e) {
                if (e == null) {
                    mAdapter.setAlbums(albums);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        return rootView;
    }

    private class SharedAlbumsAdapter extends RecyclerView.Adapter<SharedAlbumsAdapter.ViewHolder> implements BitmapDownloader.OnBitmapDownloadedListenner<SharedAlbumsAdapter.ViewHolder> {

        private List<Album> mAlbums;
        private List<Integer> mSelectedPositions;
        private BitmapDownloader<ViewHolder> mBitmapDownloader;

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnLongClickListener, View.OnClickListener{

            public RelativeLayout mAlbumLayout;
            public ImageView mThumbnailView;
            public ImageView mAuthorPictureView;
            public TextView mAuthorNameView;
            public TextView mAlbumNameView;
            public TextView mNumberCollaboratorsView;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnLongClickListener(this);

                mAlbumLayout = (RelativeLayout) itemView.findViewById(R.id.album_layout);
                mThumbnailView = (ImageView) itemView.findViewById(R.id.album_thumbnail);
                mAuthorPictureView = (ImageView) itemView.findViewById(R.id.album_profile_pic);
                mAuthorNameView = (TextView) itemView.findViewById(R.id.album_author_view);
                mAlbumNameView = (TextView) itemView.findViewById(R.id.album_name_view);
                mNumberCollaboratorsView = (TextView) itemView.findViewById(R.id.album_number_contributors);

                mAlbumLayout.setOnClickListener(this);
                mAlbumLayout.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                if(mActionMode == null) {
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    v.setActivated(true);
                    mSelectedPositions.add(getAdapterPosition());
                }
                return true;
            }

            @Override
            public void onClick(View v) {
                if(mActionMode == null) {
                    //TODO:Open Album Detail Activity
                } else {
                    Integer position = getAdapterPosition();
                    if(mSelectedPositions.contains(position)) {
                        v.setActivated(false);
                        mSelectedPositions.remove(position);
                        if(mSelectedPositions.isEmpty()) {
                            mActionMode.finish();
                            return;
                        }
                    } else {
                        v.setActivated(true);
                        mSelectedPositions.add(position);
                    }
                }
            }
        }

        public SharedAlbumsAdapter(List<Album> mAlbums) {
            this.mAlbums = mAlbums;
            mSelectedPositions = new ArrayList<>();

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

            String facebookId = null;
            try {
                facebookId = album.getAuthor().fetchIfNeeded().getString("facebookId");

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
                                    holder.mNumberCollaboratorsView.setText(String.valueOf(album.getNumberOfCollaborators()));
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
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mAlbums.size();
        }

        public List<Album> getAlbums() {
            return mAlbums;
        }

        public void setAlbums(List<Album> albums) {
            this.mAlbums = albums;
        }

        public List<Integer> getSelectedPositions() {
            return mSelectedPositions;
        }

        public void setSelectedPositions(List<Integer> selectedPositions) {
            this.mSelectedPositions = selectedPositions;
        }
    }

}
