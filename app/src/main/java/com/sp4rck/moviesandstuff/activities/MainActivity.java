package com.sp4rck.moviesandstuff.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
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
import com.sp4rck.moviesandstuff.fragments.DetailFragment;
import com.sp4rck.moviesandstuff.fragments.MainFragment;
import com.sp4rck.moviesandstuff.model.SearchItem;
import com.sp4rck.moviesandstuff.model.SearchResult;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements MainFragment.Callback {

    private SpiceManager spiceManager = SpiceService.GetInstance().GetSpiceManager();
    private boolean _twoPane = false;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        setContentView(R.layout.main);
        if(findViewById(R.id.detail_container) != null){
            _twoPane = true;
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction().replace(R.id.detail_container, new DetailFragment()).commit();
            }
        }else{
            _twoPane = false;
        }
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
            getSupportFragmentManager().findFragmentById(R.id.main_fragment).startActivityForResult(new Intent(MainActivity.this, HistoryActivity.class), 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemSelected(String imdbID) {
        if(_twoPane) {
            DetailFragment df = new DetailFragment();

            Bundle args = new Bundle();
            args.putString("imdbId", imdbID);
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();
        }else{
            Intent intent = new Intent(this, DetailActivity.class).putExtra(Intent.EXTRA_TEXT, imdbID);
            startActivity(intent);
        }
    }
}
