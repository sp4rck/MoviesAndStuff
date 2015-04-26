package com.sp4rck.moviesandstuff.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by allie_000 on 26/04/2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    static final String DB_NAME = "moviesnstuff.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_HISTORY_TABLE = "CREATE TABLE " + DBContract.HistoryEntry.TABLE_NAME + " ("+
                DBContract.HistoryEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                DBContract.HistoryEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                DBContract.HistoryEntry.COLUMN_SEARCH + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBContract.HistoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
