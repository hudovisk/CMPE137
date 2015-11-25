package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
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

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hudoassenco on 11/24/15.
 */
public class NewAlbumActivity extends AppCompatActivity {

    private EditText _albunNameView;

    private AutoCompleteTextView _collaboratorView;

    private ImageButton _addCollaboratorButton;

    private RecyclerView _collaboratorsListView;
    private RecyclerView.LayoutManager _layoutManager;
    private RecyclerView.Adapter _adapter;

    private ArrayList<String> _friendsSuggestions;
    private ArrayList<String> _friendsSuggestionsId;
    private ArrayList<String> _friendsSuggestionsPictureUrl;
    private ArrayList<CollaboratorsAdapter.Collaborator> _collaborators;

    public NewAlbumActivity() {
        _friendsSuggestions = new ArrayList<>();
        _friendsSuggestionsId = new ArrayList<>();
        _friendsSuggestionsPictureUrl = new ArrayList<>();
        _collaborators = new ArrayList<>();

        _adapter = new CollaboratorsAdapter(_collaborators);
        _layoutManager = new LinearLayoutManager(NewAlbumActivity.this, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d("CMPE137", "onSaveInstanceState");

        outState.putInt("size",_collaborators.size());

        for(int i=0; i<_collaborators.size(); i++) {
            outState.putString("name_"+i, _collaborators.get(i).name);
            outState.putString("id_"+i, _collaborators.get(i).id);
            outState.putString("pictureUrl_"+i, _collaborators.get(i).pictureUrl);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d("CMPE137", "onRestoreInstanceState");

        int size = savedInstanceState.getInt("size");
        Log.d("CMPE137", "Size: "+size);
        _collaborators = new ArrayList<>();
        for(int i=0; i<size; i++) {
            CollaboratorsAdapter.Collaborator collaborator = new CollaboratorsAdapter.Collaborator();
            collaborator.name = savedInstanceState.getString("name_"+i);
            collaborator.id = savedInstanceState.getString("id_"+i);
            collaborator.pictureUrl = savedInstanceState.getString("pictureUrl_"+i);
            _collaborators.add(collaborator);
        }
        _adapter = new CollaboratorsAdapter(_collaborators);
        _collaboratorsListView.setAdapter(_adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_album));

        _collaboratorView = (AutoCompleteTextView) findViewById(R.id.new_album_collaborator_input);
        _albunNameView = (EditText) findViewById(R.id.new_album_name_input);
        _collaboratorsListView = (RecyclerView) findViewById(R.id.new_album_colaborator_list);
        _addCollaboratorButton = (ImageButton) findViewById(R.id.new_album_add_collaborator_bt);

        _collaboratorsListView.setLayoutManager(_layoutManager);
        _collaboratorsListView.setAdapter(_adapter);

        _addCollaboratorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = _collaboratorView.getText().toString();
                String id = "";
                String pictureUrl = "";

                for(int i=0; i<_friendsSuggestions.size(); i++) {
                    if(_friendsSuggestions.get(i).equals(name)) {
                        id = _friendsSuggestionsId.get(i);
                        pictureUrl = _friendsSuggestionsPictureUrl.get(i);
                    }
                }

                if(!id.isEmpty()) {
                    CollaboratorsAdapter.Collaborator collaborator = new CollaboratorsAdapter.Collaborator();
                    collaborator.name = name;
                    collaborator.id = id;
                    collaborator.pictureUrl = pictureUrl;
                    _collaborators.add(collaborator);
                    _adapter.notifyItemInserted(_collaborators.size() - 1);

                    _collaboratorView.setText("");
                    _collaboratorView.requestFocus();
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
                //TODO: Save album here
                return true;
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
                _friendsSuggestions.clear();
                _friendsSuggestionsId.clear();
                for(int i=0; i < data.length(); i++) {
                    _friendsSuggestions.add(data.getJSONObject(i).getString("name"));
                    _friendsSuggestionsId.add(data.getJSONObject(i).getString("id"));
                    _friendsSuggestionsPictureUrl.add(data.getJSONObject(i)
                                                            .getJSONObject("picture")
                                                            .getJSONObject("data")
                                                            .getString("url"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewAlbumActivity.this, android.R.layout.simple_list_item_1, _friendsSuggestions);
                _collaboratorView.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
