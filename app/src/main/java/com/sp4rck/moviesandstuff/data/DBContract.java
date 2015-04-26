package com.sp4rck.moviesandstuff.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by allie_000 on 26/04/2015.
 */
public class DBContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.sp4rck.moviesandstuff";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class HistoryEntry implements BaseColumns{

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + "HISTORY";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath("HISTORY").build();



        public static final String TABLE_NAME = "SearchHistory";

        public static final String ID = "_id";

        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_SEARCH = "searchText";

        public static Uri buildHistoryUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }
}
