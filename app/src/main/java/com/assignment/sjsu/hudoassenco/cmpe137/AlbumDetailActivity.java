package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hudoassenco on 12/2/15.
 */
public class AlbumDetailActivity extends AppCompatActivity {

    private RecyclerView mPhotosListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PhotosAdapter mAdapter;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_album_detail_menu, menu);

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

    public AlbumDetailActivity() {
        mAdapter = new PhotosAdapter(new ArrayList<Photo>());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_album_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);

        mPhotosListView = (RecyclerView) this.findViewById(R.id.album_detail_photos);
        mPhotosListView.setLayoutManager(mLayoutManager);
        mPhotosListView.setAdapter(mAdapter);

        Intent intent = getIntent();
        String albumId = intent.getStringExtra("id");
        if(!albumId.isEmpty()) {
            String name = intent.getStringExtra("name");
            getSupportActionBar().setTitle(name);

            ParseQuery<Album> query = ParseQuery.getQuery(Album.class);
            query.whereEqualTo("objectId", albumId);
            query.findInBackground(new FindCallback<Album>() {
                public void done(List<Album> scoreList, ParseException e) {
                    if (e == null) {
                        final Album album = scoreList.get(0);
                        ParseQuery<Photo> query = ParseQuery.getQuery(Photo.class);
                        query.whereEqualTo("originAlbum", album);
                        query.findInBackground(new FindCallback<Photo>() {
                            public void done(List<Photo> scoreList, ParseException e) {
                                if (e == null) {
                                    Log.d("CMPE137", "Returned "+scoreList.size()+" photos");
                                    mAdapter.setPhotos(scoreList);
                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("CMPE137", "onPause");
//        mAdapter.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CMPE137", "onStop");

        mAdapter.clear();
    }

    private class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder>
            implements BitmapDownloader.OnBitmapDownloadedListenner<PhotosAdapter.ViewHolder> {

        private List<Photo> mPhotos;
        private List<Integer> mSelectedPositions;
        private BitmapDownloader<ViewHolder> mBitmapDownloader;

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            public ImageView mImageView;

            public ViewHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.photo);
            }

            @Override
            public boolean onLongClick(View v) {
                if(mActionMode == null) {
                    mActionMode = startActionMode(mActionModeCallback);
                    v.setActivated(true);
                    mSelectedPositions.add(getAdapterPosition());
                }
                return true;
            }

            @Override
            public void onClick(View v) {
                if(mActionMode == null) {
                    //TODO:Open Photo Detail Activity
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

        public PhotosAdapter(List<Photo> mPhotos) {
            this.mPhotos = mPhotos;
            mSelectedPositions = new ArrayList<>();

//            mBitmapDownloader = new BitmapDownloader<>(new Handler());
//            mBitmapDownloader.setOnBitmapDownloadedListenner(this);
//            mBitmapDownloader.start();
//            mBitmapDownloader.getLooper();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_photo, parent, false);

            ViewHolder viewHolder = new ViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBitmapDownloaded(ViewHolder holder, Bitmap image) {
            holder.mImageView.setImageBitmap(image);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Photo photo = mPhotos.get(position);
//            String url = photo.getImage().getUrl();
//            mBitmapDownloader.queueUrl(holder, url);
            mPhotos.get(position).getImage().getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(data, 0, data.length, options);

                        int imageWidth = holder.mImageView.getWidth();
                        int imageHeight = holder.mImageView.getHeight();

                        int sampleSize = Utils.calculateInSampleSize(options, imageWidth, imageHeight);

                        options.inJustDecodeBounds = false;
                        options.inSampleSize = sampleSize;
                        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                        holder.mImageView.setImageBitmap(image);
                    }
                }
            });
        }

        public void clear() {
            mPhotos.clear();
        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }

        public List<Photo> getPhotos() {
            return mPhotos;
        }

        public void setPhotos(List<Photo> photos) {
            this.mPhotos = photos;
        }

        public List<Integer> getSelectedPositions() {
            return mSelectedPositions;
        }

        public void setSelectedPositions(List<Integer> selectedPositions) {
            this.mSelectedPositions = selectedPositions;
        }
    }
}
