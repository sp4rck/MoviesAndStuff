package com.sp4rck.moviesandstuff.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

/**
 * Created by allie_000 on 19/04/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult{
    @JsonProperty(value = "Search")
    private SearchList Search;

    public SearchList getSearch(){
        return Search;
    }

    public void setSearch(SearchList results){
        Search = results;
    }
}

