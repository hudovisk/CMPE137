package com.assignment.sjsu.hudoassenco.cmpe137;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by hudoassenco on 11/29/15.
 */
public class PhotoDetailActivity extends AppCompatActivity {

    public static final String PHOTO_ID_EXTRA = "id";

    private ImageView mPictureView;
    private ImageButton mLikeBt;
    private ImageView mProfilePictureView;
    private TextView mDescriptionView;
    private TextView mLikesTextView;

    private EditText mCommentEditText;
    private ImageButton mSendCommentBt;

    private RecyclerView mCommentsListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_photo_detail);

        mPictureView = (ImageView) findViewById(R.id.photo_detail_photo);
        mLikeBt = (ImageButton) findViewById(R.id.photo_detail_like_bt);
        mProfilePictureView = (ImageView) findViewById(R.id.photo_detail_profile_picture);
        mDescriptionView = (TextView) findViewById(R.id.photo_detail_description);
        mLikesTextView = (TextView) findViewById(R.id.photo_detail_number_likes);

        mCommentEditText = (EditText) findViewById(R.id.photo_detail_comment_box);
        mSendCommentBt = (ImageButton) findViewById(R.id.photo_detail_send_bt);

        mCommentsListView = (RecyclerView) findViewById(R.id.photo_detail_comments_list);

        mLikeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLikeBtClicked();
            }
        });
        mSendCommentBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendCommentBtClicked();
            }
        });

        //TODO: Back end to query informations about the photo, comments, likes, etc.
    }

    private void onSendCommentBtClicked() {
        String comment = mCommentEditText.getText().toString();
        //TODO: Back end to save comment.
    }

    private void onLikeBtClicked() {
        if(isUserLikedPicture()) {
            //TODO: Back end to unlike the picture
            mLikeBt.setImageResource(R.drawable.thumbs_outline);
        } else {
            //TODO: Back end to like the picture
            mLikeBt.setImageResource(R.drawable.thumbs_fill);
        }
    }

    private boolean isUserLikedPicture() {
        //TODO: back end to query if the user liked the picture
        return true;
    }
}
