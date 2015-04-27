package com.sp4rck.moviesandstuff.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.sp4rck.moviesandstuff.OMDBApi.SearchRequest;
import com.sp4rck.moviesandstuff.OMDBApi.SpiceService;
import com.sp4rck.moviesandstuff.R;
import com.sp4rck.moviesandstuff.activities.DetailActivity;
import com.sp4rck.moviesandstuff.activities.MainActivity;
import com.sp4rck.moviesandstuff.data.DBContract;
import com.sp4rck.moviesandstuff.data.HistoryAdapter;
import com.sp4rck.moviesandstuff.model.SearchItem;
import com.sp4rck.moviesandstuff.model.SearchResult;

/**
 * Created by allie_000 on 27/04/2015.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String KEY_LAST_REQUEST_CACHE_KEY = "lastRequestCacheKey";
    private static final int URL_LOADER = 1;

    private SpiceManager spiceManager = SpiceService.GetInstance().GetSpiceManager();

    private ArrayAdapter<SearchItem> searchAdapter;

    private HistoryAdapter _historyAdapter;

    private String lastRequestCacheKey;

    private ProgressDialog progressDialog;

    private View rootView;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == getActivity().RESULT_OK) {
                ((AutoCompleteTextView) rootView.findViewById(R.id.search_field)).setText(data.getStringExtra("selectedSearch"));
                performSearchRequest(data.getStringExtra("selectedSearch"));
            }
        }else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        getLoaderManager().initLoader(URL_LOADER, null, MainFragment.this);
        _historyAdapter = new HistoryAdapter(getActivity(), null, 0);

        progressDialog = new ProgressDialog(getActivity());
        Button searchButton = (Button) rootView.findViewById(R.id.search_button);

        final AutoCompleteTextView searchQuery = (AutoCompleteTextView) rootView.findViewById(R.id.search_field);
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
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);
                    performSearchRequest(searchQuery.getText().toString());
                    LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.search_layout);
                    linearLayout.requestFocus();
                }
                return false;
            }
        });
        ListView searchList = (ListView) rootView.findViewById(R.id.search_results);

        searchAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1);
        searchList.setAdapter(searchAdapter);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) getActivity())
                        .onItemSelected(searchAdapter.getItem(position).getImdbID());
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearchRequest(searchQuery.getText().toString());
                // clear focus
                LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.search_layout);
                linearLayout.requestFocus();
                // hide keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchQuery.getWindowToken(), 0);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_LAST_REQUEST_CACHE_KEY)) {
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
    public void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(lastRequestCacheKey)) {
            outState.putString(KEY_LAST_REQUEST_CACHE_KEY, lastRequestCacheKey);
        }
        super.onSaveInstanceState(outState);
    }

    private void performSearchRequest(String searchText) {
        progressDialog.setTitle("Searching...");
        progressDialog.setMessage("In progress");
        progressDialog.show();
        ((ActionBarActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
        SearchRequest request = new SearchRequest(searchText);
        lastRequestCacheKey = request.createCacheKey();
        ContentValues values = new ContentValues();
        long i =  System.currentTimeMillis() / 1000L;
        values.put(DBContract.HistoryEntry.COLUMN_DATE, i);
        values.put(DBContract.HistoryEntry.COLUMN_SEARCH, searchText);
        String[] strs = {searchText};
        Cursor c = getActivity().getContentResolver().query(DBContract.HistoryEntry.CONTENT_URI, null, DBContract.HistoryEntry.COLUMN_SEARCH + "=?", strs, null);
        if (c != null && !c.isAfterLast()) {
            getActivity().getContentResolver().update(DBContract.HistoryEntry.CONTENT_URI, values, DBContract.HistoryEntry.COLUMN_SEARCH + "=?", strs);
        }else{
            getActivity().getContentResolver().insert(DBContract.HistoryEntry.CONTENT_URI,values);
        }
        spiceManager.execute(request, lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new ListSearchResultsRequestListener());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(getActivity(),
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
            Toast.makeText(getActivity(),
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onRequestSuccess(SearchResult searchList) {

            searchAdapter.clear();

            progressDialog.hide();
            if (searchList == null || searchList.getSearch() == null) {
                Toast.makeText(getActivity(),
                        "No results for this request", Toast.LENGTH_LONG)
                        .show();
                getActivity().setProgressBarIndeterminateVisibility(false);
                return;
            }
            searchAdapter.addAll(searchList.getSearch());

            searchAdapter.notifyDataSetChanged();

            getActivity().setProgressBarIndeterminateVisibility(false);

        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String imdbID);
    }
}
