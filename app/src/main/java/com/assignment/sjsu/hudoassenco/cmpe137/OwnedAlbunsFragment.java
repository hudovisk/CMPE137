package com.assignment.sjsu.hudoassenco.cmpe137;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONObject;

public class OwnedAlbunsFragment extends Fragment {

    private RecyclerView _albunsView;
    private RecyclerView.LayoutManager _layoutManager;
    private RecyclerView.Adapter _adapter;

    public OwnedAlbunsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("CMPE137",response.getRawResponse());
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "photos{images}");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_owned_albuns, container, false);

        _albunsView = (RecyclerView) rootView.findViewById(R.id.owned_albuns_view);
        _layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
//        _layoutManager = new GridLayoutManager(getContext(), 2); //2 columns
//        _layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        _adapter = new OwnedAlbunsAdapter();

        _albunsView.setHasFixedSize(true);
        _albunsView.setLayoutManager(_layoutManager);
        _albunsView.setAdapter(_adapter);

        return rootView;
    }

}
