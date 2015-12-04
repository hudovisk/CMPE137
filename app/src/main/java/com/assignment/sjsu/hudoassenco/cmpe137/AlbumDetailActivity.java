package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hudoassenco on 12/2/15.
 */
public class AlbumDetailActivity extends AppCompatActivity {

    public static final String ALBUM_ID_EXTRA = "id";
    public static final String ALBUM_NAME_EXTRA = "name";

    private RecyclerView mPhotosListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PhotosAdapter mAdapter;

    private LruCache<String, Bitmap> mMemoryCache;

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
                case R.id.delete_photo_action:
                    List<Photo> selectedPhotos = new ArrayList<>();
                    for(Integer position : mAdapter.getSelectedPositions()) {
                        selectedPhotos.add(mAdapter.getPhotos().get(position));
                    }
                    for(int i=0; i < selectedPhotos.size(); i++) {
                        final Photo photo = selectedPhotos.get(i);
                        //TODO: Remove photo
                        if(photo.getAuthor() == ParseUser.getCurrentUser()){
                            ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
                            query.whereEqualTo("originPhoto", photo);
                            query.findInBackground(new FindCallback<Comment>() {
                                public void done(List<Comment> results, ParseException e) {
                                    if (e == null) {
                                        for (int i = 0; i < results.size(); i++) {
                                            results.get(i).deleteInBackground();
                                        }
                                    }
                                }
                            });
                           photo.deleteInBackground();


                            Context context = getApplicationContext();
                            CharSequence text = selectedPhotos.size()+" photos deleted";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }else{
                            selectedPhotos.remove(i);
                            Context context = getApplicationContext();
                            CharSequence text = "You can't delete photos from other users";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }

                    mAdapter.getPhotos().removeAll(selectedPhotos);
                    mAdapter.notifyDataSetChanged();
                    mActionMode.finish();
                    return true;
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

    public AlbumDetailActivity() {
        mAdapter = new PhotosAdapter(new ArrayList<Photo>());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_album_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

//        mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mAdapter = new PhotosAdapter(new ArrayList<Photo>());

        mPhotosListView = (RecyclerView) this.findViewById(R.id.album_detail_photos);
        mPhotosListView.setLayoutManager(mLayoutManager);
        mPhotosListView.setAdapter(mAdapter);

        Intent intent = getIntent();
        String albumId = intent.getStringExtra(ALBUM_ID_EXTRA);
        if(!albumId.isEmpty()) {
            String name = intent.getStringExtra(ALBUM_NAME_EXTRA);
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

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }

    private class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

        private List<Photo> mPhotos;
        private List<Integer> mSelectedPositions;

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            public LinearLayout mPhotoLayout;
            public ImageView mImageView;

            public ViewHolder(View itemView) {
                super(itemView);

                mPhotoLayout = (LinearLayout) itemView.findViewById(R.id.photo_layout);
                mImageView = (ImageView) itemView.findViewById(R.id.photo);

                mPhotoLayout.setOnClickListener(this);
                mPhotoLayout.setOnLongClickListener(this);
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
                    final Photo photo = mPhotos.get(getAdapterPosition());
                    Intent intent = new Intent(AlbumDetailActivity.this, PhotoDetailActivity.class);
                    intent.putExtra(PhotoDetailActivity.PHOTO_ID_EXTRA, photo.getObjectId());
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
                }
            }
        }

        public PhotosAdapter(List<Photo> mPhotos) {
            this.mPhotos = mPhotos;
            mSelectedPositions = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_photo, parent, false);

            ViewHolder viewHolder = new ViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Photo photo = mPhotos.get(position);

            holder.mPhotoLayout.setActivated(mSelectedPositions.contains(position));
            final Bitmap bitmap = getBitmapFromMemCache(photo.getObjectId());
            if(bitmap != null) {
                holder.mImageView.setImageBitmap(bitmap);
            } else {
                photo.getImage().getDataInBackground(new GetDataCallback() {
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

                            addBitmapToMemoryCache(photo.getObjectId(), image);

                            holder.mImageView.setImageBitmap(image);
                        }
                    }
                });
            }

        }

        public void clear() {
            for(int i=0; i < mPhotos.size(); i++)
                mPhotos.set(i, null);
            mPhotos.clear();
            mSelectedPositions.clear();
        }

        public void clearSelected() {
            mSelectedPositions.clear();
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
