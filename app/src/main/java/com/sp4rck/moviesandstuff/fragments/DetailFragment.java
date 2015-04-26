package com.sp4rck.moviesandstuff.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.sp4rck.moviesandstuff.OMDBApi.ItemRequest;
import com.sp4rck.moviesandstuff.R;
import com.sp4rck.moviesandstuff.OMDBApi.SpiceService;
import com.sp4rck.moviesandstuff.model.Item;

import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Created by allie_000 on 19/04/2015.
 */
public class DetailFragment extends Fragment {

    private SpiceManager spiceManager = SpiceService.GetInstance().GetSpiceManager();

    private Item _item;
    private View rootView;

    private ProgressDialog progressDialog;

    public ProgressDialog GetProgressDialog(){
        return progressDialog;
    }

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        progressDialog = new ProgressDialog(getActivity());
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String imdbID = intent.getStringExtra(Intent.EXTRA_TEXT);
            performGetRequest(imdbID);
        }

        return rootView;
    }

    private class ListResultsRequestListener implements RequestListener<Item> {
        @Override
        public void onRequestFailure(SpiceException e) {
            progressDialog.hide();
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException exception = (HttpClientErrorException) e.getCause();
                Log.e("ERROR", exception.getResponseBodyAsString());
            }
            if (e.getCause() instanceof UnrecognizedPropertyException) {
                UnrecognizedPropertyException exception = (UnrecognizedPropertyException) e.getCause();
                Log.e("ERROR", exception.getLocation().toString());
            }
            Toast.makeText(getActivity(),
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onRequestSuccess(Item item) {
            progressDialog.hide();
            // listFollowers could be null just if contentManager.getFromCache(...)
            // doesn't return anything.
            if (item == null) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                return;
            }
            _item = item;
            UpdateView();
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }

    private void UpdateView() {
        getActivity().setTitle(_item.getTitle());
        ((TextView) rootView.findViewById(R.id.title)).setText(_item.getTitle());
        ((TextView) rootView.findViewById(R.id.year)).setText(_item.getYear());
        ((TextView) rootView.findViewById(R.id.genre)).setText(String.format(getString(R.string.genre), _item.getGenre()));
        ((TextView) rootView.findViewById(R.id.actors)).setText(String.format(getString(R.string.actors), _item.getActors()));
        if (_item.getPlot() == null) {
            ((TextView) rootView.findViewById(R.id.plot)).setHeight(0);
        } else {
            ((TextView) rootView.findViewById(R.id.plot)).setText(_item.getPlot());
        }
        ((TextView) rootView.findViewById(R.id.imdbRating)).setText(String.format(getString(R.string.imdbRating), _item.getImdbRating()));
        TextView imdbLinkView = ((TextView) rootView.findViewById(R.id.imdbLink));
        imdbLinkView.setText(Html.fromHtml("<a href=\"http://imdb.com/title/" + _item.getImdbID() + "/\">Link to IMDB</a>"));
        imdbLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://imdb.com/title/" + _item.getImdbID() + "/"));
                if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                    getActivity().startActivity(intent);
                }
            }
        });

        if (_item.getPoster() != null) {
            ImageLoader.getInstance().loadImage(_item.getPoster(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ((ImageView) rootView.findViewById(R.id.poster)).setImageBitmap(loadedImage);
                }
            });
        } else {
            ((ImageView) rootView.findViewById(R.id.poster)).setImageResource(R.drawable.noimageavailable);
        }
    }

    private void performGetRequest(String imdbId) {
        progressDialog.setTitle("Fetching data...");
        progressDialog.setMessage("In progress");
        progressDialog.show();
        getActivity().setProgressBarIndeterminateVisibility(true);
        ItemRequest request = new ItemRequest(imdbId);
        spiceManager.execute(request, null, DurationInMillis.ONE_MINUTE, new ListResultsRequestListener());
    }
}
