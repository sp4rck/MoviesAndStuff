package com.sp4rck.moviesandstuff.OMDBApi;

import com.octo.android.robospice.SpiceManager;

/**
 * Created by allie_000 on 20/04/2015.
 */
public class SpiceService {

    private static SpiceService _self = null;
    private SpiceManager spiceManager = new SpiceManager(
            JsonSpiceService.class);

    public SpiceManager GetSpiceManager(){
        return spiceManager;
    }

    public static SpiceService GetInstance(){
        if(_self == null)
            _self = new SpiceService();
        return _self;
    }
}
