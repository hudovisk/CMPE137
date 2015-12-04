package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hudoassenco on 11/29/15.
 */
public class PhotoDetailActivity extends AppCompatActivity {

    public static final String PHOTO_ID_EXTRA = "id";

    private Photo mPhoto;

    private ImageView mPictureView;

    private EditText mCommentEditText;
    private ImageButton mSendCommentBt;

    private RecyclerView mCommentsListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CommentAdapter mAdapter;

    private LruCache<String, Bitmap> mMemoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_photo_detail);

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

        mPictureView = (ImageView) findViewById(R.id.photo_detail_photo);
        mCommentEditText = (EditText) findViewById(R.id.photo_detail_comment_box);
        mSendCommentBt = (ImageButton) findViewById(R.id.photo_detail_send_bt);

        mCommentsListView = (RecyclerView) findViewById(R.id.photo_detail_comments_list);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new CommentAdapter(new ArrayList<Comment>());

        mCommentsListView.setLayoutManager(mLayoutManager);
        mCommentsListView.setAdapter(mAdapter);

        mSendCommentBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendCommentBtClicked();
            }
        });

        Intent intent = getIntent();
        String photoId = intent.getStringExtra(PHOTO_ID_EXTRA);
        if(!photoId.isEmpty()) {
            ParseQuery<Photo> query = ParseQuery.getQuery(Photo.class);
            query.whereEqualTo("objectId", photoId);
            query.findInBackground(new FindCallback<Photo>() {
                public void done(List<Photo> scoreList, ParseException e) {
                    if (e == null) {
                        if(!scoreList.isEmpty()) {
                            mPhoto = scoreList.get(0);
                            bindPhotoInfo();
                        }
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

    private void bindPhotoInfo() {
        Bitmap image = getBitmapFromMemCache(mPhoto.getObjectId());
        if(image == null) {
            mPhoto.getImage().getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                    addBitmapToMemoryCache(mPhoto.getObjectId(), image);
                    mPictureView.setImageBitmap(image);
                }
            });
        } else {
            mPictureView.setImageBitmap(image);
        }
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.whereEqualTo("originPhoto", mPhoto);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Comment>() {
            public void done(List<Comment> scoreList, ParseException e) {
                if (e == null) {
                    if (!scoreList.isEmpty()) {
                        mAdapter.setComments(scoreList);
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onSendCommentBtClicked() {
        String text = mCommentEditText.getText().toString();
        if(!text.isEmpty()) {
            Comment comment = new Comment();
            comment.setAuthor(ParseUser.getCurrentUser());
            comment.setText(text);
            comment.setPhoto(mPhoto);
            comment.saveInBackground();
            mAdapter.addComment(comment);
            mCommentEditText.setText("");
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>
            implements BitmapDownloader.OnBitmapDownloadedListenner<CommentAdapter.ViewHolder> {

        private List<Comment> mComments;
        private BitmapDownloader<ViewHolder> mBitmapDownloader;
        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView mProfilePictureView;
            public TextView mCommentTextView;

            public ViewHolder(View itemView) {
                super(itemView);

                mProfilePictureView = (ImageView) itemView.findViewById(R.id.profile_picture);
                mCommentTextView = (TextView) itemView.findViewById(R.id.comment_text);
            }
        }

        public CommentAdapter(List<Comment> comments) {
            this.mComments = comments;

            mBitmapDownloader = new BitmapDownloader<>(new Handler());
            mBitmapDownloader.setOnBitmapDownloadedListenner(this);
            mBitmapDownloader.start();
            mBitmapDownloader.getLooper();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(PhotoDetailActivity.this)
                    .inflate(R.layout.card_comment, parent, false);

            ViewHolder holder = new ViewHolder(itemView);

            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Comment comment = mComments.get(position);

            holder.mCommentTextView.setText(comment.getText());
            final String facebookId;
            try {
                facebookId = comment.getAuthor().fetchIfNeeded().getString("facebookId");
                Bitmap image = getBitmapFromMemCache(facebookId);
                if(image == null) {
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

                                        int width = holder.mProfilePictureView.getWidth();
                                        int height = holder.mProfilePictureView.getHeight();

                                        holder.mProfilePictureView.setTag(facebookId);
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
                } else {
                    holder.mProfilePictureView.setImageBitmap(image);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapDownloaded(ViewHolder holder, Bitmap image) {
            String facebookId = String.valueOf(holder.mProfilePictureView.getTag());
            addBitmapToMemoryCache(facebookId, image);
            holder.mProfilePictureView.setImageBitmap(image);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void addComment(Comment comment) {
            mComments.add(0, comment);
            notifyItemInserted(0);
        }

        public List<Comment> getComments() {
            return mComments;
        }

        public void setComments(List<Comment> comments) {
            this.mComments = comments;
        }
    }

}
