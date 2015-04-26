package com.sp4rck.moviesandstuff.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by allie_000 on 19/04/2015.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchList extends ArrayList<SearchItem> {
}
