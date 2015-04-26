package com.sp4rck.moviesandstuff.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.sp4rck.moviesandstuff.R;
import com.sp4rck.moviesandstuff.OMDBApi.SearchRequest;
import com.sp4rck.moviesandstuff.OMDBApi.SpiceService;
import com.sp4rck.moviesandstuff.data.DBContract;
import com.sp4rck.moviesandstuff.data.DBHelper;
import com.sp4rck.moviesandstuff.data.HistoryAdapter;
import com.sp4rck.moviesandstuff.model.SearchItem;
import com.sp4rck.moviesandstuff.model.SearchResult;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_LAST_REQUEST_CACHE_KEY = "lastRequestCacheKey";
    private static final int URL_LOADER = 1;

    private SpiceManager spiceManager = SpiceService.GetInstance().GetSpiceManager();

    private ArrayAdapter<SearchItem> searchAdapter;

    private HistoryAdapter _historyAdapter;

    private String lastRequestCacheKey;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportLoaderManager().initLoader(URL_LOADER, null, MainActivity.this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        setContentView(R.layout.main);
        progressDialog = new ProgressDialog(this);
        initUIComponents();
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_history) {
            startActivityForResult(new Intent(MainActivity.this, HistoryActivity.class), 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ((AutoCompleteTextView) findViewById(R.id.search_field)).setText(data.getStringExtra("selectedSearch"));
                performSearchRequest(data.getStringExtra("selectedSearch"));
            }
        }
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    private void initUIComponents() {
        _historyAdapter = new HistoryAdapter(this, null, 0);

        Button searchButton = (Button) findViewById(R.id.search_button);

        final AutoCompleteTextView searchQuery = (AutoCompleteTextView) findViewById(R.id.search_field);
        searchQuery.setAdapter(_historyAdapter);
        searchQuery.setThreshold(1);
        searchQuery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c =((Cursor)_historyAdapter.getItem(position));
                searchQuery.setText(c.getString(c.getColumnIndex(DBContract.HistoryEntry.COLUMN_SEARCH)));
            }
        });
        searchQuery.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);
                    performSearchRequest(searchQuery.getText().toString());
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.search_layout);
                    linearLayout.requestFocus();
                }
                return false;
            }
        });
        ListView searchList = (ListView) findViewById(R.id.search_results);

        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        searchList.setAdapter(searchAdapter);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class).putExtra(Intent.EXTRA_TEXT, searchAdapter.getItem(position).getImdbID());
                startActivity(intent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearchRequest(searchQuery.getText().toString());
                // clear focus
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.search_layout);
                linearLayout.requestFocus();
                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);
            }
        });
    }

    private void performSearchRequest(String searchText) {
        progressDialog.setTitle("Searching...");
        progressDialog.setMessage("In progress");
        progressDialog.show();
        MainActivity.this.setSupportProgressBarIndeterminateVisibility(true);
        SearchRequest request = new SearchRequest(searchText);
        lastRequestCacheKey = request.createCacheKey();
        ContentValues values = new ContentValues();
        long i =  System.currentTimeMillis() / 1000L;
        values.put(DBContract.HistoryEntry.COLUMN_DATE, i);
        values.put(DBContract.HistoryEntry.COLUMN_SEARCH, searchText);
        String[] strs = {searchText};
        Cursor c = getContentResolver().query(DBContract.HistoryEntry.CONTENT_URI, null, DBContract.HistoryEntry.COLUMN_SEARCH + "=?", strs, null);
        if (c != null && !c.isAfterLast()) {
            getContentResolver().update(DBContract.HistoryEntry.CONTENT_URI, values, DBContract.HistoryEntry.COLUMN_SEARCH + "=?", strs);
        }else{
            getContentResolver().insert(DBContract.HistoryEntry.CONTENT_URI,values);
        }
        spiceManager.execute(request, lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new ListSearchResultsRequestListener());
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(lastRequestCacheKey)) {
            outState.putString(KEY_LAST_REQUEST_CACHE_KEY, lastRequestCacheKey);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
            lastRequestCacheKey = savedInstanceState
                    .getString(KEY_LAST_REQUEST_CACHE_KEY);
            spiceManager.addListenerIfPending(SearchResult.class,
                    lastRequestCacheKey, new ListSearchResultsRequestListener());
            spiceManager.getFromCache(SearchResult.class,
                    lastRequestCacheKey, DurationInMillis.ONE_MINUTE,
                    new ListSearchResultsRequestListener());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(this,
                        DBContract.HistoryEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        _historyAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        _historyAdapter.swapCursor(null);
    }

    private class ListSearchResultsRequestListener implements RequestListener<SearchResult> {
        @Override
        public void onRequestFailure(SpiceException e) {
            progressDialog.hide();
            Toast.makeText(MainActivity.this,
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            MainActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onRequestSuccess(SearchResult searchList) {

            searchAdapter.clear();

            progressDialog.hide();
            if (searchList == null || searchList.getSearch() == null) {
                Toast.makeText(MainActivity.this,
                        "No results for this request", Toast.LENGTH_LONG)
                        .show();
                MainActivity.this.setProgressBarIndeterminateVisibility(false);
                return;
            }
            searchAdapter.addAll(searchList.getSearch());

            searchAdapter.notifyDataSetChanged();

            MainActivity.this.setProgressBarIndeterminateVisibility(false);

        }
    }


}
