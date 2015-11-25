package com.assignment.sjsu.hudoassenco.cmpe137;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SearchableActivity extends AppCompatActivity {

    private SearchView _searchView;
    private RecyclerView _resultsView;
    private RecyclerView.LayoutManager _layoutManager;
    private RecyclerView.Adapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        _resultsView = (RecyclerView) findViewById(R.id.result_view);
        _layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        _adapter = new SearchResultAdapter();

        _resultsView.setLayoutManager(_layoutManager);
        _resultsView.setAdapter(_adapter);

        Intent intent = getIntent();
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        _searchView = (SearchView) menu.findItem(R.id.search_action_search_view).getActionView();
        // Assumes current activity is the searchable activity
        _searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        _searchView.setIconifiedByDefault(false);

        // TODO: Bug with searchView and request focus. Keyboard not showing up.

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("CMPE137", "Query: " + query);
            //TODO: Search
        }
    }
}
