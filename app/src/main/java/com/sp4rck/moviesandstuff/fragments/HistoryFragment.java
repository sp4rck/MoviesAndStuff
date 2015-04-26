package com.sp4rck.moviesandstuff.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sp4rck.moviesandstuff.R;
import com.sp4rck.moviesandstuff.activities.MainActivity;
import com.sp4rck.moviesandstuff.data.DBContract;
import com.sp4rck.moviesandstuff.data.HistoryAdapter;

/**
 * Created by allie_000 on 26/04/2015.
 */
public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    // Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

    private HistoryAdapter historyAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        getLoaderManager().initLoader(URL_LOADER, null, this);

        final ListView historyList = (ListView) rootView.findViewById(R.id.history_list);

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(getActivity().getClass() != MainActivity.class) {
                    Intent intent = new Intent();
                    Cursor c =((Cursor)historyAdapter.getItem(position));
                    intent.putExtra("selectedSearch", c.getString(c.getColumnIndex(DBContract.HistoryEntry.COLUMN_SEARCH)));
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            }
        });

        historyAdapter = new HistoryAdapter(getActivity(), null, 0);
        historyList.setAdapter(historyAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(URL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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
                        DBContract.HistoryEntry.COLUMN_DATE + " DESC");
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        historyAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        historyAdapter.swapCursor(null);
    }
}
