package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewAlbumActivity extends AppCompatActivity {

    private EditText mAlbumNameView;
    private EditText mAlbumDescription;

    private AutoCompleteTextView mCollaboratorNameInputView;

    private ImageButton mAddCollaboratorButton;

    private RecyclerView mCollaboratorsListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private ArrayList<String> mCollaboratorsSuggestions;
    private ArrayList<String> mCollaboratorsSuggestionsId;
    private ArrayList<String> mCollaboratorsSuggestionsPictureUrl;
    private ArrayList<CollaboratorsAdapter.Collaborator> mCollaborators;

    private boolean mNewAlbum;
    private Album mAlbumToEdit;

    public NewAlbumActivity() {
        mCollaboratorsSuggestions = new ArrayList<>();
        mCollaboratorsSuggestionsId = new ArrayList<>();
        mCollaboratorsSuggestionsPictureUrl = new ArrayList<>();
        mCollaborators = new ArrayList<>();

        mAdapter = new CollaboratorsAdapter(mCollaborators);
        mLayoutManager = new LinearLayoutManager(NewAlbumActivity.this, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("CMPE137", "onSaveInstanceState");

        outState.putInt("size", mCollaborators.size());

        for(int i=0; i< mCollaborators.size(); i++) {
            outState.putString("name_"+i, mCollaborators.get(i).mName);
            outState.putString("id_"+i, mCollaborators.get(i).mId);
            outState.putString("pictureUrl_"+i, mCollaborators.get(i).mPictureUrl);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d("CMPE137", "onRestoreInstanceState");

        int size = savedInstanceState.getInt("size");
        Log.d("CMPE137", "Size: "+size);
        mCollaborators = new ArrayList<>();
        for(int i=0; i<size; i++) {
            CollaboratorsAdapter.Collaborator collaborator = new CollaboratorsAdapter.Collaborator();
            collaborator.mName = savedInstanceState.getString("name_"+i);
            collaborator.mId = savedInstanceState.getString("id_"+i);
            collaborator.mPictureUrl = savedInstanceState.getString("pictureUrl_"+i);
            mCollaborators.add(collaborator);
        }
        mAdapter = new CollaboratorsAdapter(mCollaborators);
        mCollaboratorsListView.setAdapter(mAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_album));

        mCollaboratorNameInputView = (AutoCompleteTextView) findViewById(R.id.new_album_collaborator_input);
        mAlbumNameView = (EditText) findViewById(R.id.new_album_name_input);
        mAlbumDescription = (EditText) findViewById(R.id.new_album_description_input);

        mCollaboratorsListView = (RecyclerView) findViewById(R.id.new_album_colaborator_list);
        mAddCollaboratorButton = (ImageButton) findViewById(R.id.new_album_add_collaborator_bt);

        mCollaboratorsListView.setLayoutManager(mLayoutManager);
        mCollaboratorsListView.setAdapter(mAdapter);

        Intent intent = getIntent();
        handleIntent(intent);


        mAddCollaboratorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mCollaboratorNameInputView.getText().toString();
                String id = "";
                String pictureUrl = "";

                for (int i = 0; i < mCollaboratorsSuggestions.size(); i++) {
                    if (mCollaboratorsSuggestions.get(i).equals(name)) {
                        id = mCollaboratorsSuggestionsId.get(i);
                        pictureUrl = mCollaboratorsSuggestionsPictureUrl.get(i);
                    }
                }

                if (!id.isEmpty()) {
                    CollaboratorsAdapter.Collaborator collaborator = new CollaboratorsAdapter.Collaborator();
                    collaborator.mName = name;
                    collaborator.mId = id;
                    collaborator.mPictureUrl = pictureUrl;
                    mCollaborators.add(collaborator);
                    mAdapter.notifyItemInserted(mCollaborators.size() - 1);

                    mCollaboratorNameInputView.setText("");
                    mCollaboratorNameInputView.requestFocus();
                }
            }
        });

        //Get friends that use the same app
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(accessToken, facebookCallback);

        Bundle parameters = new Bundle();
        parameters.putString("fields", "friends{picture,name,id}");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void handleIntent(Intent intent) {
        if (intent.getStringExtra("id").length()!=0){
            // Edit album activity
            mNewAlbum = false;
            getSupportActionBar().setTitle("Edit Album");

            String albumId = intent.getStringExtra("id");
            ParseQuery<Album> query = ParseQuery.getQuery(Album.class);
            query.whereEqualTo("objectId", albumId);
            query.findInBackground(new FindCallback<Album>() {
                public void done(List<Album> albums, ParseException e) {
                    if (e == null) {
                        mAlbumToEdit = albums.get(0);
                        mAlbumNameView.setText(albums.get(0).getName());
                        mAlbumDescription.setText(albums.get(0).getDescription());
                    } else {
                        e.printStackTrace();
                    }
                }
            });



        }else{
            // New Album activity
            mNewAlbum = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_album_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_album_action: {
                if(mNewAlbum) {
                    //TODO: Save album here
                    final Album album = new Album();
                    final ParseRelation<ParseUser> relation = album.getRelation("collaborators");

                    for (int i = 0; i < mCollaborators.size(); i++) {
                        String facebookId = mCollaborators.get(i).mId;

                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("facebookId", facebookId);
                        query.findInBackground(new FindCallback<ParseUser>() {
                            public void done(List<ParseUser> objects, ParseException e) {
                                if (e == null) {
                                    // The query was successful.
                                    relation.add(objects.get(0));
                                    album.incrementNumberOfCollaborators();
                                    album.saveInBackground();

                                } else {
                                    // Something went wrong.
                                }
                            }
                        });
                    }

                    album.setAuthor(ParseUser.getCurrentUser());
                    album.setName(mAlbumNameView.getText().toString());
                    album.setDescription(mAlbumDescription.getText().toString());
                    album.saveInBackground();

                    //create new feed

                    Feed feed = new Feed();
                    feed.setAuthor(ParseUser.getCurrentUser());
                    feed.setAlbum(album);
                    feed.saveInBackground();

                    Context context = getApplicationContext();
                    CharSequence text = "New Album Created";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    finish();
                    return true;
                }else{
                    mAlbumToEdit.setName(mAlbumNameView.getText().toString());
                    mAlbumToEdit.setDescription(mAlbumDescription.getText().toString());
                    mAlbumToEdit.saveInBackground();

                    Context context = getApplicationContext();
                    CharSequence text = "New Album Created";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    finish();
                    return true;

                }
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    GraphRequest.GraphJSONObjectCallback facebookCallback = new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(JSONObject object, GraphResponse response) {
            Log.d("CMPE137", "Response from facebook. " + response.getRawResponse());
            JSONObject result = response.getJSONObject();
            try {
                JSONArray data = result.getJSONObject("friends").getJSONArray("data");
                mCollaboratorsSuggestions.clear();
                mCollaboratorsSuggestionsId.clear();
                for(int i=0; i < data.length(); i++) {
                    mCollaboratorsSuggestions.add(data.getJSONObject(i).getString("name"));
                    mCollaboratorsSuggestionsId.add(data.getJSONObject(i).getString("id"));
                    mCollaboratorsSuggestionsPictureUrl.add(data.getJSONObject(i)
                            .getJSONObject("picture")
                            .getJSONObject("data")
                            .getString("url"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewAlbumActivity.this, android.R.layout.simple_list_item_1, mCollaboratorsSuggestions);
                mCollaboratorNameInputView.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}

//ParseQuery<ParseUser> query = ParseUser.getQuery();
//query.whereEqualTo("facebookId", facebookId);
//        query.findInBackground(new FindCallback<ParseUser>() {
//public void done(List<ParseUser> objects, ParseException e) {
//        if (e == null) {
//        // The query was successful.
//        } else {
//        // Something went wrong.
//        }
//        }
//        });
