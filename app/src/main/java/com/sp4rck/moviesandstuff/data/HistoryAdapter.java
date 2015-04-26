package com.sp4rck.moviesandstuff.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.sp4rck.moviesandstuff.R;

/**
 * Created by allie_000 on 26/04/2015.
 */
public class HistoryAdapter extends CursorAdapter {

    public HistoryAdapter(Context context, Cursor c, int flags){
        super(context,c,flags);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView)view;
        tv.setText(cursor.getString(cursor.getColumnIndex(DBContract.HistoryEntry.COLUMN_SEARCH)));
    }
}
