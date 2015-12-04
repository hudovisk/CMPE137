package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private RecyclerView mResultsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchResultAdapter mAdapter;
    private List<Album> mAllAlbums;
    private ArrayList<Uri> mImageUris;
    private boolean mSendPhoto;
    private ProgressDialog mProgressDialog;
    private int mCountPhotos;


    public SearchableActivity() {
        mAllAlbums = new ArrayList<>();
        mAdapter = new SearchResultAdapter(mAllAlbums);
        mAdapter.setHasStableIds(true);
        mImageUris = new ArrayList<Uri>();
        mSendPhoto = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mResultsView = (RecyclerView) findViewById(R.id.result_view);
        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        mResultsView.setLayoutManager(mLayoutManager);
        mResultsView.setAdapter(mAdapter);

        mProgressDialog = new ProgressDialog(SearchableActivity.this, R.style.AppTheme_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.send_progress));

        Intent intent = getIntent();
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search_action_search_view).getActionView();
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);

        // TODO: Bug with searchView and request focus. Keyboard not showing up.

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        getAllAlbums();
        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("CMPE137", "Query: " + query);
            mSendPhoto = false;
        } else {
            mSendPhoto = true;

            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSendImage(intent); // Handle single image being sent
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSendMultipleImages(intent); // Handle multiple images being sent
                }
            }
        }
    }


    private void getAllAlbums() {
        ParseQuery<Album> queryAuthor = ParseQuery.getQuery("Album");
        queryAuthor.whereEqualTo("author", ParseUser.getCurrentUser());
        ParseQuery<Album> queryCollaborator = ParseQuery.getQuery("Album");
        queryCollaborator.whereEqualTo("collaborators", ParseUser.getCurrentUser());
        List<ParseQuery<Album>> queries = new ArrayList<>();
        queries.add(queryAuthor);
        queries.add(queryCollaborator);
        ParseQuery<Album> mainQuery = ParseQuery.or(queries);
        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground(new FindCallback<Album>() {
            public void done(List<Album> results, ParseException e) {
                if(e == null) {
                    mAllAlbums = results;
                    mAdapter.setAlbums(mAllAlbums);
                    mResultsView.scrollToPosition(0);
                }
            }
        });
    }

    private void handleSendMultipleImages(Intent intent) {
        mImageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (mImageUris != null) {
            // Update UI to reflect multiple images being shared

        }
    }

    private void handleSendImage(Intent intent)  {
        mImageUris.add((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
//        if(imageUri != null){
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] data = stream.toByteArray();
//                ParseFile file = new ParseFile("teste.jpg", data);
//                file.saveInBackground();
//
//                ParseObject photo = new ParseObject("Photo");
//                photo.put("image", file);
//                photo.saveInBackground();
//
//
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//
//        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<Album> filteredModelList = filter(mAllAlbums, query);
        mAdapter.setAlbums(filteredModelList);
        mResultsView.scrollToPosition(0);
        return true;
    }

    private List<Album> filter(List<Album> albums, String query) {
        query = query.toLowerCase();

        final List<Album> filteredAlbumsList = new ArrayList<>();
        for (Album album : albums) {
            final String text = album.getName().toLowerCase();
            if (text.contains(query)) {
                filteredAlbumsList.add(album);
            }
        }
        return filteredAlbumsList;
    }

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> implements BitmapDownloader.OnBitmapDownloadedListenner<ImageView> {

        private List<Album> mAlbums;
        private BitmapDownloader<ImageView> mBitmapDownloader;

        @Override
        public void onBitmapDownloaded(ImageView holder, Bitmap image) {
            int width = holder.getWidth();
            int height = holder.getHeight();
            Bitmap thumbail = ThumbnailUtils.extractThumbnail(image, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            holder.setImageBitmap(thumbail);
        }

        public class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

            public ImageView mThumbnailView;
            public ImageView mAuthorPictureView;
            public TextView mAuthorNameView;
            public TextView mAlbumNameView;
            public TextView mNumberCollaboratorsView;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                mThumbnailView = (ImageView) itemView.findViewById(R.id.album_thumbnail);
                mAuthorPictureView = (ImageView) itemView.findViewById(R.id.album_profile_pic);
                mAuthorNameView = (TextView) itemView.findViewById(R.id.album_author_view);
                mAlbumNameView = (TextView) itemView.findViewById(R.id.album_name_view);
                mNumberCollaboratorsView = (TextView) itemView.findViewById(R.id.album_number_contributors);
            }

            @Override
            public void onClick(View v) {
                //TODO: Check flag
                final Album album = mAlbums.get(getAdapterPosition());
                mCountPhotos = mImageUris.size();
                if (mSendPhoto){
                    mProgressDialog.show();
                    for(Uri imageUri : mImageUris){
                        try {
                            final Bitmap bitmap = MediaStore.Images.Media.getBitmap(SearchableActivity.this.getContentResolver(), imageUri);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                            final byte[] data = stream.toByteArray();

                            ParseFile file = new ParseFile("teste.jpg", data);

                            file.saveInBackground(new SaveCallback() {
                                public void done(ParseException e) {
                                    // Handle success or failure here ...
                                    mCountPhotos--;
                                    if (mCountPhotos == 0) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                            }, new ProgressCallback() {
                                public void done(Integer percentDone) {
                                    // Update your progress spinner here. percentDone will be between 0 and 100.
                                }
                            });

                            Photo photo = new Photo();
                            photo.setImage(file);
                            photo.setAlbum(album);
                            photo.setAuthor(ParseUser.getCurrentUser());
                            photo.saveInBackground();

                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        mSendPhoto = false;

                    }

                }else{
                    Intent intent = new Intent(SearchableActivity.this, AlbumDetailActivity.class);
                    intent.putExtra(AlbumDetailActivity.ALBUM_ID_EXTRA, album.getObjectId());
                    intent.putExtra(AlbumDetailActivity.ALBUM_NAME_EXTRA, album.getName());
                    startActivity(intent);
                }

            }
        }

        public SearchResultAdapter(List<Album> mAlbums) {
            this.mAlbums = mAlbums;

            mBitmapDownloader = new BitmapDownloader<>(new Handler());
            mBitmapDownloader.setOnBitmapDownloadedListenner(this);
            mBitmapDownloader.start();
            mBitmapDownloader.getLooper();
        }

        @Override
        public long getItemId(int position) {
            return mAlbums.get(position).getObjectId().hashCode();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().from(parent.getContext())
                    .inflate(R.layout.card_search_result, parent, false);

            ViewHolder holder = new ViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Album album = mAlbums.get(position);

            holder.mAlbumNameView.setText(album.getName());

            String facebookId = null;
            try {
                facebookId = album.getAuthor().fetchIfNeeded().getString("facebookId");

                ParseQuery<Photo> query = ParseQuery.getQuery(Photo.class);
                query.whereEqualTo("originAlbum", album);
                query.findInBackground(new FindCallback<Photo>() {
                    public void done(List<Photo> scoreList, ParseException e) {
                        if (e == null) {
                            if(!scoreList.isEmpty()) {
                                int width = holder.mThumbnailView.getWidth();
                                int height = holder.mThumbnailView.getHeight();
                                mBitmapDownloader.queueUrl(holder.mThumbnailView, scoreList.get(0).getImage().getUrl(), new Size(width, height));
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

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

                                    mBitmapDownloader.queueUrl(holder.mAuthorPictureView, pictureUrl, new Size(width,height));
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

        public void setAlbums(List<Album> mAlbums) {
            this.mAlbums = mAlbums;
        }
    }
}
