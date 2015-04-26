package com.sp4rck.moviesandstuff.OMDBApi;

import android.util.Log;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.sp4rck.moviesandstuff.model.SearchList;
import com.sp4rck.moviesandstuff.model.SearchResult;

/**
 * Created by allie_000 on 19/04/2015.
 */
public class SearchRequest extends SpringAndroidSpiceRequest<SearchResult> {


    public final String ombdAPISearchUrl = "http://www.omdbapi.com/?s=%s";

    private String _searchText;

    public SearchRequest(String searchText){
        super(SearchResult.class);
        _searchText = searchText;
    }

    @Override
    public SearchResult loadDataFromNetwork() throws Exception {
        String url = String.format(ombdAPISearchUrl, _searchText);
        Log.d("SEARCH_REQUEST", "url = "+url);
        return getRestTemplate().getForObject(url, SearchResult.class);
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     * @return
     */
    public String createCacheKey() {
        return "searchResults." + _searchText;
    }
}
