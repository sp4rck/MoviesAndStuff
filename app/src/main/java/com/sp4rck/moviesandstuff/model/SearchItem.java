package com.sp4rck.moviesandstuff.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchItem {
    @JsonProperty(value = "Title")
    private String Title;
    @JsonProperty(value = "Year")
    private String Year;
    @JsonProperty(value = "imdbID")
    private String imdbID;
    @JsonProperty(value = "Type")
    private String Type;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    @Override
    public String toString(){
        return Title+" ("+Type+", "+Year+")";
    }
}
