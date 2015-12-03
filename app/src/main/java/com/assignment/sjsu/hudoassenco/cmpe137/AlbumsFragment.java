package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class AlbumsFragment extends Fragment {

    private RecyclerView mAlbumsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private OwnedAlbumsAdapter mAdapter;
    private MenuItem mEditMenuItem;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_owned_album_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if(mAdapter != null) {
                if(mAdapter.getSelectedPositions().size() > 1) {
                    menu.findItem(R.id.edit_album_action).setVisible(false);
                } else {
                    menu.findItem(R.id.edit_album_action).setVisible(true);
                }
                return true;
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
        }
    };

    public AlbumsFragment() {
        mAdapter = new OwnedAlbumsAdapter(new ArrayList<Album>());
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

        mAlbumsView.setHasFixedSize(true);
        mAlbumsView.setLayoutManager(mLayoutManager);
        mAlbumsView.setAdapter(mAdapter);

        // query for list of albums from the current user

        ParseQuery<Album> query = ParseQuery.getQuery("Album");
        query.whereEqualTo("author", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Album>() {
            public void done(List<Album> albums, ParseException e) {
                if(e == null) {
                    mAdapter.setAlbums(albums);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        return rootView;
    }

    private class OwnedAlbumsAdapter extends RecyclerView.Adapter<OwnedAlbumsAdapter.ViewHolder> {

        private List<Album> mAlbums;
        private List<Integer> mSelectedPositions;

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnLongClickListener, View.OnClickListener {

            public RelativeLayout mAlbumLayout;
            public ImageView mThumbnailView;
            public TextView mAlbumNameView;

            public ViewHolder(View itemView) {
                super(itemView);
//                itemView.setOnLongClickListener(this);

                mAlbumLayout = (RelativeLayout) itemView.findViewById(R.id.album_layout);
                mThumbnailView = (ImageView) itemView.findViewById(R.id.album_thumbnail);
                mAlbumNameView = (TextView) itemView.findViewById(R.id.album_name);

                mAlbumLayout.setOnLongClickListener(this);
                mAlbumLayout.setOnClickListener(this);
            }


            @Override
            public boolean onLongClick(View v) {
                if(mActionMode == null) {
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    v.setActivated(true);
                    mSelectedPositions.add(getAdapterPosition());
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

        public OwnedAlbumsAdapter(List<Album> mAlbums) {
            this.mAlbums = mAlbums;
            mSelectedPositions = new ArrayList<>();
        }

            @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_album, parent, false);

            ViewHolder viewHolder = new ViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mAlbumNameView.setText(mAlbums.get(position).getName());
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
