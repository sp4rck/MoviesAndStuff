package com.sp4rck.moviesandstuff.OMDBApi;

import android.util.Log;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.sp4rck.moviesandstuff.model.Item;

/**
 * Created by allie_000 on 19/04/2015.
 */
public class ItemRequest extends SpringAndroidSpiceRequest<Item>{

    public final String ombdAPIUrl = "http://www.omdbapi.com/?i=%s";

    private String _searchText;

    public ItemRequest(String imdbId){
        super(Item.class);
        _searchText = imdbId;
    }

    @Override
    public Item loadDataFromNetwork() throws Exception {
        String url = String.format(ombdAPIUrl, _searchText);
        Log.d("SEARCH_REQUEST", "url = " + url);
        return getRestTemplate().getForObject(url, Item.class);
    }
}
