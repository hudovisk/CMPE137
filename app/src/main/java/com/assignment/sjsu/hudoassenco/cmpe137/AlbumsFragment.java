package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Size;
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

public class AlbumsFragment extends Fragment {

    private RecyclerView mAlbumsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private AlbumsAdapter mAdapter;

    private MenuItem mEditMenuItem;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_album_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if(mAdapter != null) {
                final List<Integer> selectedPositions = mAdapter.getSelectedPositions();
                if(!selectedPositions.isEmpty()) {
                    final List<Album> albuns = mAdapter.getAlbums();
                    if(selectedPositions.size() > 1) {
                        menu.findItem(R.id.edit_album_action).setVisible(false);
                    } else {
                        String authorId = albuns.get(selectedPositions.get(0)).getAuthor().getObjectId();
                        if(authorId.equals(ParseUser.getCurrentUser().getObjectId())){
                            menu.findItem(R.id.edit_album_action).setVisible(true);
                        }
                    }
                    return true;
                }
            }
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
            mAdapter.clearSelected();
            mAdapter.notifyDataSetChanged();
        }
    };

    public AlbumsFragment() {
        mAdapter = new AlbumsAdapter(new ArrayList<Album>());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);

        mAlbumsView = (RecyclerView) rootView.findViewById(R.id.albums_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        mAlbumsView.setHasFixedSize(true);
        mAlbumsView.setLayoutManager(mLayoutManager);
        mAlbumsView.setAdapter(mAdapter);

        // query for list of albums from the current user
        ParseQuery<Album> queryAuthor = ParseQuery.getQuery("Album");
        ParseQuery<Album> queryCollaborator = ParseQuery.getQuery("Album");

        queryAuthor.whereEqualTo("author", ParseUser.getCurrentUser());
        queryCollaborator.whereEqualTo("collaborators", ParseUser.getCurrentUser());

        List<ParseQuery<Album>> queries = new ArrayList<>();
        queries.add(queryAuthor);
        queries.add(queryCollaborator);

        ParseQuery<Album> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground(new FindCallback<Album>() {
            public void done(List<Album> results, ParseException e) {
                if(e == null) {
                    mAdapter.setAlbums(results);
                    mAlbumsView.scrollToPosition(0);
                }
            }
        });

        return rootView;
    }

    private class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder>
            implements BitmapDownloader.OnBitmapDownloadedListenner<AlbumsAdapter.ViewHolder> {

        private List<Album> mAlbums;
        private List<Integer> mSelectedPositions;
        private BitmapDownloader<ViewHolder> mBitmapDownloader;

        public void clearSelected() {
            mSelectedPositions.clear();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnLongClickListener, View.OnClickListener {

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
                    mActionMode.invalidate();
                    return true;
                }
                return false;
            }


            @Override
            public void onClick(View v) {
                if(mActionMode == null) {
                    final Album album = mAlbums.get(getAdapterPosition());
                    Intent intent = new Intent(getContext(), AlbumDetailActivity.class);
                    intent.putExtra("id", album.getObjectId());
                    intent.putExtra("name", album.getName());
                    startActivity(intent);
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
                    mActionMode.invalidate();
                }
            }
        }

        public AlbumsAdapter(List<Album> mAlbums) {
            this.mAlbums = mAlbums;
            mSelectedPositions = new ArrayList<>();

            mBitmapDownloader = new BitmapDownloader<>(new Handler());
            mBitmapDownloader.setOnBitmapDownloadedListenner(this);
            mBitmapDownloader.start();
            mBitmapDownloader.getLooper();
        }

            @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_album, parent, false);

            ViewHolder viewHolder = new ViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBitmapDownloaded(ViewHolder holder, Bitmap image) {
            holder.mAuthorPictureView.setImageBitmap(image);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Album album = mAlbums.get(position);

            holder.mAlbumLayout.setActivated(mSelectedPositions.contains(position));

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

                                    int width = holder.mAuthorPictureView.getWidth();
                                    int height = holder.mAuthorPictureView.getHeight();

                                    mBitmapDownloader.queueUrl(holder, pictureUrl, new Size(width,height));
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
