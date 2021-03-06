package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView mProfilePictureView;
    private TextView mProfileNameView;
    private Button mLogoutButton;
    private TextView mAlbumNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mProfilePictureView = (ImageView) rootView.findViewById(R.id.profile_picture);
        mProfileNameView = (TextView) rootView.findViewById(R.id.profile_name);
        mAlbumNumber = (TextView) rootView.findViewById(R.id.profile_albums_number);
        mLogoutButton = (Button) rootView.findViewById(R.id.profile_logout_bt);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        ParseUser user = ParseUser.getCurrentUser();
//        mProfileNameView.setText(user.getString("name"));


        ParseQuery<Album> queryAuthor = ParseQuery.getQuery("Album");
        ParseQuery<Album> queryCollaborator = ParseQuery.getQuery("Album");

        queryAuthor.whereEqualTo("author", ParseUser.getCurrentUser());
        queryCollaborator.whereEqualTo("collaborators", ParseUser.getCurrentUser());

        List<ParseQuery<Album>> queries = new ArrayList<>();
        queries.add(queryAuthor);
        queries.add(queryCollaborator);

        ParseQuery<Album> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<Album>() {
            public void done(List<Album> scoreList, ParseException e) {
                if (e == null) {
                    mAlbumNumber.setText(String.valueOf(scoreList.size()));
                }
            }
        });

        if(ParseFacebookUtils.isLinked(user)) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            JSONObject result = response.getJSONObject();
                            try {
                                String name = result.getString("name");
                                mProfileNameView.setText(name);

                                JSONObject picture = result.getJSONObject("picture").getJSONObject("data");
                                String urlPicture = picture.getString("url");
                                DownloadImageTask task = new DownloadImageTask(mProfilePictureView);
                                task.execute(urlPicture);
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

        return rootView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImageView;

        public DownloadImageTask(ImageView imageView) {
            this.mImageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.d("CMPE137", "Downloading image: " + urls[0]);
            String url = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", (e.getMessage() == null) ? "Error downloading image." : e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            this.mImageView.setImageBitmap(result);
        }
    }
}
