package com.sp4rck.moviesandstuff.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.sp4rck.moviesandstuff.fragments.DetailFragment;
import com.sp4rck.moviesandstuff.R;


public class DetailActivity extends ActionBarActivity {

    private DetailFragment _fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        _fragment  = new DetailFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container,_fragment)
                    .commit();
        }
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home || id == R.id.up || id == R.id.homeAsUp){
            if(_fragment!= null && _fragment.GetProgressDialog() != null)
                _fragment.GetProgressDialog().dismiss();
            finish();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }
}
